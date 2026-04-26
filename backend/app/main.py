import json
import os
import uuid
from dataclasses import dataclass
from datetime import datetime, timezone
from pathlib import Path
from typing import Literal
from urllib import error as urlerror
from urllib import request as urlrequest

from fastapi import FastAPI, HTTPException, status
from pydantic import BaseModel, Field

app = FastAPI(title="CITY WALLET Context API", version="2.0.0")

CONFIG_DIR = Path(__file__).resolve().parent.parent / "config" / "cities"
OLLAMA_BASE_URL = os.getenv("OLLAMA_BASE_URL", "http://127.0.0.1:11434")
OLLAMA_MODEL = os.getenv("OLLAMA_MODEL", "llama3.1:8b")


class UserContextInput(BaseModel):
    user_id: int
    city: str = "stuttgart"
    latitude: float
    longitude: float
    intent_signal: str = "browse"
    weather_condition: Literal["sunny", "cloudy", "rainy", "overcast"] = "overcast"
    temperature_c: float = 11.0
    event_tags: list[str] = Field(default_factory=list)
    hour_of_day: int = Field(ge=0, le=23)
    weekday: str = "Tuesday"
    movement_state: Literal["commuting", "browsing", "lingering"] = "browsing"


class MerchantRuleInput(BaseModel):
    merchant_id: str
    merchant_name: str
    category: str
    max_discount_percent: int = Field(ge=5, le=60)
    goal: str = "fill_quiet_hours"
    quiet_hour_multiplier: float = 1.0
    min_spend_eur: float = 0.0


class GenerateOfferRequest(BaseModel):
    user_context: UserContextInput
    merchant_rules: list[MerchantRuleInput]
    payone_density_by_merchant: dict[str, int]


class ContextStateResponse(BaseModel):
    context_id: str
    city: str
    composite_state: str
    trigger_reason: str
    visible_signals: dict[str, str]


class GeneratedWidget(BaseModel):
    theme: str
    emotion: str
    badge: str
    cta_text: str
    image_prompt: str


class GeneratedOfferResponse(BaseModel):
    offer_id: str
    merchant_id: str
    merchant_name: str
    title: str
    body: str
    discount_percent: int
    expires_at_epoch_ms: int
    qr_payload_seed: str
    widget: GeneratedWidget
    context: ContextStateResponse


class RedemptionCreateRequest(BaseModel):
    offer_id: str
    user_id: int
    merchant_id: str


class RedemptionCreateResponse(BaseModel):
    redemption_token: str
    qr_payload: str


class RedemptionValidateRequest(BaseModel):
    redemption_token: str


class RedemptionValidateResponse(BaseModel):
    valid: bool
    status: str
    cashback_eur: float
    redeemed_at_epoch_ms: int | None = None


class MerchantMetricsResponse(BaseModel):
    merchant_id: str
    generated_offers: int
    accepted_offers: int
    redeemed_offers: int
    acceptance_rate: float
    redemption_rate: float


class MerchantDashboardResponse(BaseModel):
    city: str
    generated_total: int
    accepted_total: int
    redeemed_total: int
    merchants: list[MerchantMetricsResponse]


@dataclass
class OfferRecord:
    offer_id: str
    merchant_id: str
    discount_percent: int
    accepted: bool = False
    redeemed: bool = False


OFFERS: dict[str, OfferRecord] = {}
TOKENS: dict[str, dict] = {}


def _load_city_config(city: str) -> dict:
    file_path = CONFIG_DIR / f"{city.lower()}.json"
    if not file_path.exists():
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"City config not found: {city}",
        )
    return json.loads(file_path.read_text(encoding="utf-8"))


def _compose_context_state(payload: UserContextInput, density: dict[str, int]) -> ContextStateResponse:
    city_cfg = _load_city_config(payload.city)
    low_density_threshold = city_cfg.get("low_density_threshold", 50)
    nearby_density = min(density.values()) if density else low_density_threshold
    density_state = "low_density" if nearby_density <= low_density_threshold else "normal_density"
    composite = (
        f"{payload.weather_condition}+{payload.weekday.lower()}_{payload.hour_of_day}h+"
        f"{payload.movement_state}+{density_state}"
    )
    trigger_reason = (
        "Context trigger: weather, time, movement and local demand indicate a high-probability conversion window."
    )
    return ContextStateResponse(
        context_id=str(uuid.uuid4()),
        city=payload.city,
        composite_state=composite,
        trigger_reason=trigger_reason,
        visible_signals={
            "weather": f"{payload.weather_condition} {payload.temperature_c}C",
            "time": f"{payload.weekday} {payload.hour_of_day}:00",
            "location": city_cfg.get("display_name", payload.city.title()),
            "demand_proxy": density_state,
        },
    )


def _score_rule(ctx: UserContextInput, rule: MerchantRuleInput, density_value: int, weather_weight: dict[str, int]) -> int:
    quiet_bonus = max(0, 100 - density_value)
    movement_bonus = 15 if ctx.movement_state == "browsing" else 5
    weather_bonus = weather_weight.get(rule.category, 0)
    intent_bonus = 10 if ctx.intent_signal == "warm_drink" and rule.category in {"coffee", "bakery"} else 0
    return int(quiet_bonus * rule.quiet_hour_multiplier) + movement_bonus + weather_bonus + intent_bonus


def _build_widget(category: str, weather: str) -> GeneratedWidget:
    if category in {"coffee", "bakery"} and weather in {"rainy", "overcast"}:
        return GeneratedWidget(
            theme="warm-minimal",
            emotion="cozy",
            badge="2 min away",
            cta_text="Warm up now",
            image_prompt="A cozy street cafe interior with steam rising from fresh coffee.",
        )
    return GeneratedWidget(
        theme="urban-clean",
        emotion="spontaneous",
        badge="Live nearby",
        cta_text="Claim and go",
        image_prompt="Modern city storefront with people walking by during lunchtime.",
    )


def _fallback_copy(winner: MerchantRuleInput, discount: int) -> tuple[str, str]:
    title = f"{discount}% off at {winner.merchant_name}"
    body = f"{winner.goal.replace('_', ' ').title()} now. Valid for the next 15 minutes while nearby."
    return title, body


def _generate_offer_copy_with_ollama(
    context: ContextStateResponse,
    request: GenerateOfferRequest,
    winner: MerchantRuleInput,
    discount: int,
) -> tuple[str, str, GeneratedWidget]:
    fallback_title, fallback_body = _fallback_copy(winner, discount)
    fallback_widget = _build_widget(winner.category, request.user_context.weather_condition)
    prompt = f"""
You create short in-app local commerce offers.
Return ONLY valid minified JSON with keys:
title, body, widget_theme, widget_emotion, widget_badge, widget_cta_text, widget_image_prompt.

Constraints:
- city: {context.city}
- merchant_name: {winner.merchant_name}
- category: {winner.category}
- discount_percent: {discount}
- intent_signal: {request.user_context.intent_signal}
- weather: {request.user_context.weather_condition}
- temperature_c: {request.user_context.temperature_c}
- weekday: {request.user_context.weekday}
- hour: {request.user_context.hour_of_day}
- movement_state: {request.user_context.movement_state}
- composite_context: {context.composite_state}
- keep title <= 60 chars
- keep body <= 140 chars
- no hashtags
- no emoji
""".strip()

    payload = {
        "model": OLLAMA_MODEL,
        "prompt": prompt,
        "format": "json",
        "stream": False,
    }
    req = urlrequest.Request(
        f"{OLLAMA_BASE_URL}/api/generate",
        data=json.dumps(payload).encode("utf-8"),
        headers={"Content-Type": "application/json"},
        method="POST",
    )
    try:
        with urlrequest.urlopen(req, timeout=8) as response:
            raw = response.read().decode("utf-8")
        envelope = json.loads(raw)
        content = envelope.get("response", "").strip()
        generated = json.loads(content) if content else {}
        title = str(generated.get("title") or fallback_title)
        body = str(generated.get("body") or fallback_body)
        widget = GeneratedWidget(
            theme=str(generated.get("widget_theme") or fallback_widget.theme),
            emotion=str(generated.get("widget_emotion") or fallback_widget.emotion),
            badge=str(generated.get("widget_badge") or fallback_widget.badge),
            cta_text=str(generated.get("widget_cta_text") or fallback_widget.cta_text),
            image_prompt=str(generated.get("widget_image_prompt") or fallback_widget.image_prompt),
        )
        return title[:60], body[:140], widget
    except (urlerror.URLError, TimeoutError, json.JSONDecodeError, ValueError):
        return fallback_title, fallback_body, fallback_widget


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok"}


@app.post("/context/evaluate", response_model=ContextStateResponse)
def evaluate_context(request: GenerateOfferRequest) -> ContextStateResponse:
    return _compose_context_state(request.user_context, request.payone_density_by_merchant)


@app.post("/offers/generate", response_model=GeneratedOfferResponse)
def generate_offer(request: GenerateOfferRequest) -> GeneratedOfferResponse:
    if not request.merchant_rules:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="At least one merchant rule is required")

    context = _compose_context_state(request.user_context, request.payone_density_by_merchant)
    weather_weight = {"coffee": 8, "bakery": 7, "food": 4, "fitness": -3} if request.user_context.weather_condition in {"rainy", "overcast"} else {"fitness": 6, "food": 3, "coffee": 2}
    scored = sorted(
        request.merchant_rules,
        key=lambda rule: _score_rule(
            request.user_context,
            rule,
            request.payone_density_by_merchant.get(rule.merchant_id, 50),
            weather_weight,
        ),
        reverse=True,
    )
    winner = scored[0]
    density = request.payone_density_by_merchant.get(winner.merchant_id, 50)
    discount_boost = 8 if density < 40 else 4 if density < 60 else 0
    discount = min(winner.max_discount_percent, 10 + discount_boost)

    expires = int((datetime.now(timezone.utc).timestamp() + 15 * 60) * 1000)
    offer_id = str(uuid.uuid4())
    OFFERS[offer_id] = OfferRecord(
        offer_id=offer_id,
        merchant_id=winner.merchant_id,
        discount_percent=discount,
        accepted=True,
    )

    title, body, widget = _generate_offer_copy_with_ollama(context, request, winner, discount)

    return GeneratedOfferResponse(
        offer_id=offer_id,
        merchant_id=winner.merchant_id,
        merchant_name=winner.merchant_name,
        title=title,
        body=body,
        discount_percent=discount,
        expires_at_epoch_ms=expires,
        qr_payload_seed=f"{offer_id}:{request.user_context.user_id}:{winner.merchant_id}",
        widget=widget,
        context=context,
    )


@app.post("/redemptions/create", response_model=RedemptionCreateResponse)
def create_redemption_token(payload: RedemptionCreateRequest) -> RedemptionCreateResponse:
    if payload.offer_id not in OFFERS:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Offer not found")

    token = str(uuid.uuid4())
    TOKENS[token] = {
        "offer_id": payload.offer_id,
        "user_id": payload.user_id,
        "merchant_id": payload.merchant_id,
        "validated": False,
    }
    qr_payload = f"CITYWALLET::{token}"
    return RedemptionCreateResponse(redemption_token=token, qr_payload=qr_payload)


@app.post("/redemptions/validate", response_model=RedemptionValidateResponse)
def validate_redemption(payload: RedemptionValidateRequest) -> RedemptionValidateResponse:
    token = TOKENS.get(payload.redemption_token)
    if not token:
        return RedemptionValidateResponse(valid=False, status="invalid_token", cashback_eur=0.0)

    if token["validated"]:
        return RedemptionValidateResponse(valid=False, status="already_redeemed", cashback_eur=0.0)

    token["validated"] = True
    offer = OFFERS.get(token["offer_id"])
    if offer:
        offer.redeemed = True
    cashback = round((offer.discount_percent if offer else 10) * 0.1, 2)
    return RedemptionValidateResponse(
        valid=True,
        status="redeemed",
        cashback_eur=cashback,
        redeemed_at_epoch_ms=int(datetime.now(timezone.utc).timestamp() * 1000),
    )


@app.get("/merchant/dashboard", response_model=MerchantDashboardResponse)
def merchant_dashboard(city: str = "stuttgart") -> MerchantDashboardResponse:
    _load_city_config(city)
    merchant_map: dict[str, list[OfferRecord]] = {}
    for offer in OFFERS.values():
        merchant_map.setdefault(offer.merchant_id, []).append(offer)

    merchants: list[MerchantMetricsResponse] = []
    generated_total = len(OFFERS)
    accepted_total = 0
    redeemed_total = 0

    for merchant_id, offers in merchant_map.items():
        generated = len(offers)
        accepted = sum(1 for item in offers if item.accepted)
        redeemed = sum(1 for item in offers if item.redeemed)
        accepted_total += accepted
        redeemed_total += redeemed
        merchants.append(
            MerchantMetricsResponse(
                merchant_id=merchant_id,
                generated_offers=generated,
                accepted_offers=accepted,
                redeemed_offers=redeemed,
                acceptance_rate=round((accepted / generated) * 100, 1) if generated else 0.0,
                redemption_rate=round((redeemed / generated) * 100, 1) if generated else 0.0,
            )
        )

    return MerchantDashboardResponse(
        city=city,
        generated_total=generated_total,
        accepted_total=accepted_total,
        redeemed_total=redeemed_total,
        merchants=sorted(merchants, key=lambda row: row.generated_offers, reverse=True),
    )

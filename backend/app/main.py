import json
import os
import uuid
from datetime import datetime
from typing import Literal, Optional

import httpx
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field

app = FastAPI(title="City-Wallet API", version="2.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

LATITUDE = 48.7758
LONGITUDE = 9.1829

# ── In-memory state ───────────────────────────────────────────────────────────

_merchant_rules: dict = {
    "merchant_name": "Café Königsbau",
    "merchant_emoji": "☕",
    "category": "Café · Specialty Coffee",
    "quiet_start": "14:00",
    "quiet_end": "17:00",
    "max_discount": 30,
    "goal": "traffic",
    "focus": ["coffee", "pastries"],
    "weather_boost": True,
    "weekend_boost": False,
    "daily_budget": 80.0,
    "distance_m": 80,
    "typical_price": 4.20,
}

_redemptions: list[dict] = []
_generated_offers: list[dict] = []

# ── Weather helpers ───────────────────────────────────────────────────────────

WMO_MAP: dict[int, tuple[str, str, str]] = {
    0: ("Clear sky", "☀️", "sunny"),
    1: ("Mainly clear", "🌤️", "sunny"),
    2: ("Partly cloudy", "⛅", "cloudy"),
    3: ("Overcast", "☁️", "overcast"),
    45: ("Foggy", "🌫️", "foggy"),
    48: ("Freezing fog", "🌫️", "foggy"),
    51: ("Light drizzle", "🌦️", "drizzly"),
    53: ("Drizzle", "🌦️", "drizzly"),
    55: ("Heavy drizzle", "🌦️", "drizzly"),
    61: ("Slight rain", "🌧️", "rainy"),
    63: ("Rain", "🌧️", "rainy"),
    65: ("Heavy rain", "🌧️", "rainy"),
    71: ("Slight snow", "🌨️", "snowy"),
    73: ("Snow", "🌨️", "snowy"),
    75: ("Heavy snow", "🌨️", "snowy"),
    80: ("Rain showers", "🌧️", "rainy"),
    81: ("Rain showers", "🌧️", "rainy"),
    82: ("Violent showers", "⛈️", "rainy"),
    95: ("Thunderstorm", "⛈️", "stormy"),
}


def _period(hour: int) -> str:
    if 6 <= hour < 11:
        return "morning"
    if 11 <= hour < 14:
        return "lunch"
    if 14 <= hour < 18:
        return "afternoon"
    if 18 <= hour < 22:
        return "evening"
    return "night"


def _composite(temp: float, wtype: str, period: str, is_weekend: bool) -> list[str]:
    tags: list[str] = []
    tags.append("cold" if temp < 10 else "cool" if temp < 18 else "warm")
    tags.append(wtype)
    if period == "lunch":
        tags.append("lunch_rush")
    if period == "morning":
        tags.append("morning_commute")
    if is_weekend:
        tags.append("weekend")
    return tags


# ── Models ────────────────────────────────────────────────────────────────────

class WeatherCtx(BaseModel):
    temp_c: float
    description: str
    icon: str
    weather_type: str
    precipitation_mm: float


class TimeCtx(BaseModel):
    hour: int
    period: str
    day_of_week: str
    is_weekend: bool


class LocationCtx(BaseModel):
    city: str
    district: str
    latitude: float
    longitude: float


class ContextResponse(BaseModel):
    weather: WeatherCtx
    time: TimeCtx
    location: LocationCtx
    composite_state: list[str]
    context_label: str


class MerchantRules(BaseModel):
    merchant_name: str = "Café Königsbau"
    merchant_emoji: str = "☕"
    category: str = "Café · Specialty Coffee"
    quiet_start: str = "14:00"
    quiet_end: str = "17:00"
    max_discount: int = Field(default=30, ge=5, le=50)
    goal: Literal["traffic", "inventory", "loyalty"] = "traffic"
    focus: list[str] = Field(default_factory=lambda: ["coffee", "pastries"])
    weather_boost: bool = True
    weekend_boost: bool = False
    daily_budget: float = 80.0
    distance_m: int = 80
    typical_price: float = 4.20


class GenerateRequest(BaseModel):
    context: Optional[dict] = None
    rules: Optional[MerchantRules] = None


class GeneratedOffer(BaseModel):
    id: str
    merchant: str
    merchant_emoji: str
    category: str
    distance_m: int
    headline: str
    subline: str
    discount: int
    original_price: float
    final_price: float
    reasoning: str
    valid_seconds: int
    emoji: str
    theme: str
    bonus_item: Optional[str] = None
    token: str
    generated_at: str


class RedeemRequest(BaseModel):
    offer_id: str
    token: str
    merchant: str
    discount: int
    original_price: float
    final_price: float


class RedeemResponse(BaseModel):
    success: bool
    cashback: float
    message: str


class AnalyticsResponse(BaseModel):
    offers_generated_today: int
    acceptance_rate: float
    payone_uplift: float
    delta_offers: str
    delta_acceptance: str
    delta_uplift: str
    recent_offers: list[dict]
    hourly_data: list[dict]
    weekly_uplift: list[dict]
    top_templates: list[dict]


# ── Endpoints ─────────────────────────────────────────────────────────────────

@app.get("/health")
async def health() -> dict:
    return {"status": "ok", "version": "2.0.0"}


@app.get("/context", response_model=ContextResponse)
async def get_context() -> ContextResponse:
    try:
        async with httpx.AsyncClient(timeout=5.0) as client:
            r = await client.get(
                "https://api.open-meteo.com/v1/forecast",
                params={
                    "latitude": LATITUDE,
                    "longitude": LONGITUDE,
                    "current": "temperature_2m,precipitation,weather_code",
                    "timezone": "Europe/Berlin",
                },
            )
            r.raise_for_status()
            cur = r.json()["current"]
            temp_c = round(float(cur["temperature_2m"]), 1)
            precip = round(float(cur["precipitation"]), 1)
            wmo = int(cur["weather_code"])
    except Exception:
        temp_c, precip, wmo = 11.0, 0.0, 3

    desc, icon, wtype = WMO_MAP.get(wmo, ("Cloudy", "☁️", "overcast"))
    now = datetime.now()
    hour = now.hour
    dow = now.strftime("%A")
    is_weekend = now.weekday() >= 5
    period = _period(hour)
    composite = _composite(temp_c, wtype, period, is_weekend)
    temp_label = "Cold" if temp_c < 10 else "Cool" if temp_c < 18 else "Warm"

    return ContextResponse(
        weather=WeatherCtx(
            temp_c=temp_c,
            description=desc,
            icon=icon,
            weather_type=wtype,
            precipitation_mm=precip,
        ),
        time=TimeCtx(hour=hour, period=period, day_of_week=dow, is_weekend=is_weekend),
        location=LocationCtx(
            city="Stuttgart",
            district="Mitte",
            latitude=LATITUDE,
            longitude=LONGITUDE,
        ),
        composite_state=composite,
        context_label=f"{temp_label} & {desc} · {dow} {period.capitalize()}",
    )


@app.get("/merchant/rules", response_model=MerchantRules)
async def get_rules() -> MerchantRules:
    return MerchantRules(**_merchant_rules)


@app.post("/merchant/rules", response_model=MerchantRules)
async def save_rules(rules: MerchantRules) -> MerchantRules:
    global _merchant_rules
    _merchant_rules = rules.model_dump()
    return rules


@app.post("/offers/generate", response_model=GeneratedOffer)
async def generate_offer(req: GenerateRequest) -> GeneratedOffer:
    if req.context:
        ctx = req.context
    else:
        ctx_obj = await get_context()
        ctx = ctx_obj.model_dump()

    rules = req.rules if req.rules else MerchantRules(**_merchant_rules)
    offer_data = await _call_claude(ctx, rules)
    offer = GeneratedOffer(**offer_data)
    _generated_offers.append(offer.model_dump())
    return offer


@app.post("/offers/redeem", response_model=RedeemResponse)
async def redeem_offer(req: RedeemRequest) -> RedeemResponse:
    cashback = round(req.original_price - req.final_price, 2)
    _redemptions.append(
        {**req.model_dump(), "redeemed_at": datetime.now().isoformat(), "cashback": cashback}
    )
    return RedeemResponse(
        success=True,
        cashback=cashback,
        message=f"Discount applied. €{cashback:.2f} saved via Payone (simulated).",
    )


@app.get("/merchant/analytics", response_model=AnalyticsResponse)
async def get_analytics() -> AnalyticsResponse:
    today = 247 + len(_generated_offers)
    accepted = len(_redemptions)
    rate = min(round(38.2 + (accepted / max(today, 1)) * 10, 1), 65.0)
    uplift = round(312.0 + sum(r["cashback"] for r in _redemptions), 2)

    recent_live = [
        {
            "title": f"{r['merchant']} · −{r['discount']}%",
            "context": "User redeemed",
            "time": "just now",
            "discount": r["discount"],
            "status": "Accepted",
        }
        for r in reversed(_redemptions[-2:])
    ]
    simulated = [
        {"title": "Cappuccino · −30% to user 80m away", "context": "Cold/Rain trigger", "time": "2 min ago", "discount": 30, "status": "Accepted"},
        {"title": "Butter croissant · −20% lunchtime push", "context": "Quiet-hours rule", "time": "8 min ago", "discount": 20, "status": "Generated"},
        {"title": "Flat white · −25% to passerby", "context": "Foot-traffic goal", "time": "14 min ago", "discount": 25, "status": "Accepted"},
        {"title": "Pain au chocolat · −40% inventory clear", "context": "End-of-day rule", "time": "22 min ago", "discount": 40, "status": "Dismissed"},
    ]
    recent = (recent_live + simulated)[:4]

    gen_count = len(_generated_offers)
    acc_count = accepted
    top_rate = round((41 + acc_count) / max(86 + gen_count, 1) * 100, 1)

    return AnalyticsResponse(
        offers_generated_today=today,
        acceptance_rate=rate,
        payone_uplift=uplift,
        delta_offers="+12% vs yesterday",
        delta_acceptance="+4.1 pp",
        delta_uplift="▲ 22% week-on-week",
        recent_offers=recent,
        hourly_data=[
            {"h": "08", "offers": 12, "accepted": 5},
            {"h": "10", "offers": 18, "accepted": 7},
            {"h": "12", "offers": 36, "accepted": 16},
            {"h": "14", "offers": 42 + gen_count, "accepted": 18 + acc_count},
            {"h": "15", "offers": 38, "accepted": 17},
            {"h": "16", "offers": 48, "accepted": 21},
            {"h": "17", "offers": 28, "accepted": 11},
            {"h": "18", "offers": 25, "accepted": 9},
        ],
        weekly_uplift=[
            {"d": "Mon", "uplift": 180},
            {"d": "Tue", "uplift": 210},
            {"d": "Wed", "uplift": 240},
            {"d": "Thu", "uplift": 220},
            {"d": "Fri", "uplift": 285},
            {"d": "Sat", "uplift": 305},
            {"d": "Sun", "uplift": int(uplift)},
        ],
        top_templates=[
            {"template": "Cold rain → Cappuccino −30%", "sent": 86 + gen_count, "accepted": 41 + acc_count, "rate": f"{top_rate}%"},
            {"template": "Quiet hours → Croissant −20%", "sent": 64, "accepted": 22, "rate": "34.4%"},
            {"template": "Foot traffic → Flat white −25%", "sent": 52, "accepted": 19, "rate": "36.5%"},
            {"template": "End-of-day → Pastry −40%", "sent": 45, "accepted": 12, "rate": "26.7%"},
        ],
    )


# ── Claude integration ────────────────────────────────────────────────────────

PERIOD_THEMES: dict[str, str] = {
    "morning": "amber",
    "lunch": "green",
    "afternoon": "blue",
    "evening": "rose",
    "night": "purple",
}

FALLBACKS = [
    {
        "headline": "Cold outside? Your cappuccino is waiting just 80 m away.",
        "subline": "House-roasted beans, oat milk on the house.",
        "discount": 25,
        "emoji": "☕",
        "bonus_item": "free oat-milk shot",
        "reasoning": "Rain + quiet hours: perfect moment for a warm drink.",
        "valid_seconds": 900,
    },
    {
        "headline": "Freshly brewed — 80 metres from where you're standing.",
        "subline": "Warm up with our signature blend. Ready right now.",
        "discount": 20,
        "emoji": "☕",
        "bonus_item": "free biscuit",
        "reasoning": "Overcast & slow afternoon — a good time to attract walk-ins.",
        "valid_seconds": 1200,
    },
    {
        "headline": "The café around the corner just brewed a fresh batch.",
        "subline": "80 m away. Still steaming. Grab it before it's gone.",
        "discount": 15,
        "emoji": "☕",
        "bonus_item": None,
        "reasoning": "Low demand signal + you're nearby = offer triggers now.",
        "valid_seconds": 720,
    },
]


OLLAMA_URL = os.getenv("OLLAMA_URL", "http://localhost:11434")
OLLAMA_MODEL = os.getenv("OLLAMA_MODEL", "llama3.2:3b")


def _build_prompt(ctx: dict, rules: MerchantRules) -> str:
    weather = ctx.get("weather", {})
    time_ctx = ctx.get("time", {})
    composite = ctx.get("composite_state", [])
    hour = int(time_ctx.get("hour", 14))
    period = time_ctx.get("period", "afternoon")
    qs = int(rules.quiet_start.split(":")[0])
    qe = int(rules.quiet_end.split(":")[0])
    is_quiet = qs <= hour < qe
    goal_text = {
        "traffic": "attract new walk-in customers",
        "inventory": "clear remaining inventory before end of day",
        "loyalty": "reward and retain loyal regulars",
    }.get(rules.goal, "attract customers")

    return f"""You create one short marketing offer for a local café. Reply with ONLY a JSON object — no explanation, no markdown, nothing else.

Situation: {weather.get("temp_c", 11)}°C, {weather.get("description", "overcast")}, {time_ctx.get("day_of_week", "Tuesday")} at {hour}:00 ({period}). Quiet hours: {"yes" if is_quiet else "no"} ({rules.quiet_start}–{rules.quiet_end}). Signals: {", ".join(composite) if composite else "none"}.

Café: {rules.merchant_name}, {rules.category}. Goal: {goal_text}. Focus: {", ".join(rules.focus) if rules.focus else "all items"}. Max discount: {rules.max_discount}%. Price: €{rules.typical_price:.2f}. User is {rules.distance_m} m away.

Rules: headline max 20 words, subline max 12 words, discount integer 5–{rules.max_discount}, emoji single character, reasoning max 15 words, valid_seconds 600–1200, bonus_item a short string like "free biscuit" or the word null.

JSON (fill in the values, keep all 7 keys):
{{"headline":"FILL","subline":"FILL","discount":20,"emoji":"☕","reasoning":"FILL","valid_seconds":900,"bonus_item":null}}"""


def _normalise_offer_json(data: dict) -> dict:
    """Coerce types that small models sometimes get wrong."""
    # bonus_item may come back as a dict or list — stringify or null it
    bonus = data.get("bonus_item")
    if isinstance(bonus, (dict, list)):
        bonus = None
    elif isinstance(bonus, str) and bonus.lower() in ("null", "none", ""):
        bonus = None
    data["bonus_item"] = bonus

    # Clamp valid_seconds
    try:
        data["valid_seconds"] = min(max(int(data.get("valid_seconds", 900)), 600), 1800)
    except (TypeError, ValueError):
        data["valid_seconds"] = 900

    # Ensure emoji is a single token
    emoji = str(data.get("emoji", "☕"))
    data["emoji"] = emoji[:4]  # keep first emoji cluster (up to 4 bytes)

    return data


async def _call_ollama(prompt: str) -> dict:
    """Call local Ollama model and parse JSON response."""
    async with httpx.AsyncClient(timeout=50.0) as client:
        r = await client.post(
            f"{OLLAMA_URL}/api/generate",
            json={"model": OLLAMA_MODEL, "prompt": prompt, "stream": False},
        )
        r.raise_for_status()
        raw = r.json()["response"].strip()
        # Extract first complete JSON object
        start = raw.find("{")
        if start == -1:
            raise ValueError("no JSON object in response")
        # Walk forward to find a parseable object
        for end in range(len(raw), start, -1):
            if raw[end - 1] == "}":
                try:
                    data = json.loads(raw[start:end])
                    return _normalise_offer_json(data)
                except json.JSONDecodeError:
                    continue
        raise ValueError("could not parse JSON from response")


async def _call_anthropic(prompt: str, api_key: str) -> dict:
    """Call Anthropic Claude and parse JSON response."""
    import anthropic as _ant

    client = _ant.Anthropic(api_key=api_key)
    msg = client.messages.create(
        model="claude-haiku-4-5-20251001",
        max_tokens=400,
        messages=[{"role": "user", "content": prompt}],
    )
    raw = msg.content[0].text.strip()
    start, end = raw.find("{"), raw.rfind("}") + 1
    return json.loads(raw[start:end])


async def _call_claude(ctx: dict, rules: MerchantRules) -> dict:
    import random

    time_ctx = ctx.get("time", {})
    period = time_ctx.get("period", "afternoon")
    theme = PERIOD_THEMES.get(period, "blue")
    prompt = _build_prompt(ctx, rules)

    # 1. Try Anthropic if key is set
    api_key = os.getenv("ANTHROPIC_API_KEY")
    if api_key:
        try:
            data = await _call_anthropic(prompt, api_key)
            disc = min(max(int(data.get("discount", 20)), 5), rules.max_discount)
            return _build(data, disc, rules, theme)
        except Exception:
            pass  # fall through to Ollama

    # 2. Try local Ollama
    try:
        data = await _call_ollama(prompt)
        disc = min(max(int(data.get("discount", 20)), 5), rules.max_discount)
        return _build(data, disc, rules, theme)
    except Exception:
        pass  # fall through to static fallback

    # 3. Static fallback
    fb = random.choice(FALLBACKS)
    return _build(fb, min(fb["discount"], rules.max_discount), rules, theme)


def _build(data: dict, disc: int, rules: MerchantRules, theme: str) -> dict:
    orig = rules.typical_price
    final = round(orig * (1 - disc / 100), 2)
    bonus = data.get("bonus_item")
    if bonus in (None, "null", ""):
        bonus = None
    return {
        "id": str(uuid.uuid4()),
        "merchant": rules.merchant_name,
        "merchant_emoji": rules.merchant_emoji,
        "category": rules.category,
        "distance_m": rules.distance_m,
        "headline": str(data.get("headline", "Special offer just for you.")),
        "subline": str(data.get("subline", "Limited time only.")),
        "discount": disc,
        "original_price": orig,
        "final_price": final,
        "reasoning": str(data.get("reasoning", "Context-aware offer for this moment.")),
        "valid_seconds": min(int(data.get("valid_seconds", 900)), 1800),
        "emoji": str(data.get("emoji", rules.merchant_emoji)),
        "theme": theme,
        "bonus_item": bonus,
        "token": f"CW-{uuid.uuid4().hex[:8].upper()}",
        "generated_at": datetime.now().isoformat(),
    }

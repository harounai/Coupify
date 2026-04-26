from __future__ import annotations

import asyncio
import json
import uuid
from datetime import datetime

from sqlalchemy.orm import Session

from app.models import Business, CouponTemplate, Notification, User
from app.services.ai_ranker import AiRanker
from app.services.context_signals import get_demand_signal, get_events_signal, get_weather_signal
from app.services.offers_service import create_coupon_instance, list_businesses_with_distance
from app.services.signals import LocationSignal, TimeSignal


def _today_key(now: datetime) -> str:
    return now.strftime("%Y-%m-%d")


def _build_candidates(db: Session, *, lat: float, lon: float) -> tuple[list[dict], list[CouponTemplate], list[tuple[Business, float]]]:
    templates = db.query(CouponTemplate).all()
    businesses_with_distance = list_businesses_with_distance(db, user_lat=lat, user_lon=lon)
    business_ids = [b.id for b, _ in businesses_with_distance]
    now = datetime.utcnow()
    demand = get_demand_signal(db, business_ids, now)

    candidates: list[dict] = []
    for tpl in templates:
        match = next(((b, d) for (b, d) in businesses_with_distance if b.category == tpl.category), None)
        if not match:
            match = businesses_with_distance[0]
        b, dist = match
        candidates.append(
            {
                "id": f"{tpl.id}::{b.id}",
                "template_id": tpl.id,
                "business_id": b.id,
                "category": tpl.category,
                "base_discount": tpl.base_discount,
                "distance_km": round(dist, 2),
                "demand_level": demand.demand_by_business_id.get(b.id, 50),
            }
        )
    return candidates, templates, businesses_with_distance


def build_should_notify_prompt(
    *,
    user_id: str,
    user_preferences: list[str],
    exploration_preference: int,
    signals: dict,
    candidates: list[dict],
) -> str:
    schema = {
        "type": "object",
        "required": ["should_notify", "candidate_id", "title", "body", "reason"],
        "properties": {
            "should_notify": {"type": "boolean"},
            "candidate_id": {"type": "string"},
            "title": {"type": "string"},
            "body": {"type": "string"},
            "reason": {"type": "string"},
        },
        "additionalProperties": False,
    }
    payload = {
        "user": {"id": user_id, "preferences": user_preferences, "exploration_preference": exploration_preference},
        "signals": signals,
        "candidates": candidates[:12],
        "output_schema": schema,
    }
    return (
        "You are an AI notification decision engine for a city offers app.\n"
        "Return ONLY valid JSON matching output_schema.\n\n"
        "Decide if we should notify the user RIGHT NOW.\n"
        "- Notify only if the timing is good (daypart/weekend), and there is a strong match.\n"
        "- Avoid spamming: if unsure, set should_notify=false.\n"
        "- Pick exactly one candidate_id.\n"
        "- Title/body must be short and compelling.\n\n"
        f"INPUT:\n{json.dumps(payload, ensure_ascii=False)}\n"
    )


async def tick_generate_notifications(db: Session) -> int:
    """
    One tick: for each user, decide whether to create a new backend notification.
    Returns how many notifications were created.
    """
    created = 0
    now = datetime.utcnow()
    day_key = _today_key(now)

    users = db.query(User).all()
    if not users:
        return 0

    for user in users:
        if not user.has_completed_onboarding:
            continue

        # Basic anti-spam: max 1 notification per user per day for now.
        already_today = (
            db.query(Notification)
            .filter(Notification.user_id == user.id)
            .filter(Notification.created_at >= datetime(now.year, now.month, now.day))
            .first()
        )
        if already_today:
            continue

        interests = [s.strip() for s in (user.interests_csv or "").split(",") if s.strip()]

        # Use default location for now; can be replaced with real last-known user location later.
        lat, lon = 48.137154, 11.576124
        candidates, templates, businesses_with_distance = _build_candidates(db, lat=lat, lon=lon)
        if not candidates:
            continue

        business_ids = [b.id for b, _ in businesses_with_distance]
        weather = await get_weather_signal(lat=lat, lon=lon, now=now)
        demand = get_demand_signal(db, business_ids, now)
        events = get_events_signal(now)
        time = TimeSignal(now=now)
        location = LocationSignal(lat=lat, lon=lon)

        # Step 1: rank candidates (deterministic fallback if ollama unavailable)
        ranker = AiRanker()
        ranked_ids, _, _ = await ranker.rank_home_feed(
            user_id=user.id,
            user_preferences=interests,
            exploration_preference=user.exploration_preference,
            weather=weather,
            time=time,
            location=location,
            demand=demand,
            events=events,
            candidates=candidates,
        )
        ranked_set = {cid: i for i, cid in enumerate(ranked_ids)}
        candidates.sort(key=lambda c: ranked_set.get(c["id"], 10_000))

        # Step 2: ask ollama "should_notify" on the top slice
        signals = {
            "weather": {"condition": weather.condition, "temperature_c": weather.temperature_c},
            "time": {"iso": time.now.isoformat(), "daypart": time.daypart, "is_weekend": time.is_weekend, "hour": time.hour},
            "location": {"lat": location.lat, "lon": location.lon},
            "demand": demand.demand_by_business_id,
            "events": events.events,
        }

        prompt = build_should_notify_prompt(
            user_id=user.id,
            user_preferences=interests,
            exploration_preference=user.exploration_preference,
            signals=signals,
            candidates=candidates,
        )

        # Reuse the same Ollama client through AiRanker
        result = await ranker._ollama.generate_json(prompt, temperature=0.2)  # noqa: SLF001 (local app)
        decision = result.parsed_json or {}
        should_notify = bool(decision.get("should_notify"))
        candidate_id = str(decision.get("candidate_id") or "")
        if not should_notify or not candidate_id:
            continue

        chosen = next((c for c in candidates if c["id"] == candidate_id), None)
        if not chosen:
            # fallback to best ranked
            chosen = candidates[0]

        tpl = next((t for t in templates if t.id == chosen["template_id"]), None)
        biz = next((b for b, _ in businesses_with_distance if b.id == chosen["business_id"]), None)
        if not tpl or not biz:
            continue

        # Create a real claimed coupon instance so it can appear in Home/claimed rewards.
        inst = create_coupon_instance(
            db,
            user=user,
            template=tpl,
            business=biz,
            discount_percent=int(chosen.get("base_discount") or tpl.base_discount),
            day_key=day_key,
        )

        notif = Notification(
            id=f"notif_{uuid.uuid4().hex}",
            user_id=user.id,
            coupon_instance_id=inst.id,
            title=str(decision.get("title") or f"{tpl.title}"),
            body=str(decision.get("body") or f"{tpl.base_discount}% OFF at {biz.name}"),
            status="pending",
            created_at=now,
        )
        db.add(notif)
        db.commit()
        created += 1

    return created


async def schedule_login_notification(
    *,
    session_factory,
    user_id: str,
    delay_seconds: int = 60,
    lat: float = 48.137154,
    lon: float = 11.576124,
) -> None:
    """
    Hackathon UX: emit a notification ~1 minute after login.
    Best-effort: if anything fails, we just skip.
    """
    await asyncio.sleep(max(1, int(delay_seconds)))
    try:
        with session_factory() as db:
            user = db.get(User, user_id)
            if not user:
                return

            now = datetime.utcnow()
            # Avoid spamming: if a notification was created in the last 10 minutes, skip.
            recent = (
                db.query(Notification)
                .filter(Notification.user_id == user_id)
                .filter(Notification.created_at >= datetime(now.year, now.month, now.day))
                .order_by(Notification.created_at.desc())
                .first()
            )
            if recent and (now - recent.created_at).total_seconds() < 10 * 60:
                return

            # Build candidates and create ONE catchy notification unconditionally.
            day_key = _today_key(now)
            candidates, templates, businesses_with_distance = _build_candidates(db, lat=lat, lon=lon)
            if not candidates:
                return

            business_ids = [b.id for b, _ in businesses_with_distance]
            weather = await get_weather_signal(lat=lat, lon=lon, now=now)
            demand = get_demand_signal(db, business_ids, now)
            events = get_events_signal(now)
            time = TimeSignal(now=now)
            location = LocationSignal(lat=lat, lon=lon)

            ranker = AiRanker()
            ranked_ids, _, _ = await ranker.rank_home_feed(
                user_id=user.id,
                user_preferences=[s.strip() for s in (user.interests_csv or "").split(",") if s.strip()],
                exploration_preference=user.exploration_preference,
                weather=weather,
                time=time,
                location=location,
                demand=demand,
                events=events,
                candidates=candidates,
            )
            ranked_set = {cid: i for i, cid in enumerate(ranked_ids)}
            candidates.sort(key=lambda c: ranked_set.get(c["id"], 10_000))
            chosen = candidates[0]

            tpl = next((t for t in templates if t.id == chosen["template_id"]), None)
            biz = next((b for b, _ in businesses_with_distance if b.id == chosen["business_id"]), None)
            if not tpl or not biz:
                return

            inst = create_coupon_instance(
                db,
                user=user,
                template=tpl,
                business=biz,
                discount_percent=int(chosen.get("base_discount") or tpl.base_discount),
                day_key=day_key,
            )

            title = f"Perfect timing at {biz.name}"
            if weather.temperature_c >= 24 and tpl.category == "coffee":
                title = "It’s hot — iced coffee time!"
            elif weather.temperature_c <= 12 and tpl.category == "coffee":
                title = "Chilly out — warm coffee break?"
            elif weather.condition in ("Rainy", "Stormy", "Snowy") and tpl.category in ("coffee", "food"):
                title = "Weather’s rough — cozy deal nearby"

            body = f"{inst.discount_percent}% OFF right now at {biz.name} - {chosen.get('distance_km', '?')} km away."

            notif = Notification(
                id=f"notif_{uuid.uuid4().hex}",
                user_id=user.id,
                coupon_instance_id=inst.id,
                title=title,
                body=body,
                status="pending",
                created_at=now,
            )
            db.add(notif)
            db.commit()
    except Exception:
        return


async def run_notification_loop(*, session_factory, tick_seconds: int, enabled: bool) -> None:
    if not enabled:
        return
    while True:
        try:
            with session_factory() as db:
                await tick_generate_notifications(db)
        except Exception:
            # swallow to keep loop alive (local dev)
            pass
        await asyncio.sleep(max(5, int(tick_seconds)))


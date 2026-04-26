from __future__ import annotations

from datetime import datetime

from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session

from app.deps import get_current_user_id, get_db, require_user
from app.models import CouponTemplate
from app.schemas import AiDebugResponse
from app.services.ai_ranker import AiRanker, build_rank_prompt
from app.services.offers_service import list_businesses_with_distance
from app.services.simulated_signals import get_demand_signal, get_events_signal, get_weather_signal
from app.services.signals import LocationSignal, TimeSignal

router = APIRouter(prefix="/ai", tags=["ai"])


@router.get("/debug/rank-home", response_model=AiDebugResponse)
async def debug_rank_home(
    db: Session = Depends(get_db),
    user_id: str = Depends(get_current_user_id),
    lat: float = Query(default=48.137154),
    lon: float = Query(default=11.576124),
) -> AiDebugResponse:
    now = datetime.utcnow()
    user = require_user(db, user_id)
    interests = [s.strip() for s in user.interests_csv.split(",") if s.strip()]

    templates = db.query(CouponTemplate).all()
    businesses_with_distance = list_businesses_with_distance(db, user_lat=lat, user_lon=lon)
    business_ids = [b.id for b, _ in businesses_with_distance]

    weather = get_weather_signal(now)
    demand = get_demand_signal(business_ids, now)
    events = get_events_signal(now)
    time = TimeSignal(now=now)
    location = LocationSignal(lat=lat, lon=lon)

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

    prompt = build_rank_prompt(
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

    ranker = AiRanker()
    ranked_ids, raw_text, parsed = await ranker.rank_home_feed(
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

    return AiDebugResponse(prompt=prompt, raw_model_response=raw_text, parsed_json=parsed or {"ranked_ids": ranked_ids})


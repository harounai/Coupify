from __future__ import annotations

from datetime import datetime

from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session

from app.deps import get_current_user_id, get_db, require_user
from app.models import Business, CouponInstance, CouponTemplate, User
from app.models import MerchantRule, MerchantStatsDaily
from app.schemas import (
    BusinessOut,
    CouponInstanceOut,
    CouponTemplateOut,
    HomeFeedResponse,
    StreakState,
    UserProfile,
)
from app.services.ai_ranker import AiRanker
from app.services.context_signals import get_demand_signal, get_events_signal, get_weather_signal
from app.services.offers_service import list_businesses_with_distance, pick_new_in_town
from app.services.signals import DemandSignal, EventsSignal, LocationSignal, TimeSignal
import uuid

router = APIRouter(prefix="/home", tags=["home"])


def _today_key(now: datetime) -> str:
    return now.strftime("%Y-%m-%d")


def _tpl_out(tpl: CouponTemplate) -> CouponTemplateOut:
    return CouponTemplateOut(
        id=tpl.id,
        title=tpl.title,
        category=tpl.category,
        base_discount=tpl.base_discount,
        duration_hours=tpl.duration_hours,
    )


def _catchy_title(*, category: str, weather_condition: str, temperature_c: int, daypart: str, demand_level: int) -> str:
    cat = (category or "").lower()
    cond = (weather_condition or "").lower()

    hot = temperature_c >= 24
    cold = temperature_c <= 12
    rainy = cond in ("rainy", "stormy", "snowy")
    quiet = demand_level <= 35

    if cat == "coffee":
        if hot:
            return "It’s pretty hot — let’s grab an iced coffee!"
        if cold or rainy:
            return "It’s chilly out — warm up with a coffee break."
        if quiet:
            return "Perfect timing — no queue right now. Coffee?"
        if daypart == "morning":
            return "Morning boost? Coffee is calling."
        return "Coffee break unlocked."

    if cat == "food":
        if daypart in ("afternoon", "evening"):
            return "Hungry? This is your sign to grab a bite."
        if rainy:
            return "Rainy day comfort food? Let’s go."
        if quiet:
            return "Quiet right now — best time for lunch."
        return "Take a tasty detour."

    if cat == "fitness":
        if rainy:
            return "Rain outside — perfect indoor workout moment."
        if daypart == "evening":
            return "After-work reset? Quick workout?"
        return "Move your body — you’ll thank yourself."

    # fallback
    if quiet:
        return "Perfect timing — it’s quiet nearby."
    return "A great local pick for right now."


def _biz_out(b: Business, *, distance_km: float | None = None, demand_level: int | None = None) -> BusinessOut:
    return BusinessOut(
        id=b.id,
        name=b.name,
        category=b.category,
        lat=b.lat,
        lon=b.lon,
        image_url=b.image_url,
        distance_km=distance_km,
        demand_level=demand_level,
    )


def _inst_out(inst: CouponInstance, *, distance_km: float | None = None, demand_level: int | None = None) -> CouponInstanceOut:
    return CouponInstanceOut(
        id=inst.id,
        status=inst.status,  # type: ignore[arg-type]
        discount_percent=inst.discount_percent,
        created_at=inst.created_at,
        expires_at=inst.expires_at,
        redeemed_at=inst.redeemed_at,
        day_key=inst.day_key,
        business=_biz_out(inst.business, distance_km=distance_km, demand_level=demand_level),
        template=_tpl_out(inst.template),
    )


@router.get("", response_model=HomeFeedResponse)
async def get_home_feed(
    db: Session = Depends(get_db),
    user_id: str = Depends(get_current_user_id),
    lat: float = Query(default=48.137154),
    lon: float = Query(default=11.576124),
    debug_ai: bool = Query(default=False),
) -> HomeFeedResponse:
    now = datetime.utcnow()
    day_key = _today_key(now)

    user: User = require_user(db, user_id)
    interests = [s.strip() for s in user.interests_csv.split(",") if s.strip()]

    streak = user.streak
    streak_state = StreakState(
        current_days=streak.current_days if streak else 0,
        best_days=streak.best_days if streak else 0,
        last_checkin_date=streak.last_checkin_date if streak else None,
        reward_unlocked_7=(streak.current_days if streak else 0) >= 7,
        reward_unlocked_30=(streak.current_days if streak else 0) >= 30,
    )

    # Candidate "live opportunities" are (template x business) pairs. We'll let the AI rank them,
    # and materialize the top as a claimed coupon if the user taps claim.
    templates = db.query(CouponTemplate).all()
    businesses_with_distance = list_businesses_with_distance(db, user_lat=lat, user_lon=lon)
    business_ids = [b.id for b, _ in businesses_with_distance]

    weather = await get_weather_signal(lat=lat, lon=lon, now=now)
    demand: DemandSignal = get_demand_signal(db, business_ids, now)
    events: EventsSignal = get_events_signal(now)
    time = TimeSignal(now=now)
    location = LocationSignal(lat=lat, lon=lon)

    candidates: list[dict] = []
    for tpl in templates:
        # Take closest business in matching category (fallback closest overall)
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

    # Apply merchant rules + context to generate a dynamic discount (living offer).
    # Offers don't exist as DB rows until claimed; we generate parameters per request.
    rules_by_business_id = {
        r.business_id: r for r in db.query(MerchantRule).filter(MerchantRule.business_id.in_(business_ids)).all()
    }

    # Filter candidates by merchant "products" (simple: match template category)
    filtered: list[dict] = []
    for c in candidates:
        rule = rules_by_business_id.get(c["business_id"])
        if not rule or not rule.products_csv:
            filtered.append(c)
            continue
        products = {p.strip().lower() for p in (rule.products_csv or "").split(",") if p.strip()}
        if not products:
            filtered.append(c)
            continue
        if str(c.get("category") or "").lower() in products:
            filtered.append(c)
    candidates = filtered
    for c in candidates:
        rule = rules_by_business_id.get(c["business_id"])
        min_d = int(rule.min_discount_percent) if rule else 5
        max_d = int(rule.max_discount_percent) if rule else 20
        demand_level = int(c.get("demand_level") or 50)
        # quieter places get higher discounts (demand proxy)
        quiet_boost = int(round((50 - demand_level) / 10.0))  # -5..+5-ish
        # weather boosts: cold/rainy -> coffee/food
        weather_boost = 0
        if weather.condition in ("Rainy", "Stormy", "Snowy") and c.get("category") in ("coffee", "food"):
            weather_boost += 2
        if weather.temperature_c <= 12 and c.get("category") == "coffee":
            weather_boost += 2
        # time boosts
        time_boost = 0
        if time.daypart == "morning" and c.get("category") == "coffee":
            time_boost += 2
        if time.daypart in ("afternoon", "evening") and c.get("category") == "food":
            time_boost += 2

        base = int(c.get("base_discount") or 10)
        dynamic = base + quiet_boost + weather_boost + time_boost
        dynamic = max(min_d, min(max_d, dynamic))
        c["dynamic_discount"] = int(dynamic)

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
    ranked_set = {cid: i for i, cid in enumerate(ranked_ids)}
    candidates.sort(key=lambda c: ranked_set.get(c["id"], 10_000))

    # Offer of the day = top candidate (not yet claimed)
    offer_of_day_candidate = candidates[0] if candidates else None

    # Claimed rewards stay visible until end of day: claimed today & not expired.
    claimed_today = (
        db.query(CouponInstance)
        .filter(CouponInstance.user_id == user.id)
        .filter(CouponInstance.day_key == day_key)
        .filter(CouponInstance.status == "claimed")
        .order_by(CouponInstance.created_at.desc())
        .all()
    )

    # Live opportunities: represent top-N candidates as "virtual coupons" for UI.
    # We serialize as CouponInstanceOut-like objects with synthetic IDs prefixed "virtual_".
    live: list[CouponInstanceOut] = []
    for c in candidates[:5]:
        tpl = next(t for t in templates if t.id == c["template_id"])
        biz = next(b for b, _ in businesses_with_distance if b.id == c["business_id"])
        dist = next(d for b, d in businesses_with_distance if b.id == biz.id)
        catchy = _catchy_title(
            category=tpl.category,
            weather_condition=weather.condition,
            temperature_c=weather.temperature_c,
            daypart=time.daypart,
            demand_level=int(c.get("demand_level") or 50),
        )
        live.append(
            CouponInstanceOut(
                id=f"virtual_{c['id']}",
                status="claimed",  # UI treats these as actionable items; claim endpoint materializes real instance
                discount_percent=int(c.get("dynamic_discount") or c["base_discount"]),
                created_at=now,
                expires_at=now,
                redeemed_at=None,
                day_key=day_key,
                business=_biz_out(biz, distance_km=dist, demand_level=c.get("demand_level")),
                template=CouponTemplateOut(
                    id=tpl.id,
                    title=catchy,
                    category=tpl.category,
                    base_discount=tpl.base_discount,
                    duration_hours=tpl.duration_hours,
                ),
            )
        )

    # New in town
    new_biz = pick_new_in_town(db, user_lat=lat, user_lon=lon)
    new_in_town = [
        _biz_out(b, distance_km=d, demand_level=demand.demand_by_business_id.get(b.id))
        for (b, d) in new_biz
    ]

    offer_of_day = None
    if offer_of_day_candidate:
        tpl = next(t for t in templates if t.id == offer_of_day_candidate["template_id"])
        biz = next(b for b, _ in businesses_with_distance if b.id == offer_of_day_candidate["business_id"])
        dist = next(d for b, d in businesses_with_distance if b.id == biz.id)
        catchy = _catchy_title(
            category=tpl.category,
            weather_condition=weather.condition,
            temperature_c=weather.temperature_c,
            daypart=time.daypart,
            demand_level=int(offer_of_day_candidate.get("demand_level") or 50),
        )
        offer_of_day = CouponInstanceOut(
            id=f"virtual_{offer_of_day_candidate['id']}",
            status="claimed",
            discount_percent=int(offer_of_day_candidate.get("dynamic_discount") or offer_of_day_candidate["base_discount"]),
            created_at=now,
            expires_at=now,
            redeemed_at=None,
            day_key=day_key,
            business=_biz_out(biz, distance_km=dist, demand_level=offer_of_day_candidate.get("demand_level")),
            template=CouponTemplateOut(
                id=tpl.id,
                title=catchy,
                category=tpl.category,
                base_discount=tpl.base_discount,
                duration_hours=tpl.duration_hours,
            ),
        )

    # Merchant stats: count one impression per business shown in the live list.
    try:
        shown_business_ids = {c["business_id"] for c in candidates[:5]}
        for bid in shown_business_ids:
            row = (
                db.query(MerchantStatsDaily)
                .filter(MerchantStatsDaily.business_id == bid, MerchantStatsDaily.day_key == day_key)
                .one_or_none()
            )
            if not row:
                row = MerchantStatsDaily(id=str(uuid.uuid4()), business_id=bid, day_key=day_key, impressions=0)
                db.add(row)
            row.impressions = int(row.impressions or 0) + 1
        db.commit()
    except Exception:
        db.rollback()

    # Convert claimed instances
    claimed_out: list[CouponInstanceOut] = []
    for inst in claimed_today:
        dist = None
        demand_level = None
        for b, d in businesses_with_distance:
            if b.id == inst.business_id:
                dist = d
                break
        demand_level = demand.demand_by_business_id.get(inst.business_id)
        claimed_out.append(_inst_out(inst, distance_km=dist, demand_level=demand_level))

    return HomeFeedResponse(
        user=UserProfile(
            id=user.id,
            display_name=user.display_name,
            interests=interests,
            exploration_preference=user.exploration_preference,
        ),
        streak=streak_state,
        live_opportunities=live,
        claimed_rewards_today=claimed_out,
        offer_of_the_day=offer_of_day,
        new_in_town=new_in_town,
    )


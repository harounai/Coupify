from __future__ import annotations

import uuid
from datetime import datetime, timedelta

from sqlalchemy.orm import Session

from app.models import Business, CouponInstance, CouponTemplate, User
from app.services.utils_geo import haversine_km


def list_businesses_with_distance(
    db: Session, *, user_lat: float, user_lon: float, limit: int | None = None
) -> list[tuple[Business, float]]:
    businesses = db.query(Business).all()
    items = [(b, haversine_km(user_lat, user_lon, b.lat, b.lon)) for b in businesses]
    items.sort(key=lambda x: x[1])
    return items[:limit] if limit else items


def pick_new_in_town(db: Session, *, user_lat: float, user_lon: float) -> list[tuple[Business, float]]:
    # Simple: "new" == farthest 3 within a cap, or last 3 inserted; offline-friendly.
    businesses = db.query(Business).all()
    items = [(b, haversine_km(user_lat, user_lon, b.lat, b.lon)) for b in businesses]
    items.sort(key=lambda x: x[1], reverse=True)
    return items[:3]


def get_templates(db: Session) -> list[CouponTemplate]:
    return db.query(CouponTemplate).all()


def create_coupon_instance(
    db: Session,
    *,
    user: User,
    template: CouponTemplate,
    business: Business,
    discount_percent: int,
    day_key: str,
) -> CouponInstance:
    now = datetime.utcnow()
    inst = CouponInstance(
        id=f"cinst_{uuid.uuid4().hex}",
        user_id=user.id,
        template_id=template.id,
        business_id=business.id,
        status="claimed",
        discount_percent=discount_percent,
        created_at=now,
        expires_at=now + timedelta(hours=template.duration_hours),
        redeemed_at=None,
        day_key=day_key,
    )
    db.add(inst)
    db.commit()
    db.refresh(inst)
    return inst


def redeem_coupon(db: Session, *, coupon_id: str, user_id: str) -> CouponInstance:
    inst = db.get(CouponInstance, coupon_id)
    if not inst or inst.user_id != user_id:
        raise ValueError("Coupon not found")
    if inst.status == "redeemed":
        return inst
    inst.status = "redeemed"
    inst.redeemed_at = datetime.utcnow()
    db.add(inst)
    db.commit()
    db.refresh(inst)
    return inst


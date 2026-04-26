from __future__ import annotations

from datetime import datetime

from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy.orm import Session

from app.deps import get_current_user_id, get_db, require_user
from app.models import Business, CouponInstance, CouponTemplate
from app.schemas import ClaimCouponRequest, CouponInstanceOut, CouponTemplateOut, RedeemCouponResponse
from app.services.offers_service import create_coupon_instance, redeem_coupon

router = APIRouter(prefix="/coupons", tags=["coupons"])


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


def _biz_out(b: Business) -> dict:
    return {"id": b.id, "name": b.name, "category": b.category, "lat": b.lat, "lon": b.lon, "image_url": b.image_url}


def _inst_out(inst: CouponInstance) -> CouponInstanceOut:
    # Keep distance/demand optional; computed on home feed.
    return CouponInstanceOut(
        id=inst.id,
        status=inst.status,  # type: ignore[arg-type]
        discount_percent=inst.discount_percent,
        created_at=inst.created_at,
        expires_at=inst.expires_at,
        redeemed_at=inst.redeemed_at,
        day_key=inst.day_key,
        business={**_biz_out(inst.business)},  # type: ignore[arg-type]
        template=_tpl_out(inst.template),
    )


@router.get("/templates", response_model=list[CouponTemplateOut])
def list_templates(db: Session = Depends(get_db)) -> list[CouponTemplateOut]:
    return [_tpl_out(t) for t in db.query(CouponTemplate).all()]


@router.get("/my", response_model=list[CouponInstanceOut])
def my_coupons(
    db: Session = Depends(get_db),
    user_id: str = Depends(get_current_user_id),
    day_key: str | None = Query(default=None),
) -> list[CouponInstanceOut]:
    q = db.query(CouponInstance).filter(CouponInstance.user_id == user_id).order_by(CouponInstance.created_at.desc())
    if day_key:
        q = q.filter(CouponInstance.day_key == day_key)
    return [_inst_out(i) for i in q.all()]


@router.post("/claim", response_model=CouponInstanceOut)
def claim_coupon(
    payload: ClaimCouponRequest,
    db: Session = Depends(get_db),
    user_id: str = Depends(get_current_user_id),
) -> CouponInstanceOut:
    user = require_user(db, user_id)
    tpl = db.get(CouponTemplate, payload.template_id)
    biz = db.get(Business, payload.business_id)
    if not tpl or not biz:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Template or business not found")

    now = datetime.utcnow()
    day_key = _today_key(now)

    # Idempotency: one claim per (user, template, day). If it already exists, return it.
    existing = (
        db.query(CouponInstance)
        .filter(CouponInstance.user_id == user.id)
        .filter(CouponInstance.template_id == tpl.id)
        .filter(CouponInstance.day_key == day_key)
        .first()
    )
    if existing:
        return _inst_out(existing)

    # For now, discount = base_discount; AI can later send an adjusted value.
    inst = create_coupon_instance(
        db,
        user=user,
        template=tpl,
        business=biz,
        discount_percent=tpl.base_discount,
        day_key=day_key,
    )
    return _inst_out(inst)


@router.post("/{coupon_id}/redeem", response_model=RedeemCouponResponse)
def redeem(
    coupon_id: str,
    db: Session = Depends(get_db),
    user_id: str = Depends(get_current_user_id),
) -> RedeemCouponResponse:
    try:
        inst = redeem_coupon(db, coupon_id=coupon_id, user_id=user_id)
    except ValueError:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Coupon not found")
    return RedeemCouponResponse(coupon=_inst_out(inst))


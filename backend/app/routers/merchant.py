from __future__ import annotations

import json
from datetime import datetime, timedelta
from uuid import uuid4

from fastapi import APIRouter, HTTPException
from sqlalchemy.orm import Session

from app.db import engine
from app.models import Business, DemandOverride, MerchantRule, MerchantStatsDaily
from app.schemas import MerchantBusinessOut, MerchantRuleIn, MerchantRuleOut, MerchantStatsOut

router = APIRouter(tags=["merchant"])


def _day_key(dt: datetime) -> str:
    return dt.strftime("%Y-%m-%d")


@router.get("/merchant/businesses", response_model=list[MerchantBusinessOut])
def list_merchant_businesses() -> list[MerchantBusinessOut]:
    with Session(engine) as db:
        businesses = db.query(Business).order_by(Business.name.asc()).all()
        return [
            MerchantBusinessOut(
                id=b.id,
                name=b.name,
                category=b.category,
                lat=b.lat,
                lon=b.lon,
                image_url=b.image_url,
            )
            for b in businesses
        ]


@router.get("/merchant/{business_id}/rules", response_model=MerchantRuleOut)
def get_rules(business_id: str) -> MerchantRuleOut:
    with Session(engine) as db:
        b = db.get(Business, business_id)
        if not b:
            raise HTTPException(status_code=404, detail="business_not_found")

        rule = db.get(MerchantRule, business_id)
        if not rule:
            rule = MerchantRule(business_id=business_id)
            db.add(rule)
            db.commit()
            db.refresh(rule)

        return MerchantRuleOut(
            business_id=rule.business_id,
            max_discount_percent=rule.max_discount_percent,
            min_discount_percent=rule.min_discount_percent,
            quiet_hours_start=rule.quiet_hours_start,
            quiet_hours_end=rule.quiet_hours_end,
            goal=rule.goal,
            coupons_per_day=rule.coupons_per_day,
            coupons_total=rule.coupons_total,
            coupons_total_issued=rule.coupons_total_issued,
            products=[p.strip() for p in (rule.products_csv or "").split(",") if p.strip()],
            rules_json=json.loads(rule.rules_json or "{}"),
            updated_at=rule.updated_at,
        )


@router.put("/merchant/{business_id}/rules", response_model=MerchantRuleOut)
def put_rules(business_id: str, body: MerchantRuleIn) -> MerchantRuleOut:
    if body.min_discount_percent > body.max_discount_percent:
        raise HTTPException(status_code=422, detail="min_discount_above_max")

    with Session(engine) as db:
        b = db.get(Business, business_id)
        if not b:
            raise HTTPException(status_code=404, detail="business_not_found")

        rule = db.get(MerchantRule, business_id)
        if not rule:
            rule = MerchantRule(business_id=business_id)
            db.add(rule)

        rule.max_discount_percent = body.max_discount_percent
        rule.min_discount_percent = body.min_discount_percent
        rule.quiet_hours_start = body.quiet_hours_start
        rule.quiet_hours_end = body.quiet_hours_end
        rule.goal = body.goal
        rule.coupons_per_day = body.coupons_per_day
        rule.coupons_total = body.coupons_total
        rule.products_csv = ",".join([p.strip() for p in (body.products or []) if p.strip()])
        rule.rules_json = json.dumps(body.rules_json or {})
        rule.updated_at = datetime.utcnow()

        db.commit()
        db.refresh(rule)

        return MerchantRuleOut(
            business_id=rule.business_id,
            max_discount_percent=rule.max_discount_percent,
            min_discount_percent=rule.min_discount_percent,
            quiet_hours_start=rule.quiet_hours_start,
            quiet_hours_end=rule.quiet_hours_end,
            goal=rule.goal,
            coupons_per_day=rule.coupons_per_day,
            coupons_total=rule.coupons_total,
            coupons_total_issued=rule.coupons_total_issued,
            products=[p.strip() for p in (rule.products_csv or "").split(",") if p.strip()],
            rules_json=json.loads(rule.rules_json or "{}"),
            updated_at=rule.updated_at,
        )


@router.get("/merchant/{business_id}/stats", response_model=MerchantStatsOut)
def get_stats(business_id: str, day_key: str | None = None) -> MerchantStatsOut:
    with Session(engine) as db:
        b = db.get(Business, business_id)
        if not b:
            raise HTTPException(status_code=404, detail="business_not_found")

        dk = day_key or _day_key(datetime.utcnow())
        row = (
            db.query(MerchantStatsDaily)
            .filter(MerchantStatsDaily.business_id == business_id, MerchantStatsDaily.day_key == dk)
            .one_or_none()
        )
        if not row:
            row = MerchantStatsDaily(id=str(uuid4()), business_id=business_id, day_key=dk)
            db.add(row)
            db.commit()
            db.refresh(row)

        return MerchantStatsOut(
            business_id=row.business_id,
            day_key=row.day_key,
            impressions=row.impressions,
            accepts=row.accepts,
            declines=row.declines,
            redemptions=row.redemptions,
        )


@router.post("/merchant/{business_id}/simulate/low-demand", response_model=dict)
def simulate_low_demand(business_id: str, minutes: int = 60, demand_level: int = 20) -> dict:
    minutes = max(5, min(24 * 60, minutes))
    demand_level = max(0, min(100, demand_level))

    with Session(engine) as db:
        b = db.get(Business, business_id)
        if not b:
            raise HTTPException(status_code=404, detail="business_not_found")

        ov = db.get(DemandOverride, business_id)
        if not ov:
            ov = DemandOverride(
                business_id=business_id,
                demand_level=demand_level,
                expires_at=datetime.utcnow() + timedelta(minutes=minutes),
            )
            db.add(ov)
        else:
            ov.demand_level = demand_level
            ov.expires_at = datetime.utcnow() + timedelta(minutes=minutes)
            ov.updated_at = datetime.utcnow()

        db.commit()

        return {
            "business_id": business_id,
            "demand_level": demand_level,
            "expires_at": ov.expires_at.isoformat(),
        }


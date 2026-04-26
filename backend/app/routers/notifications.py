from __future__ import annotations

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from app.deps import get_current_user_id, get_db
from app.models import Notification

router = APIRouter(prefix="/notifications", tags=["notifications"])


@router.get("/inbox")
def inbox(
    db: Session = Depends(get_db),
    user_id: str = Depends(get_current_user_id),
) -> list[dict]:
    items = (
        db.query(Notification)
        .filter(Notification.user_id == user_id)
        .order_by(Notification.created_at.desc())
        .limit(50)
        .all()
    )
    return [
        {
            "id": n.id,
            "coupon_instance_id": n.coupon_instance_id,
            "business_name": n.coupon.business.name if n.coupon and n.coupon.business else "",
            "image_url": n.coupon.business.image_url if n.coupon and n.coupon.business else "",
            "discount_percent": int(n.coupon.discount_percent) if n.coupon else 0,
            "title": n.title,
            "body": n.body,
            "status": n.status,
            "created_at": n.created_at.isoformat(),
        }
        for n in items
    ]


@router.post("/{notification_id}/accept")
def accept(
    notification_id: str,
    db: Session = Depends(get_db),
    user_id: str = Depends(get_current_user_id),
) -> dict:
    n = db.get(Notification, notification_id)
    if not n or n.user_id != user_id:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Notification not found")
    n.status = "accepted"
    db.add(n)
    db.commit()
    return {"status": "ok"}


@router.post("/{notification_id}/decline")
def decline(
    notification_id: str,
    db: Session = Depends(get_db),
    user_id: str = Depends(get_current_user_id),
) -> dict:
    n = db.get(Notification, notification_id)
    if not n or n.user_id != user_id:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Notification not found")
    n.status = "declined"
    db.add(n)
    db.commit()
    return {"status": "ok"}


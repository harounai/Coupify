from __future__ import annotations

from datetime import datetime, timedelta

from dateutil.parser import isoparse
from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session

from app.deps import get_current_user_id, get_db
from app.models import Streak
from app.schemas import StreakCheckinResponse, StreakState

router = APIRouter(prefix="/streaks", tags=["streaks"])


def _today_key(now: datetime) -> str:
    return now.strftime("%Y-%m-%d")


def _yesterday_key(now: datetime) -> str:
    return (now - timedelta(days=1)).strftime("%Y-%m-%d")


def _state(streak: Streak) -> StreakState:
    return StreakState(
        current_days=streak.current_days,
        best_days=streak.best_days,
        last_checkin_date=streak.last_checkin_date,
        reward_unlocked_7=streak.current_days >= 7,
        reward_unlocked_30=streak.current_days >= 30,
    )


@router.get("/me", response_model=StreakState)
def get_my_streak(
    db: Session = Depends(get_db),
    user_id: str = Depends(get_current_user_id),
) -> StreakState:
    streak = db.get(Streak, user_id)
    if not streak:
        streak = Streak(user_id=user_id, current_days=0, best_days=0, last_checkin_date=None)
        db.add(streak)
        db.commit()
        db.refresh(streak)
    return _state(streak)


@router.post("/checkin", response_model=StreakCheckinResponse)
def checkin(
    db: Session = Depends(get_db),
    user_id: str = Depends(get_current_user_id),
    # For deterministic testing from Android, allow overriding "now".
    now_iso: str | None = Query(default=None),
) -> StreakCheckinResponse:
    now = isoparse(now_iso) if now_iso else datetime.utcnow()
    today = _today_key(now)
    yesterday = _yesterday_key(now)

    streak = db.get(Streak, user_id)
    if not streak:
        streak = Streak(user_id=user_id, current_days=0, best_days=0, last_checkin_date=None)
        db.add(streak)
        db.commit()
        db.refresh(streak)

    if streak.last_checkin_date == today:
        return StreakCheckinResponse(streak=_state(streak), checked_in_today=True)

    if streak.last_checkin_date == yesterday:
        streak.current_days += 1
    else:
        streak.current_days = 1

    streak.best_days = max(streak.best_days, streak.current_days)
    streak.last_checkin_date = today

    db.add(streak)
    db.commit()
    db.refresh(streak)
    return StreakCheckinResponse(streak=_state(streak), checked_in_today=True)


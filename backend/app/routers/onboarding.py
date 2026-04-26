from __future__ import annotations

import json

from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.deps import get_current_user_id, get_db, require_user
from app.schemas import OnboardingStatus, OnboardingSurvey

router = APIRouter(prefix="/onboarding", tags=["onboarding"])


@router.get("/status", response_model=OnboardingStatus)
def get_status(
    db: Session = Depends(get_db),
    user_id: str = Depends(get_current_user_id),
) -> OnboardingStatus:
    user = require_user(db, user_id)
    try:
        raw = json.loads(user.survey_json or "{}")
    except Exception:
        raw = {}
    if not raw:
        return OnboardingStatus(completed=False, survey=None)
    return OnboardingStatus(completed=True, survey=OnboardingSurvey(**raw))


@router.put("/survey", response_model=OnboardingStatus)
def put_survey(
    payload: OnboardingSurvey,
    db: Session = Depends(get_db),
    user_id: str = Depends(get_current_user_id),
) -> OnboardingStatus:
    user = require_user(db, user_id)
    user.survey_json = payload.model_dump_json()
    # Also mirror key fields into profile fields used elsewhere
    user.interests_csv = ",".join(payload.interests)
    user.exploration_preference = payload.exploration_behavior
    db.add(user)
    db.commit()
    db.refresh(user)
    return OnboardingStatus(completed=True, survey=payload)


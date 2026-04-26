from __future__ import annotations

import json
from datetime import datetime

from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.deps import get_current_user_id, get_db, require_user
from app.models import UserPreferences
from app.schemas import UserPreferencesIn, UserProfile

router = APIRouter(prefix="/user", tags=["preferences"])


@router.get("/preferences", response_model=UserPreferencesIn)
def get_preferences(
    db: Session = Depends(get_db),
    user_id: str = Depends(get_current_user_id),
) -> UserPreferencesIn:
    require_user(db, user_id)
    prefs = db.get(UserPreferences, user_id)
    if not prefs:
        return UserPreferencesIn()
    raw = {}
    try:
        raw = json.loads(prefs.preferences_json or "{}")
    except Exception:
        raw = {}
    return UserPreferencesIn(**raw)


@router.put("/preferences", response_model=UserProfile)
def put_preferences(
    payload: UserPreferencesIn,
    db: Session = Depends(get_db),
    user_id: str = Depends(get_current_user_id),
) -> UserProfile:
    user = require_user(db, user_id)

    prefs = db.get(UserPreferences, user_id) or UserPreferences(user_id=user_id)
    prefs.interests_csv = ",".join(payload.interests)
    prefs.budget_range = payload.budget_range
    prefs.time_preferences_csv = ",".join(payload.active_times)
    prefs.behavior_frequency = payload.behavior_frequency
    prefs.spontaneous = payload.is_spontaneous
    prefs.location_habits_csv = ",".join(payload.location_habits)
    prefs.environment_preference = payload.environment_preference
    prefs.discovery_mode = payload.discovery_mode
    prefs.preferences_json = payload.model_dump_json()
    prefs.updated_at = datetime.utcnow()

    # Mirror into user for quick reads
    user.interests_csv = prefs.interests_csv
    user.has_completed_onboarding = True
    # exploration_preference is kept for legacy features, map novelty+discovery into 0..100.
    user.exploration_preference = int(max(0, min(100, round((payload.novelty_seeking + (100 if payload.discovery_mode == "HIDDEN_GEMS" else 50)) / 2))))

    db.add(prefs)
    db.add(user)
    db.commit()
    db.refresh(user)

    interests = [s.strip() for s in user.interests_csv.split(",") if s.strip()]
    return UserProfile(
        id=user.id,
        display_name=user.display_name,
        email=user.email,
        interests=interests,
        exploration_preference=user.exploration_preference,
        has_completed_onboarding=True,
    )


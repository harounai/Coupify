from __future__ import annotations

from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.deps import get_current_user_id, get_db, require_user
from app.schemas import UpdateUserProfileRequest, UserProfile

router = APIRouter(prefix="/users", tags=["users"])


@router.get("/me", response_model=UserProfile)
def get_me(
    db: Session = Depends(get_db),
    user_id: str = Depends(get_current_user_id),
) -> UserProfile:
    user = require_user(db, user_id)
    interests = [s.strip() for s in user.interests_csv.split(",") if s.strip()]
    return UserProfile(
        id=user.id,
        display_name=user.display_name,
        email=user.email,
        interests=interests,
        exploration_preference=user.exploration_preference,
        has_completed_onboarding=bool(user.has_completed_onboarding),
    )


@router.patch("/me", response_model=UserProfile)
def update_me(
    payload: UpdateUserProfileRequest,
    db: Session = Depends(get_db),
    user_id: str = Depends(get_current_user_id),
) -> UserProfile:
    user = require_user(db, user_id)
    if payload.display_name is not None:
        user.display_name = payload.display_name
    if payload.interests is not None:
        user.interests_csv = ",".join(payload.interests)
    if payload.exploration_preference is not None:
        user.exploration_preference = payload.exploration_preference
    db.add(user)
    db.commit()
    db.refresh(user)
    interests = [s.strip() for s in user.interests_csv.split(",") if s.strip()]
    return UserProfile(
        id=user.id,
        display_name=user.display_name,
        interests=interests,
        exploration_preference=user.exploration_preference,
    )


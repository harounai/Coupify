from __future__ import annotations

import uuid

from datetime import datetime

from fastapi import APIRouter, Depends, HTTPException, Header, status
from sqlalchemy.orm import Session

from app.deps import get_db
from app.models import AuthSession, Business, Streak, User
from app.schemas import AuthResponse, LoginRequest, SignupRequest, UserProfile
from app.services.security import create_access_token, decode_access_token, hash_password, verify_password
from app.services.ai_notifier import schedule_login_notification
from app.db import SessionLocal
import asyncio
import threading

router = APIRouter(prefix="/auth", tags=["auth"])


def _new_session(db: Session, user_id: str) -> str:
    session_id = f"sess_{uuid.uuid4().hex}"
    db.add(AuthSession(id=session_id, user_id=user_id, revoked_at=None))
    return session_id


@router.post("/register", response_model=AuthResponse)
@router.post("/signup", response_model=AuthResponse, include_in_schema=False)
def register(payload: SignupRequest, db: Session = Depends(get_db)) -> AuthResponse:
    existing = db.query(User).filter(User.email == payload.email.lower()).first()
    if existing:
        raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail="Email already registered")

    role = (payload.role or "USER").upper()
    if role not in ("USER", "COMPANY"):
        raise HTTPException(status_code=status.HTTP_422_UNPROCESSABLE_ENTITY, detail="invalid_role")
    company_business_id = None
    if role == "COMPANY":
        if not payload.business_id:
            raise HTTPException(status_code=status.HTTP_422_UNPROCESSABLE_ENTITY, detail="business_id_required_for_company")
        biz = db.get(Business, payload.business_id)
        if not biz:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="business_not_found")
        company_business_id = biz.id

    user = User(
        id=f"user_{uuid.uuid4().hex}",
        email=payload.email.lower(),
        password_hash=hash_password(payload.password),
        display_name=payload.display_name,
        role=role,
        company_business_id=company_business_id,
        interests_csv="",
        exploration_preference=50,
        survey_json="{}",
    )
    db.add(user)
    db.add(Streak(user_id=user.id, current_days=0, best_days=0, last_checkin_date=None))
    session_id = _new_session(db, user.id)
    db.commit()
    db.refresh(user)

    token = create_access_token(user_id=user.id, session_id=session_id)
    # Schedule a notification ~1 minute after signup/login.
    # This endpoint is sync (runs in a threadpool), so we start a daemon thread and run an event loop there.
    threading.Thread(
        target=lambda: asyncio.run(schedule_login_notification(session_factory=SessionLocal, user_id=user.id, delay_seconds=60)),
        daemon=True,
    ).start()
    return AuthResponse(
        access_token=token,
        user=UserProfile(
            id=user.id,
            display_name=user.display_name,
            email=user.email,
            role=(user.role or "USER"),  # type: ignore[arg-type]
            business_id=user.company_business_id,
            interests=[],
            exploration_preference=user.exploration_preference,
            has_completed_onboarding=bool(user.has_completed_onboarding),
        ),
    )


@router.post("/login", response_model=AuthResponse)
def login(payload: LoginRequest, db: Session = Depends(get_db)) -> AuthResponse:
    user = db.query(User).filter(User.email == payload.email.lower()).first()
    if not user or not verify_password(payload.password, user.password_hash):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid email or password")

    interests = [s.strip() for s in user.interests_csv.split(",") if s.strip()]
    session_id = _new_session(db, user.id)
    db.commit()
    token = create_access_token(user_id=user.id, session_id=session_id)
    threading.Thread(
        target=lambda: asyncio.run(schedule_login_notification(session_factory=SessionLocal, user_id=user.id, delay_seconds=60)),
        daemon=True,
    ).start()
    return AuthResponse(
        access_token=token,
        user=UserProfile(
            id=user.id,
            display_name=user.display_name,
            email=user.email,
            role=(user.role or "USER"),  # type: ignore[arg-type]
            business_id=user.company_business_id,
            interests=interests,
            exploration_preference=user.exploration_preference,
            has_completed_onboarding=bool(user.has_completed_onboarding),
        ),
    )


@router.get("/me", response_model=UserProfile)
def me(db: Session = Depends(get_db), authorization: str | None = Header(default=None, alias="Authorization")) -> UserProfile:
    if not authorization or not authorization.lower().startswith("bearer "):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Missing bearer token")
    token = authorization.split(" ", 1)[1].strip()
    payload = decode_access_token(token)
    user_id = str(payload.get("sub") or "")
    if not user_id:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid token")
    user = db.get(User, user_id)
    if not user:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="User not found")
    interests = [s.strip() for s in user.interests_csv.split(",") if s.strip()]
    return UserProfile(
        id=user.id,
        display_name=user.display_name,
        email=user.email,
        role=(user.role or "USER"),  # type: ignore[arg-type]
        business_id=user.company_business_id,
        interests=interests,
        exploration_preference=user.exploration_preference,
        has_completed_onboarding=bool(user.has_completed_onboarding),
    )


@router.post("/logout")
def logout(db: Session = Depends(get_db), authorization: str | None = Header(default=None, alias="Authorization")) -> dict:
    if not authorization or not authorization.lower().startswith("bearer "):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Missing bearer token")
    token = authorization.split(" ", 1)[1].strip()
    try:
        payload = decode_access_token(token)
        jti = str(payload.get("jti") or "")
        if not jti:
            raise ValueError("missing jti")
        session = db.get(AuthSession, jti)
        if session and session.revoked_at is None:
            session.revoked_at = datetime.utcnow()
            db.add(session)
            db.commit()
    except Exception:
        # logout should be idempotent
        pass
    return {"status": "ok"}


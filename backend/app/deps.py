from __future__ import annotations

from fastapi import Depends, Header, HTTPException, status
from sqlalchemy.orm import Session

from app.db import get_db
from app.models import AuthSession, User
from app.services.security import decode_access_token


def get_current_user_id(
    db: Session = Depends(get_db),
    authorization: str | None = Header(default=None, alias="Authorization"),
) -> str:
    if not authorization or not authorization.lower().startswith("bearer "):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Missing bearer token")
    token = authorization.split(" ", 1)[1].strip()
    try:
        payload = decode_access_token(token)
        sub = payload.get("sub")
        jti = payload.get("jti")
        if not sub:
            raise ValueError("missing sub")
        if not jti:
            raise ValueError("missing jti")
        session = db.get(AuthSession, str(jti))
        if not session or session.revoked_at is not None:
            raise ValueError("revoked session")
        return str(sub)
    except Exception:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid token")


def require_user(db: Session, user_id: str) -> User:
    user = db.get(User, user_id)
    if not user:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="User not found")
    return user


__all__ = ["get_db", "get_current_user_id", "require_user"]


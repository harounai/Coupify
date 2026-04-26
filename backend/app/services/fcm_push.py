from __future__ import annotations

import asyncio
import logging
from pathlib import Path

from sqlalchemy.orm import Session

from app.core.config import settings
from app.models import UserDeviceToken

logger = logging.getLogger(__name__)


def _load_firebase_app():
    if not settings.fcm_enabled:
        return None
    try:
        import firebase_admin
        from firebase_admin import credentials
    except Exception:
        logger.exception("firebase-admin not available; skipping FCM")
        return None

    try:
        if firebase_admin._apps:  # noqa: SLF001 (firebase-admin internal cache)
            return firebase_admin.get_app()

        cred_path = (settings.fcm_service_account_json or "").strip()
        if cred_path:
            p = Path(cred_path)
            if not p.exists():
                logger.warning("FCM credential file not found: %s", cred_path)
                return None
            return firebase_admin.initialize_app(credentials.Certificate(str(p)))

        # Fallback to ADC for environments configured with GOOGLE_APPLICATION_CREDENTIALS.
        return firebase_admin.initialize_app()
    except Exception:
        logger.exception("Failed to initialize Firebase app; skipping FCM")
        return None


def _to_data_map(payload: dict | None) -> dict[str, str]:
    if not payload:
        return {}
    result: dict[str, str] = {}
    for k, v in payload.items():
        if v is None:
            continue
        result[str(k)] = str(v)
    return result


async def send_push_to_user(
    db: Session,
    *,
    user_id: str,
    title: str,
    body: str,
    data: dict | None = None,
) -> None:
    tokens = [
        row[0]
        for row in db.query(UserDeviceToken.token)
        .filter(UserDeviceToken.user_id == user_id)
        .all()
    ]
    if not tokens:
        return

    app = _load_firebase_app()
    if app is None:
        return

    try:
        from firebase_admin import messaging

        message = messaging.MulticastMessage(
            tokens=tokens,
            notification=messaging.Notification(title=title, body=body),
            data=_to_data_map(data),
        )

        response = await asyncio.to_thread(messaging.send_multicast, message, app=app)

        invalid_tokens: list[str] = []
        for token, item in zip(tokens, response.responses):
            if item.success:
                continue
            err = str(getattr(item, "exception", "")).lower()
            if "registration-token-not-registered" in err or "invalid registration token" in err:
                invalid_tokens.append(token)

        if invalid_tokens:
            (
                db.query(UserDeviceToken)
                .filter(UserDeviceToken.token.in_(invalid_tokens))
                .delete(synchronize_session=False)
            )
            db.commit()
    except Exception:
        logger.exception("Failed to send FCM push")

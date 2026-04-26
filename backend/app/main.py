from __future__ import annotations

from contextlib import asynccontextmanager
import asyncio

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.core.config import settings
from app.db import Base, SessionLocal, engine
from app.routers.coupons import router as coupons_router
from app.routers.ai import router as ai_router
from app.routers.auth import router as auth_router
from app.routers.home import router as home_router
from app.routers.onboarding import router as onboarding_router
from app.routers.preferences import router as preferences_router
from app.routers.notifications import router as notifications_router
from app.routers.streaks import router as streaks_router
from app.routers.users import router as users_router
from app.schemas import HealthResponse
from app.services.seed_data import seed_if_empty
from app.services.ai_notifier import run_notification_loop


@asynccontextmanager
async def lifespan(app: FastAPI):
    Base.metadata.create_all(bind=engine)
    # Seed synchronously; small dataset.
    from sqlalchemy.orm import Session

    with Session(engine) as db:
        seed_if_empty(db)
        db.commit()

    task = asyncio.create_task(
        run_notification_loop(
            session_factory=SessionLocal,
            tick_seconds=settings.ai_notification_tick_seconds,
            enabled=settings.ai_notification_enabled,
            max_per_user_per_day=settings.ai_notification_max_per_user_per_day,
        )
    )
    yield
    task.cancel()


app = FastAPI(title="City Spark Offers API", version="1.0.0", lifespan=lifespan)

# Emulator / Android app needs CORS if you hit from other tooling; harmless locally.
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/health", response_model=HealthResponse)
def health() -> HealthResponse:
    return HealthResponse(status="ok")


app.include_router(users_router, prefix=settings.api_v1_prefix)
app.include_router(streaks_router, prefix=settings.api_v1_prefix)
app.include_router(coupons_router, prefix=settings.api_v1_prefix)
app.include_router(home_router, prefix=settings.api_v1_prefix)
app.include_router(ai_router, prefix=settings.api_v1_prefix)
app.include_router(auth_router, prefix=settings.api_v1_prefix)
app.include_router(onboarding_router, prefix=settings.api_v1_prefix)
app.include_router(preferences_router, prefix=settings.api_v1_prefix)
app.include_router(notifications_router, prefix=settings.api_v1_prefix)

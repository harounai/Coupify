from __future__ import annotations

from datetime import datetime
from typing import Literal

from pydantic import BaseModel, Field


class HealthResponse(BaseModel):
    status: str = "ok"


class SignupRequest(BaseModel):
    email: str = Field(min_length=3)
    password: str = Field(min_length=6)
    display_name: str = Field(min_length=1)


class LoginRequest(BaseModel):
    email: str = Field(min_length=3)
    password: str = Field(min_length=1)


class AuthResponse(BaseModel):
    access_token: str
    token_type: Literal["bearer"] = "bearer"
    user: "UserProfile"


class UserProfile(BaseModel):
    id: str
    display_name: str
    email: str | None = None
    interests: list[str] = Field(default_factory=list)
    exploration_preference: int = Field(ge=0, le=100)
    has_completed_onboarding: bool = False


class UpdateUserProfileRequest(BaseModel):
    display_name: str | None = None
    interests: list[str] | None = None
    exploration_preference: int | None = Field(default=None, ge=0, le=100)


class OnboardingSurvey(BaseModel):
    interests: list[str] = Field(default_factory=list)
    lifestyle: list[str] = Field(default_factory=list)
    budget_sensitivity: int = Field(ge=0, le=100, default=50)
    indoor_outdoor: int = Field(ge=0, le=100, default=50)  # 0 indoor, 100 outdoor
    exploration_behavior: int = Field(ge=0, le=100, default=50)
    time_availability: list[str] = Field(default_factory=list)  # e.g. ["morning","evening","weekend"]


class OnboardingStatus(BaseModel):
    completed: bool
    survey: OnboardingSurvey | None = None


class StreakState(BaseModel):
    current_days: int
    best_days: int
    last_checkin_date: str | None
    reward_unlocked_7: bool
    reward_unlocked_30: bool


class StreakCheckinResponse(BaseModel):
    streak: StreakState
    checked_in_today: bool


class BusinessOut(BaseModel):
    id: str
    name: str
    category: str
    lat: float
    lon: float
    image_url: str
    distance_km: float | None = None
    demand_level: int | None = Field(default=None, ge=0, le=100)


class CouponTemplateOut(BaseModel):
    id: str
    title: str
    category: str
    base_discount: int = Field(ge=0, le=100)
    duration_hours: int = Field(gt=0, le=48)


class CouponInstanceOut(BaseModel):
    id: str
    status: Literal["claimed", "redeemed"]
    discount_percent: int = Field(ge=0, le=100)
    created_at: datetime
    expires_at: datetime
    redeemed_at: datetime | None
    day_key: str
    business: BusinessOut
    template: CouponTemplateOut


class ClaimCouponRequest(BaseModel):
    template_id: str
    business_id: str


class RedeemCouponResponse(BaseModel):
    coupon: CouponInstanceOut


class HomeFeedResponse(BaseModel):
    user: UserProfile
    streak: StreakState
    live_opportunities: list[CouponInstanceOut] = Field(default_factory=list)
    claimed_rewards_today: list[CouponInstanceOut] = Field(default_factory=list)
    offer_of_the_day: CouponInstanceOut | None = None
    new_in_town: list[BusinessOut] = Field(default_factory=list)
    notification_inbox: list[dict] = Field(default_factory=list)


class AiDebugResponse(BaseModel):
    prompt: str
    raw_model_response: str
    parsed_json: dict | None


AuthResponse.model_rebuild()


class UserPreferencesIn(BaseModel):
    interests: list[str] = Field(default_factory=list)
    budget_range: Literal["LOW", "MEDIUM", "HIGH"] = "MEDIUM"
    active_times: list[Literal["MORNING", "AFTERNOON", "EVENING", "NIGHT"]] = Field(default_factory=list)
    behavior_frequency: Literal["DAILY", "WEEKLY", "RARELY"] = "WEEKLY"
    is_spontaneous: bool = True
    location_habits: list[str] = Field(default_factory=list)
    environment_preference: Literal["INDOOR", "OUTDOOR", "BOTH"] = "BOTH"
    discovery_mode: Literal["POPULAR", "HIDDEN_GEMS", "MIXED"] = "MIXED"

    # richer signals for AI personalization
    time_budget_minutes: int | None = Field(default=None, ge=0, le=24 * 60)
    prefers_walkable: bool | None = None
    travel_radius_km: float | None = Field(default=None, ge=0, le=100)
    social_mode: Literal["SOLO", "COUPLE", "FRIENDS", "FAMILY", "ANY"] = "ANY"
    deal_sensitivity: int = Field(default=50, ge=0, le=100)
    novelty_seeking: int = Field(default=50, ge=0, le=100)  # popular vs hidden gems (separate axis)

    # contextual / decision questions (captured as structured JSON)
    right_now_choices: list[str] = Field(default_factory=list)
    avoid_list: list[str] = Field(default_factory=list)
    free_text_notes: str | None = Field(default=None, max_length=500)


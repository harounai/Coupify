from __future__ import annotations

from datetime import datetime

from sqlalchemy import Boolean, DateTime, ForeignKey, Integer, String, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.db import Base


class User(Base):
    __tablename__ = "users"

    id: Mapped[str] = mapped_column(String, primary_key=True)
    email: Mapped[str] = mapped_column(String, unique=True, index=True)
    password_hash: Mapped[str] = mapped_column(String)
    display_name: Mapped[str] = mapped_column(String, default="Alex")
    role: Mapped[str] = mapped_column(String, default="USER")  # USER|COMPANY
    company_business_id: Mapped[str | None] = mapped_column(String, ForeignKey("businesses.id"), nullable=True)
    interests_csv: Mapped[str] = mapped_column(String, default="")
    exploration_preference: Mapped[int] = mapped_column(Integer, default=50)  # 0..100
    survey_json: Mapped[str] = mapped_column(String, default="{}")
    has_completed_onboarding: Mapped[bool] = mapped_column(Boolean, default=False)
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)

    streak: Mapped["Streak"] = relationship(back_populates="user", uselist=False)
    preferences: Mapped["UserPreferences"] = relationship(back_populates="user", uselist=False)
    # Optional relationship for company accounts.
    company_business: Mapped["Business"] = relationship(foreign_keys=[company_business_id])


class Streak(Base):
    __tablename__ = "streaks"

    user_id: Mapped[str] = mapped_column(String, ForeignKey("users.id"), primary_key=True)
    current_days: Mapped[int] = mapped_column(Integer, default=0)
    best_days: Mapped[int] = mapped_column(Integer, default=0)
    last_checkin_date: Mapped[str | None] = mapped_column(String, nullable=True)  # YYYY-MM-DD

    user: Mapped[User] = relationship(back_populates="streak")


class Business(Base):
    __tablename__ = "businesses"

    id: Mapped[str] = mapped_column(String, primary_key=True)
    name: Mapped[str] = mapped_column(String)
    category: Mapped[str] = mapped_column(String)
    lat: Mapped[float] = mapped_column()
    lon: Mapped[float] = mapped_column()
    image_url: Mapped[str] = mapped_column(String, default="")


class CouponTemplate(Base):
    __tablename__ = "coupon_templates"

    id: Mapped[str] = mapped_column(String, primary_key=True)
    title: Mapped[str] = mapped_column(String)
    category: Mapped[str] = mapped_column(String)
    base_discount: Mapped[int] = mapped_column(Integer)
    duration_hours: Mapped[int] = mapped_column(Integer)


class CouponInstance(Base):
    """
    Lifecycle: available (implicit) -> claimed -> redeemed.
    We materialize only claimed/redeemed instances, templates remain static.
    """

    __tablename__ = "coupon_instances"
    __table_args__ = (UniqueConstraint("user_id", "template_id", "day_key", name="uq_user_template_day"),)

    id: Mapped[str] = mapped_column(String, primary_key=True)
    user_id: Mapped[str] = mapped_column(String, ForeignKey("users.id"))
    template_id: Mapped[str] = mapped_column(String, ForeignKey("coupon_templates.id"))
    business_id: Mapped[str] = mapped_column(String, ForeignKey("businesses.id"))
    status: Mapped[str] = mapped_column(String)  # claimed|redeemed
    discount_percent: Mapped[int] = mapped_column(Integer)
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)
    expires_at: Mapped[datetime] = mapped_column(DateTime)
    redeemed_at: Mapped[datetime | None] = mapped_column(DateTime, nullable=True)
    day_key: Mapped[str] = mapped_column(String)  # YYYY-MM-DD (when created/featured)

    user: Mapped[User] = relationship()
    template: Mapped[CouponTemplate] = relationship()
    business: Mapped[Business] = relationship()


class UserInteraction(Base):
    __tablename__ = "user_interactions"

    id: Mapped[str] = mapped_column(String, primary_key=True)
    user_id: Mapped[str] = mapped_column(String, ForeignKey("users.id"))
    event_type: Mapped[str] = mapped_column(String)  # view|claim|redeem|dismiss
    business_id: Mapped[str | None] = mapped_column(String, ForeignKey("businesses.id"), nullable=True)
    coupon_instance_id: Mapped[str | None] = mapped_column(String, ForeignKey("coupon_instances.id"), nullable=True)
    occurred_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)


class Notification(Base):
    __tablename__ = "notifications"

    id: Mapped[str] = mapped_column(String, primary_key=True)
    user_id: Mapped[str] = mapped_column(String, ForeignKey("users.id"), index=True)

    # one notification == one offer
    coupon_instance_id: Mapped[str] = mapped_column(String, ForeignKey("coupon_instances.id"))
    title: Mapped[str] = mapped_column(String)
    body: Mapped[str] = mapped_column(String)
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)

    status: Mapped[str] = mapped_column(String, default="pending")  # pending|accepted|declined

    coupon: Mapped[CouponInstance] = relationship()


class AuthSession(Base):
    """
    Token revocation + session tracking.
    We store JWT jti and can revoke it on logout.
    """

    __tablename__ = "auth_sessions"

    id: Mapped[str] = mapped_column(String, primary_key=True)  # jti
    user_id: Mapped[str] = mapped_column(String, ForeignKey("users.id"), index=True)
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)
    revoked_at: Mapped[datetime | None] = mapped_column(DateTime, nullable=True)


class UserPreferences(Base):
    __tablename__ = "user_preferences"

    user_id: Mapped[str] = mapped_column(String, ForeignKey("users.id"), primary_key=True)

    interests_csv: Mapped[str] = mapped_column(String, default="")
    budget_range: Mapped[str] = mapped_column(String, default="MEDIUM")  # LOW|MEDIUM|HIGH
    time_preferences_csv: Mapped[str] = mapped_column(String, default="")  # MORNING|...
    behavior_frequency: Mapped[str] = mapped_column(String, default="WEEKLY")  # DAILY|WEEKLY|RARELY
    spontaneous: Mapped[bool] = mapped_column(Boolean, default=True)
    location_habits_csv: Mapped[str] = mapped_column(String, default="")
    environment_preference: Mapped[str] = mapped_column(String, default="BOTH")  # INDOOR|OUTDOOR|BOTH
    discovery_mode: Mapped[str] = mapped_column(String, default="POPULAR")  # POPULAR|HIDDEN_GEMS

    # richer AI personalization payload (free-form JSON as text)
    preferences_json: Mapped[str] = mapped_column(String, default="{}")

    updated_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)

    user: Mapped[User] = relationship(back_populates="preferences")


class MerchantRule(Base):
    __tablename__ = "merchant_rules"

    business_id: Mapped[str] = mapped_column(String, ForeignKey("businesses.id"), primary_key=True)

    # Simple constraints/goals merchants can set without marketing resources.
    max_discount_percent: Mapped[int] = mapped_column(Integer, default=20)  # 0..100
    min_discount_percent: Mapped[int] = mapped_column(Integer, default=5)  # 0..100
    quiet_hours_start: Mapped[int | None] = mapped_column(Integer, nullable=True)  # 0..23
    quiet_hours_end: Mapped[int | None] = mapped_column(Integer, nullable=True)  # 0..23
    goal: Mapped[str] = mapped_column(String, default="FILL_QUIET_HOURS")  # free-form enum-ish

    # Coupon budget controls
    coupons_per_day: Mapped[int] = mapped_column(Integer, default=50)  # daily handout cap
    coupons_total: Mapped[int] = mapped_column(Integer, default=1000)  # overall cap for demo
    coupons_total_issued: Mapped[int] = mapped_column(Integer, default=0)

    # "Products" the merchant wants to push (simple categories or SKUs as CSV)
    products_csv: Mapped[str] = mapped_column(String, default="coffee,food")  # e.g. "latte,croissant"

    # Lightweight extra constraints as JSON (stored as text for SQLite simplicity)
    rules_json: Mapped[str] = mapped_column(String, default="{}")

    updated_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)

    business: Mapped["Business"] = relationship()


class MerchantStatsDaily(Base):
    __tablename__ = "merchant_stats_daily"
    __table_args__ = (UniqueConstraint("business_id", "day_key", name="uq_merchant_stats_business_day"),)

    id: Mapped[str] = mapped_column(String, primary_key=True)
    business_id: Mapped[str] = mapped_column(String, ForeignKey("businesses.id"), index=True)
    day_key: Mapped[str] = mapped_column(String)  # YYYY-MM-DD

    impressions: Mapped[int] = mapped_column(Integer, default=0)
    accepts: Mapped[int] = mapped_column(Integer, default=0)
    declines: Mapped[int] = mapped_column(Integer, default=0)
    redemptions: Mapped[int] = mapped_column(Integer, default=0)

    updated_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)

    business: Mapped["Business"] = relationship()


class DemandOverride(Base):
    """
    Demand proxy override for demos (simulates 'Payone transaction density unusually low').
    If present and not expired, it drives the visible demand level for the business.
    """

    __tablename__ = "demand_overrides"

    business_id: Mapped[str] = mapped_column(String, ForeignKey("businesses.id"), primary_key=True)
    demand_level: Mapped[int] = mapped_column(Integer, default=30)  # 0..100 (lower == quieter)
    expires_at: Mapped[datetime] = mapped_column(DateTime)
    updated_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)

    business: Mapped["Business"] = relationship()


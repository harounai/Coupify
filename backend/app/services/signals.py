from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime


@dataclass(frozen=True)
class WeatherSignal:
    condition: str  # Sunny|Cloudy|Rainy
    temperature_c: int


@dataclass(frozen=True)
class TimeSignal:
    now: datetime

    @property
    def hour(self) -> int:
        return self.now.hour

    @property
    def weekday(self) -> int:
        return self.now.weekday()  # 0=Mon

    @property
    def daypart(self) -> str:
        h = self.hour
        if 6 <= h <= 10:
            return "morning"
        if 11 <= h <= 14:
            return "lunch"
        if 15 <= h <= 18:
            return "afternoon"
        if 19 <= h <= 23:
            return "evening"
        return "night"

    @property
    def is_weekend(self) -> bool:
        return self.weekday >= 5


@dataclass(frozen=True)
class LocationSignal:
    lat: float
    lon: float


@dataclass(frozen=True)
class DemandSignal:
    # Simulated store demand 0..100
    demand_by_business_id: dict[str, int]


@dataclass(frozen=True)
class EventsSignal:
    # Local events (mock/static) relevant to a city
    events: list[dict]


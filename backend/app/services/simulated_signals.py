from __future__ import annotations

import random
from datetime import datetime

from app.services.signals import DemandSignal, EventsSignal, WeatherSignal


def get_weather_signal(now: datetime) -> WeatherSignal:
    # Offline + deterministic-ish per day
    seed = int(now.strftime("%Y%m%d"))
    rng = random.Random(seed)
    options = [
        WeatherSignal(condition="Sunny", temperature_c=rng.randint(22, 30)),
        WeatherSignal(condition="Cloudy", temperature_c=rng.randint(18, 24)),
        WeatherSignal(condition="Rainy", temperature_c=rng.randint(14, 21)),
    ]
    return options[rng.randint(0, len(options) - 1)]


def get_demand_signal(business_ids: list[str], now: datetime) -> DemandSignal:
    seed = int(now.strftime("%Y%m%d%H"))
    rng = random.Random(seed)
    # Higher = more crowded.
    return DemandSignal(demand_by_business_id={bid: rng.randint(25, 95) for bid in business_ids})


def get_events_signal(now: datetime) -> EventsSignal:
    # Static/mock dataset; could be loaded from JSON file later.
    events = [
        {"id": "evt_food_fest", "title": "Street Food Pop-up", "category_boost": "food", "active": now.weekday() >= 4},
        {"id": "evt_morning_run", "title": "Community Run", "category_boost": "fitness", "active": now.hour < 11},
        {"id": "evt_coffee_crawl", "title": "Coffee Crawl", "category_boost": "coffee", "active": now.weekday() >= 5},
    ]
    return EventsSignal(events=[e for e in events if e["active"]])


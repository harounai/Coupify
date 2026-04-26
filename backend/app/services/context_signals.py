from __future__ import annotations

import random
from datetime import datetime

import httpx
from sqlalchemy.orm import Session

from app.models import DemandOverride
from app.services.signals import DemandSignal, EventsSignal, WeatherSignal
from app.services.simulated_signals import get_events_signal as _sim_events, get_weather_signal as _sim_weather


def _weather_code_to_condition(code: int) -> str:
    # Open-Meteo weather codes (coarse mapping)
    if code in (0,):
        return "Sunny"
    if code in (1, 2, 3):
        return "Cloudy"
    if code in (45, 48):
        return "Foggy"
    if code in (51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82):
        return "Rainy"
    if code in (71, 73, 75, 77, 85, 86):
        return "Snowy"
    if code in (95, 96, 99):
        return "Stormy"
    return "Cloudy"


async def get_weather_signal(*, lat: float, lon: float, now: datetime) -> WeatherSignal:
    """
    Real weather if available (Open-Meteo, no API key), otherwise deterministic offline fallback.
    """
    try:
        url = "https://api.open-meteo.com/v1/forecast"
        params = {
            "latitude": lat,
            "longitude": lon,
            "current": "temperature_2m,weather_code",
        }
        async with httpx.AsyncClient(timeout=3.0) as client:
            r = await client.get(url, params=params)
            r.raise_for_status()
            data = r.json() or {}
            current = data.get("current") or {}
            temp = float(current.get("temperature_2m"))
            code = int(current.get("weather_code"))
            return WeatherSignal(condition=_weather_code_to_condition(code), temperature_c=int(round(temp)))
    except Exception:
        return _sim_weather(now)


def get_demand_signal(db: Session, business_ids: list[str], now: datetime) -> DemandSignal:
    """
    Demand proxy: deterministic-ish random baseline + optional DB overrides for demos.
    Lower == quieter.
    """
    seed = int(now.strftime("%Y%m%d%H"))
    rng = random.Random(seed)
    baseline = {bid: rng.randint(25, 95) for bid in business_ids}

    overrides = (
        db.query(DemandOverride)
        .filter(DemandOverride.business_id.in_(business_ids))
        .filter(DemandOverride.expires_at > now)
        .all()
    )
    for ov in overrides:
        baseline[ov.business_id] = int(ov.demand_level)

    return DemandSignal(demand_by_business_id=baseline)


def get_events_signal(now: datetime) -> EventsSignal:
    # Keep mock events for now; swappable later via config.
    return _sim_events(now)


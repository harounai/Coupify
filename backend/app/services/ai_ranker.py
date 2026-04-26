from __future__ import annotations

import json
from datetime import datetime

from app.services.ollama_client import OllamaClient
from app.services.signals import DemandSignal, EventsSignal, LocationSignal, TimeSignal, WeatherSignal


class AiRanker:
    def __init__(self, ollama: OllamaClient | None = None) -> None:
        self._ollama = ollama or OllamaClient()

    async def rank_home_feed(
        self,
        *,
        user_id: str,
        user_preferences: list[str],
        exploration_preference: int,
        weather: WeatherSignal,
        time: TimeSignal,
        location: LocationSignal,
        demand: DemandSignal,
        events: EventsSignal,
        candidates: list[dict],
    ) -> tuple[list[str], str, dict | None]:
        """
        Returns: (ranked_candidate_ids, raw_text, parsed_json)
        The candidates are dicts with at least: id, business_id, category, distance_km, base_discount.
        """

        prompt = build_rank_prompt(
            user_id=user_id,
            user_preferences=user_preferences,
            exploration_preference=exploration_preference,
            weather=weather,
            time=time,
            location=location,
            demand=demand,
            events=events,
            candidates=candidates,
        )

        result = await self._ollama.generate_json(prompt, temperature=0.2)
        ranked_ids: list[str] = []
        if result.parsed_json:
            ranked_ids = list(result.parsed_json.get("ranked_ids") or [])
        else:
            # Deterministic fallback ranking if Ollama isn't available.
            ranked_ids = _fallback_rank(candidates)
        return ranked_ids, result.raw_text, result.parsed_json


def _fallback_rank(candidates: list[dict]) -> list[str]:
    # Prefer closer distance, then higher base discount, then lower demand (less crowded).
    def key(c: dict) -> tuple:
        return (
            float(c.get("distance_km") or 999.0),
            -int(c.get("base_discount") or 0),
            int(c.get("demand_level") or 50),
            str(c.get("id") or ""),
        )

    return [c["id"] for c in sorted(candidates, key=key)]


def build_rank_prompt(
    *,
    user_id: str,
    user_preferences: list[str],
    exploration_preference: int,
    weather: WeatherSignal,
    time: TimeSignal,
    location: LocationSignal,
    demand: DemandSignal,
    events: EventsSignal,
    candidates: list[dict],
) -> str:
    schema = {
        "type": "object",
        "required": ["ranked_ids", "reasoning", "signals_used"],
        "properties": {
            "ranked_ids": {"type": "array", "items": {"type": "string"}},
            "reasoning": {"type": "string"},
            "signals_used": {"type": "array", "items": {"type": "string"}},
        },
        "additionalProperties": False,
    }

    payload = {
        "user": {
            "id": user_id,
            "preferences": user_preferences,
            "exploration_preference": exploration_preference,
        },
        "signals": {
            "weather": {"condition": weather.condition, "temperature_c": weather.temperature_c},
            "time": {
                "iso": time.now.isoformat(),
                "daypart": time.daypart,
                "is_weekend": time.is_weekend,
                "hour": time.hour,
                "weekday": time.weekday,
            },
            "location": {"lat": location.lat, "lon": location.lon},
            "demand": demand.demand_by_business_id,
            "events": events.events,
        },
        "candidates": candidates,
        "output_schema": schema,
    }

    return (
        "You are a ranking engine for a city offers app. "
        "Return ONLY valid JSON matching output_schema.\n\n"
        "Goal: rank candidate offers for the home feed.\n"
        "- Prefer close distance\n"
        "- Prefer matching user preferences, but allow exploration based on exploration_preference\n"
        "- Use weather/time to boost relevant categories (e.g. rainy -> food indoors, morning -> coffee)\n"
        "- Use demand: prefer underutilized places if demand is high (crowded) and push underutilized ones\n"
        "- Consider events: if event matches category/area, boost\n"
        "- Be consistent and deterministic.\n\n"
        f"INPUT:\n{json.dumps(payload, ensure_ascii=False)}\n"
    )


from __future__ import annotations

import json
from dataclasses import dataclass

import httpx

from app.core.config import settings


@dataclass(frozen=True)
class OllamaResult:
    raw_text: str
    parsed_json: dict | None


class OllamaClient:
    def __init__(self, base_url: str | None = None, model: str | None = None) -> None:
        self._base_url = (base_url or settings.ollama_base_url).rstrip("/")
        self._model = model or settings.ollama_model

    async def generate_json(self, prompt: str, *, temperature: float = 0.2) -> OllamaResult:
        """
        Calls the local Ollama REST API. No API keys, runs offline.
        We request JSON; models sometimes wrap JSON in text, so we attempt to extract.
        """
        url = f"{self._base_url}/api/generate"
        payload = {
            "model": self._model,
            "prompt": prompt,
            "stream": False,
            "options": {"temperature": temperature},
        }

        try:
            async with httpx.AsyncClient(timeout=30.0) as client:
                resp = await client.post(url, json=payload)
                resp.raise_for_status()
                data = resp.json()
        except Exception as e:
            # Offline-friendly fallback: if Ollama isn't running, return no JSON.
            return OllamaResult(raw_text=f"Ollama unavailable: {type(e).__name__}: {e}", parsed_json=None)

        text = (data.get("response") or "").strip()
        parsed = _try_parse_json(text)
        return OllamaResult(raw_text=text, parsed_json=parsed)


def _try_parse_json(text: str) -> dict | None:
    # Happy path: pure JSON.
    try:
        val = json.loads(text)
        return val if isinstance(val, dict) else None
    except Exception:
        pass

    # Common: fenced code block.
    for fence in ("```json", "```"):
        if fence in text:
            break
    if "```" in text:
        parts = text.split("```")
        # try middle chunks first
        for chunk in parts[1:-1]:
            chunk = chunk.strip()
            if chunk.startswith("json"):
                chunk = chunk[4:].strip()
            try:
                val = json.loads(chunk)
                return val if isinstance(val, dict) else None
            except Exception:
                continue

    # Last resort: find first '{' and last '}'.
    start = text.find("{")
    end = text.rfind("}")
    if start != -1 and end != -1 and end > start:
        candidate = text[start : end + 1]
        try:
            val = json.loads(candidate)
            return val if isinstance(val, dict) else None
        except Exception:
            return None

    return None


# CITY WALLET Backend (FastAPI)

Real-time context + dynamic offer engine for the CITY WALLET MVP.

This backend powers:
- context sensing (`weather + time + location + demand proxy`)
- dynamic offer generation from merchant rules
- redemption token + checkout validation simulation
- merchant analytics for generated/accepted/redeemed offers
- local LLM copy generation through Ollama (with deterministic fallback)

## Requirements

- Python 3.11+ (3.10 also works in most setups)
- Ollama running locally (`ollama serve`)
- An installed model, for example:

```bash
ollama pull llama3.1:8b
```

## Configuration

Create `.env` from `.env.example` (optional, defaults already work):

```bash
cp .env.example .env
```

Supported env vars:
- `OLLAMA_BASE_URL` (default `http://127.0.0.1:11434`)
- `OLLAMA_MODEL` (default `llama3.1:8b`)

## Run Locally

```bash
cd backend
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8000
```

API docs:
- [http://127.0.0.1:8000/docs](http://127.0.0.1:8000/docs)

## API Endpoints

- `GET /health`
- `POST /context/evaluate`
- `POST /offers/generate`
- `POST /redemptions/create`
- `POST /redemptions/validate`
- `GET /merchant/dashboard?city=stuttgart`

## Notes

- If Ollama is not reachable, generation still works with a local deterministic fallback.
- City-specific context behavior is config-driven via `config/cities/*.json`.

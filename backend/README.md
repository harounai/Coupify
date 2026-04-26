# City Spark Offers Backend (FastAPI + SQLite + Local Ollama AI)

Production-oriented local backend for the Android app.

## Key properties

- **Fully local / offline**: no paid APIs, no API keys.
- **DB-backed**: SQLite (`cityspark.db`) seeded on first run.
- **AI decision engine**: ranks the home feed using **local Ollama** (`http://localhost:11434`).
  - If Ollama is not running, the backend **falls back** to a deterministic heuristic ranking (distance/discount/demand).

## API (v1)

All endpoints accept an offline user identity header:

- `X-User-Id: user_alex` (default used by Android)

### User profile & preferences

- `GET /v1/users/me`
- `PATCH /v1/users/me`

### Streak tracking

- `GET /v1/streaks/me`
- `POST /v1/streaks/checkin`

### Rewards & coupons (claim/store/redeem)

- `GET /v1/coupons/templates`
- `GET /v1/coupons/my`
- `POST /v1/coupons/claim`
- `POST /v1/coupons/{couponId}/redeem`

### AI-driven home feed (core experience)

- `GET /v1/home`
  - Returns:
    - **Live Opportunities** (AI-ranked)
    - **Claimed Rewards Today** (pinned)
    - **Offer of the Day**
    - **New in Town**

### AI debug (inspect prompt + raw LLM output)

- `GET /v1/ai/debug/rank-home`

## Run locally

### 1) Start Ollama (optional but recommended)

In a separate terminal:

- `ollama serve`
- `ollama pull mistral`

The backend calls `http://localhost:11434` with model `mistral` by default.

### 2) Start backend

```bash
pip install -r requirements.txt
uvicorn app.main:app --host 127.0.0.1 --port 8000
```

Open docs at `http://127.0.0.1:8000/docs`.

## Example Ollama prompt

Call the debug endpoint:

- `GET /v1/ai/debug/rank-home` (with `X-User-Id: user_alex`)

It returns the exact `prompt` sent to Ollama, plus `raw_model_response` and parsed JSON.

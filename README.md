# CITY WALLET MVP

AI-powered city wallet prototype for real-time, context-aware local offers.

This repository contains:
- `backend/`: FastAPI context + offer + redemption API (Ollama-enabled)
- `application/`: Android app (consumer + merchant experience)

## End-to-End Flow

1. User context is sent to backend (`weather`, `time`, `location`, `demand proxy`).
2. Backend chooses best merchant rule for the current moment.
3. Offer copy + widget tone are generated dynamically (via Ollama when available).
4. User accepts and receives a dynamic tokenized QR payload.
5. Merchant scan is simulated and validated by API.
6. Merchant dashboard shows generated/accepted/redeemed metrics.

## Local Setup (Quick Start)

### 1) Start Ollama

```bash
ollama serve
ollama pull llama3.1:8b
```

### 2) Start backend

```bash
cd backend
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8000
```

Docs: [http://127.0.0.1:8000/docs](http://127.0.0.1:8000/docs)

### 3) Run Android app

Open `application/` in Android Studio and run the `app` module on an emulator.

The app is already configured to call backend at `http://10.0.2.2:8000/`.

## Push Checklist

- Backend boots and `/health` returns OK.
- `POST /offers/generate` returns dynamic response.
- Redemption flow works (`/redemptions/create` -> `/redemptions/validate`).
- Android app can fetch/generate offers and show QR.
- Merchant dashboard numbers update after redemption.

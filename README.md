# 🎟 Coupify

> **Offers that read the room.** Real-time context meets generative AI — so merchants stop shouting the same deal into the void.

---

## The Problem

Local merchants are still running promotions like it's 2005. Same discount, same message, every customer, every moment — rain or shine, busy or dead. Customers tune it out. Merchants wonder why nobody's biting.

The offer doesn't fit the moment. And the moment is everything.

---

## The Solution

Coupify pulls in live context — weather, time of day, location — and feeds it to an AI that writes a fresh, relevant offer on the spot. Merchants set the rules (what to push, max discount, quiet hours). The AI handles the creativity.

A cold rainy morning gets a different offer than a sunny lunch hour. Every time. Automatically.

> No templates. No copy-paste discounts. Just offers that actually make sense *right now.*

---

## What It Looks Like

**🛍 For customers**
A clean wallet UI showing a live, context-aware offer with a countdown timer. The deal expires — creating urgency — and a new one can be generated fresh.

**📊 For merchants**
A dashboard to define campaign rules, preview exactly what the AI will generate, and track what's working — acceptance rate, redemptions, revenue uplift, all updating in real time.

**⚙️ Under the hood**

```
Live weather + time of day
        ↓
  Context snapshot built
        ↓
  AI generates offer copy       ← Claude (or local Ollama)
        ↓
 Served to customer wallet
        ↓
  Redeemed → analytics updated
```

---

## Quick Start

```bash
# Backend
cd backend
python3 -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8000

# Web frontend
npm install && npm run dev
```

Add your `ANTHROPIC_API_KEY` to `backend/.env` to use Claude. No key? It falls back to Ollama locally, or a static offer if neither is available.

For Android: open `application/` in Android Studio and run on an emulator or device.

---

## Stack

| Layer | Technology |
|-------|-----------|
| Backend | FastAPI + Python |
| AI | Claude (Anthropic) / Ollama local fallback |
| Frontend (Web) | React + TypeScript + TailwindCSS |
| Frontend (Mobile) | Kotlin + Jetpack Compose |
| Weather | Open-Meteo API (free, no key needed) |

---

*Built for a hackathon. Extendable for the real world.*

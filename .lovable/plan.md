# Generative City-Wallet — MVP Plan

A polished, demo-ready React app with two views toggled from a top nav: a simulated mobile **Consumer Wallet** and a full-screen **Merchant Dashboard**. All state in-memory, offers hardcoded per scenario, set in Stuttgart.

## Visual direction

Light, airy, Apple-Wallet inspired.
- Background: warm off-white (`#FAFAF7`), generous whitespace
- Cards: soft pastel gradients (peach/coffee for warm offers, blue/lavender for cold-weather offers), large radius, subtle shadow
- Typography: tight, modern sans (Inter), large emotional headlines
- Motion: smooth Framer-Motion-style transitions (card flip to QR, scenario change cross-fade, countdown pulse)

## Routes & navigation

```
/                 -> redirects to /wallet
/wallet           -> Consumer Wallet (mobile frame, centered)
/merchant         -> Merchant Dashboard (sidebar layout)
/merchant/campaigns
/merchant/analytics
```

Top nav bar across both views with a pill toggle: **Consumer** | **Merchant**.

## 1. Consumer Wallet (`/wallet`)

Rendered inside a centered phone frame (~390×844, rounded bezel, subtle shadow) so it reads as a mobile mockup on desktop. On real mobile, frame collapses to full-screen.

**Header (inside frame)**
- Greeting: "Guten Tag, Lena" + small Stuttgart location chip
- "Simulate Context" dropdown with scenarios:
  - Cold & Raining — Tuesday 2:00 PM
  - Sunny & Warm — Friday 5:00 PM
  - Cold Evening — Thursday 7:00 PM
  - Lunch Rush — Wednesday 12:30 PM

**Generative Offer Card** (the hero widget)
- Cross-fades when scenario changes
- Contents:
  - Merchant avatar + name + distance ("80 m away")
  - Emotional headline tied to context (e.g. "Cold outside? Your cappuccino is waiting.")
  - Dynamic discount badge (e.g. "−30%")
  - Live countdown ("Valid for 14:32")
  - Why-this-offer micro-line ("Generated because: rain + quiet hours at Café Königsbau")
  - **Accept** (primary) and **Dismiss** (ghost) buttons

**Accept flow**
- Card flips/morphs into a Redemption Screen:
  - Big QR code (dummy SVG)
  - Redemption code text
  - Merchant + offer summary
  - **Simulate Merchant Scan** button → success state with check animation, "Saved €1.20", then "Back to Wallet"

**Dismiss flow**
- Card slides out, "Looking for your next offer…" placeholder, then surfaces an alternate offer for the same scenario.

**Below the hero card**
- Small "Recently saved" strip (mock past redemptions) for visual richness.

## 2. Merchant Dashboard (`/merchant/*`)

Full desktop layout, collapsible sidebar.

**Sidebar**
- Brand mark "City-Wallet • Merchant"
- Café profile chip ("Café Königsbau, Stuttgart")
- Nav: Overview, Campaigns (Rules Engine), Analytics

**Overview** (`/merchant`)
- Welcome header
- 3 KPI cards (mirrors Analytics, condensed)
- Recent generated offers list (mock)

**Campaigns / Rules Engine** (`/merchant/campaigns`)
- Clean form, two-column on desktop:
  - Quiet Hours (time range pickers, e.g. 14:00–17:00)
  - Max Discount % (slider 5–50%)
  - Goal (segmented control: Drive foot traffic / Clear inventory / Boost loyalty)
  - Inventory focus (multi-select chips: Coffee, Pastries, Lunch, Cold drinks)
  - Trigger conditions (toggles: Bad weather boost, Weekend boost)
  - Budget cap per day (€)
- Live "Preview offer" panel on the right showing how the rules render into a sample consumer card
- Save button → toast "Rules updated" (in-memory)

**Analytics** (`/merchant/analytics`)
- KPI cards:
  - Offers Generated Today — 247
  - Acceptance Rate — 38.2%
  - Simulated Payone Transaction Uplift — +€312 (▲ 22%)
- Two charts (Recharts):
  - Offers vs Acceptances by hour (bar)
  - Uplift trend last 7 days (line)
- Small table: top performing offer templates

## Hardcoded scenario → offer mapping

A single `scenarios.ts` module maps each scenario key to:
- 1 primary offer (shown first)
- 1 alternate (shown after Dismiss)
- Headline, merchant, distance, discount %, countdown seconds, gradient palette, reasoning string

Examples:
- Cold/Rainy 2PM → Café Königsbau, "Cold outside? Your cappuccino is waiting 80 m away.", −30%, 15:00, blue-lavender gradient
- Sunny/Warm 5PM → Biergarten am Schlossplatz, "Sun's out. First Radler on us.", −50% on first drink, 20:00, peach-amber gradient
- Cold Evening → Maultaschen Manufaktur, "Warm up with house Maultaschen.", −20%, 25:00, deep-plum gradient
- Lunch Rush → Bowls & Bites, "Skip the queue — order ahead, save 15%.", −15%, 10:00, mint-cream gradient

## Technical notes

- TanStack Start file routes: `wallet.tsx`, `merchant.tsx` (layout with sidebar + Outlet), `merchant.index.tsx`, `merchant.campaigns.tsx`, `merchant.analytics.tsx`, plus `index.tsx` redirecting to `/wallet`
- Each route gets its own `head()` metadata
- State: React `useState` + a small Zustand-free context for selected scenario and merchant rules; resets on refresh
- UI: existing shadcn components (Button, Card, Select, Slider, Toggle, Tabs, Sidebar, Sonner toasts)
- Charts: `recharts` (already common in shadcn setups; install if missing)
- QR code: lightweight `qrcode.react` package, or an inline SVG generator to avoid a dependency
- Animations: CSS transitions + a tiny `framer-motion` install for card morph / cross-fade
- Countdown: `useEffect` interval, pauses on tab blur
- Tailwind tokens added to `styles.css`: warm off-white background, card gradient utility classes, soft shadow tokens
- No backend, no Lovable Cloud

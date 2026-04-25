## Lucky Spin — overview

After accepting an offer, instead of jumping straight to the QR code, the user lands on a new **Lucky Spin** screen. They see a beautiful spinning roulette wheel with what *appears* to be a 50/50 split between two outcomes: **"Double or Nothing"** vs **"Safe Reward"**. In reality, the wheel is segmented and weighted so the result is engineered, not fair — and behind the "Safe Reward" side multiple sub-outcomes can pop (time extension, bonus item, normal coupon, etc.).

The goal is engagement, surprise, and a more premium, playful UI.

```text
Offer card → [Accept]
            ↓
    ┌────────────────┐
    │  Lucky Spin 🎰 │   ← new screen
    │  ◐ wheel ◑     │
    └────────────────┘
            ↓
   Outcome reveal card
            ↓
       QR / Redeem
```

## Outcomes & weighting

Visually the wheel shows **two big halves**: left = "Double or Nothing", right = "Safe Reward". Each half is sub-divided into thinner slices so the wheel still looks like a real roulette, but the final landing is decided by weighted random:

| Outcome | Visual side | Real probability | Effect |
|---|---|---|---|
| Double discount | Double or Nothing | 12% | Discount × 2 (capped at 80%) |
| Lose the offer | Double or Nothing | 8% | Offer voided, back to feed |
| +10 min time extension | Safe Reward | 25% | Adds 10 min to countdown |
| Bonus add-on (free item) | Safe Reward | 20% | Free pretzel / extra scoop |
| Normal coupon (as accepted) | Safe Reward | 30% | Standard redeem |
| Jackpot −80% | Safe Reward | 5% | Cap discount to 80% |

Weights live in `src/lib/spin.ts` so they're easy to tune. A "House edge" toggle in the merchant rules page (existing campaigns route) can later flip these — out of scope for this pass beyond a stub.

User-facing copy emphasizes "50/50" with two giant labels, but the slice geometry plus weighted RNG means the user almost always lands in a positive sub-slice.

## Wheel UI

- SVG wheel, 6–8 colored slices using the existing oklch palette + soft gradients.
- Conic-gradient backdrop with a subtle inner shadow for depth.
- Pointer at the top; wheel rotates with framer-motion `animate={{ rotate }}` using a long ease-out (≈4s, `[0.16, 1, 0.3, 1]`).
- On stop: the landed slice pulses, confetti-like sparkle ping (CSS only, no new dep), then a result card slides up.
- Haptic-style micro-bounce on the pointer at each slice crossing using a tick sound is **out of scope** (no audio).
- Skip button always visible — accessibility fallback that picks "Normal coupon".

## Outcome screens

Each outcome gets a tailored reveal card before continuing:

- **Double / Jackpot**: gold gradient, large "−X%", "Continue to redeem" → QR with updated `finalPrice`.
- **Time extension**: blue gradient, "+10:00 added", returns to the offer card with countdown reset higher.
- **Bonus add-on**: green gradient, "Free [item] added" line under offer, continues to QR.
- **Lose**: muted gradient, gentle "Better luck next time" + "Back to feed" — offer is dismissed.
- **Normal**: straight to QR, no extra screen.

## UI / aesthetic polish pass

- Replace the flat phone-frame background with a layered glass effect: subtle noise texture (CSS `background-image` data-uri), softer dual radial gradients with more saturation contrast.
- Add a thin top "status pill" showing live context (weather emoji + temp + time) above the offer card.
- Offer card: tighten spacing, add a 1px inner highlight (`shadow-inset`), bump heading to a display-weight, add `tracking-tight`.
- Buttons: convert primary CTA to a gradient pill (`bg-gradient-to-r from-foreground to-foreground/80`), add `active:scale-95` and a soft glow shadow.
- Add a small "Saved this week: €X" chip in the header that increments on redeem.
- Smooth route-level fade between offer → spin → reveal → redeem (all through existing AnimatePresence).
- Reduce motion respected via `prefers-reduced-motion` (wheel snaps instantly, no confetti).

## Files

**New**
- `src/lib/spin.ts` — outcome types, weights, `pickOutcome()` weighted RNG, slice geometry.
- `src/components/SpinWheel.tsx` — SVG wheel + spin animation + pointer.
- `src/components/SpinResultCard.tsx` — reveal card variants per outcome.

**Edited**
- `src/routes/wallet.tsx` — add `spin` and `result` view kinds between `offer` and `redeem`; thread outcome effects (modified discount / extended time / bonus / void) into the existing `RedeemCard` and `OfferCard`. Polish styling tokens.
- `src/lib/scenarios.ts` — add an optional `bonusItem?: string` per offer (e.g. "free Brezel", "extra scoop") used by the Bonus outcome copy.
- `src/styles.css` — one extra utility for the gradient CTA shadow + reduced-motion helper.

## Technical notes

- All randomness is local React state, no persistence (matches existing in-memory choice).
- Time extension mutates a local copy of the offer's `validSeconds`; the existing `useCountdown` already keys on `offer.id`, so we'll switch the key to include a "version" suffix to force restart cleanly.
- No new npm dependencies — wheel is hand-rolled SVG, animations via existing `framer-motion`.
- Strict TS: every new file is created in the same edit batch as its imports.

// Lucky Spin outcome system. Visually the wheel reads as a 50/50 between
// "Double or Nothing" and "Safe Reward", but landings are weighted RNG so
// the user almost always gets a positive sub-outcome.

export type SpinOutcomeKind =
  | "double"
  | "lose"
  | "extend"
  | "bonus"
  | "normal"
  | "jackpot";

export type SpinOutcome = {
  kind: SpinOutcomeKind;
  label: string;
  // Slice copy (short)
  short: string;
  // Long-form result copy
  title: string;
  description: string;
  // Half on the wheel — drives the visual side
  side: "risk" | "safe";
  // Slice index 0..7 — fixed geometry below
  sliceIndex: number;
  // Hex/oklch slice color
  color: string;
};

// 8 visual slices, alternating around the wheel between "risk" half (top)
// and "safe" half (bottom). Index 0 = top, going clockwise.
// Visual layout (top = pointer):
//   0,1,2,3 = right half = "Double or Nothing"
//   4,5,6,7 = left half  = "Safe Reward"
export const SLICES: SpinOutcome[] = [
  {
    kind: "double",
    label: "DOUBLE",
    short: "×2",
    title: "Double Down!",
    description: "Your discount has been doubled. Big win.",
    side: "risk",
    sliceIndex: 0,
    color: "oklch(0.78 0.16 55)", // amber
  },
  {
    kind: "lose",
    label: "NOTHING",
    short: "✗",
    title: "So close…",
    description: "The wheel went cold. Better luck next offer.",
    side: "risk",
    sliceIndex: 1,
    color: "oklch(0.55 0.05 270)", // muted slate
  },
  {
    kind: "double",
    label: "DOUBLE",
    short: "×2",
    title: "Double Down!",
    description: "Your discount has been doubled. Big win.",
    side: "risk",
    sliceIndex: 2,
    color: "oklch(0.82 0.14 60)",
  },
  {
    kind: "jackpot",
    label: "JACKPOT",
    short: "−80%",
    title: "Jackpot — 80% off!",
    description: "The house just blinked. Enjoy the steal.",
    side: "risk",
    sliceIndex: 3,
    color: "oklch(0.85 0.18 90)", // gold
  },
  {
    kind: "extend",
    label: "+10 MIN",
    short: "+10:00",
    title: "Time bonus",
    description: "10 extra minutes added to your offer window.",
    side: "safe",
    sliceIndex: 4,
    color: "oklch(0.82 0.09 220)", // soft blue
  },
  {
    kind: "bonus",
    label: "BONUS",
    short: "FREE +1",
    title: "Bonus added",
    description: "A free add-on rides along with your order.",
    side: "safe",
    sliceIndex: 5,
    color: "oklch(0.82 0.11 150)", // soft green
  },
  {
    kind: "normal",
    label: "COUPON",
    short: "✓",
    title: "Coupon locked in",
    description: "Your original offer, ready to redeem.",
    side: "safe",
    sliceIndex: 6,
    color: "oklch(0.92 0.03 90)", // cream
  },
  {
    kind: "bonus",
    label: "BONUS",
    short: "FREE +1",
    title: "Bonus added",
    description: "A free add-on rides along with your order.",
    side: "safe",
    sliceIndex: 7,
    color: "oklch(0.85 0.1 145)",
  },
];

// Real probabilities. Visual slice geometry is even (8×45°) but the picker
// is weighted — that's the trick. Sum = 100.
const WEIGHTS: Record<SpinOutcomeKind, number> = {
  double: 12,
  lose: 8,
  extend: 25,
  bonus: 20,
  normal: 30,
  jackpot: 5,
};

export function pickOutcome(): SpinOutcome {
  const total = Object.values(WEIGHTS).reduce((a, b) => a + b, 0);
  let r = Math.random() * total;
  let chosen: SpinOutcomeKind = "normal";
  for (const [k, w] of Object.entries(WEIGHTS) as [SpinOutcomeKind, number][]) {
    if (r < w) {
      chosen = k;
      break;
    }
    r -= w;
  }
  // Pick a random slice that matches this kind so the wheel stops
  // somewhere visually consistent (e.g. lands on a "double" slice for double).
  const candidates = SLICES.filter((s) => s.kind === chosen);
  return candidates[Math.floor(Math.random() * candidates.length)];
}

// Final rotation in degrees so that `slice.sliceIndex` ends under the
// top-pointer. Each slice is 45°; slice center sits at sliceIndex*45 + 22.5
// from the top going clockwise. We rotate the wheel counter-clockwise by
// that amount, plus several full turns for drama, plus a small jitter
// so it never lands dead-center.
export function rotationFor(slice: SpinOutcome, turns = 6): number {
  const sliceCenter = slice.sliceIndex * 45 + 22.5;
  const jitter = (Math.random() - 0.5) * 18; // ±9°, stays within slice
  return turns * 360 + (360 - sliceCenter) + jitter;
}

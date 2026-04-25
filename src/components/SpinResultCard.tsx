import { motion } from "framer-motion";
import { ArrowRight, RotateCcw } from "lucide-react";
import type { SpinOutcome } from "../lib/spin";

type Props = {
  outcome: SpinOutcome;
  onContinue: () => void;
  onBack: () => void;
};

const STYLES: Record<
  SpinOutcome["kind"],
  { gradient: string; emoji: string; cta: string }
> = {
  double: {
    gradient: "from-[oklch(0.95_0.1_70)] via-[oklch(0.93_0.12_55)] to-[oklch(0.9_0.13_45)]",
    emoji: "🔥",
    cta: "Continue to redeem",
  },
  jackpot: {
    gradient: "from-[oklch(0.95_0.13_90)] via-[oklch(0.92_0.15_75)] to-[oklch(0.88_0.17_70)]",
    emoji: "🎉",
    cta: "Claim jackpot",
  },
  extend: {
    gradient: "from-[oklch(0.95_0.06_220)] via-[oklch(0.93_0.08_210)] to-[oklch(0.91_0.09_200)]",
    emoji: "⏱️",
    cta: "Back to offer",
  },
  bonus: {
    gradient: "from-[oklch(0.95_0.07_150)] via-[oklch(0.93_0.09_140)] to-[oklch(0.91_0.1_135)]",
    emoji: "🎁",
    cta: "Continue to redeem",
  },
  lose: {
    gradient: "from-[oklch(0.93_0.01_270)] via-[oklch(0.9_0.015_270)] to-[oklch(0.88_0.02_270)]",
    emoji: "🌫️",
    cta: "Back to feed",
  },
  normal: {
    gradient: "from-[oklch(0.96_0.02_90)] via-[oklch(0.94_0.025_90)] to-[oklch(0.92_0.03_90)]",
    emoji: "✓",
    cta: "Continue to redeem",
  },
};

export function SpinResultCard({ outcome, onContinue, onBack }: Props) {
  const s = STYLES[outcome.kind];
  return (
    <div
      className={`relative overflow-hidden rounded-3xl bg-gradient-to-br ${s.gradient} p-6 shadow-[var(--shadow-card)]`}
    >
      <motion.div
        initial={{ scale: 0, rotate: -20 }}
        animate={{ scale: 1, rotate: 0 }}
        transition={{ type: "spring", stiffness: 220, damping: 14 }}
        className="mx-auto flex h-20 w-20 items-center justify-center rounded-full bg-white/70 text-4xl backdrop-blur"
      >
        {s.emoji}
      </motion.div>

      <div className="mt-4 text-center">
        <div className="text-[10px] font-semibold uppercase tracking-widest text-foreground/60">
          {outcome.label}
        </div>
        <h2 className="mt-1 text-[22px] font-bold tracking-tight text-foreground">
          {outcome.title}
        </h2>
        <p className="mt-1.5 text-[13px] text-foreground/70">
          {outcome.description}
        </p>
      </div>

      <div className="mt-6 grid grid-cols-[1fr_auto] gap-2">
        <button
          onClick={onContinue}
          className="flex items-center justify-center gap-2 rounded-2xl bg-foreground py-3 text-sm font-semibold text-background shadow-[0_8px_20px_-8px_oklch(0_0_0/0.4)] transition-transform active:scale-95"
        >
          {s.cta}
          <ArrowRight className="h-4 w-4" />
        </button>
        <button
          onClick={onBack}
          aria-label="Back"
          className="flex items-center justify-center rounded-2xl bg-white/60 px-4 text-foreground/70 backdrop-blur transition-colors hover:bg-white/80"
        >
          <RotateCcw className="h-4 w-4" />
        </button>
      </div>
    </div>
  );
}

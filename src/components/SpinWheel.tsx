import { motion, useReducedMotion } from "framer-motion";
import { useEffect, useState } from "react";
import { SLICES, pickOutcome, rotationFor, type SpinOutcome } from "../lib/spin";
import { Sparkles } from "lucide-react";

type Props = {
  onResult: (outcome: SpinOutcome) => void;
  onSkip: () => void;
};

export function SpinWheel({ onResult, onSkip }: Props) {
  const reduce = useReducedMotion();
  const [spinning, setSpinning] = useState(false);
  const [rotation, setRotation] = useState(0);
  const [picked, setPicked] = useState<SpinOutcome | null>(null);

  const spin = () => {
    if (spinning) return;
    const outcome = pickOutcome();
    const rot = rotationFor(outcome);
    setPicked(outcome);
    setSpinning(true);
    // For reduced motion, jump to the end instantly.
    setRotation(reduce ? rot : rot);
  };

  // Resolve when the framer animation completes.
  useEffect(() => {
    if (!spinning || !picked) return;
    const t = setTimeout(() => onResult(picked), reduce ? 50 : 4200);
    return () => clearTimeout(t);
  }, [spinning, picked, onResult, reduce]);

  const size = 280;
  const r = size / 2;

  return (
    <div className="overflow-hidden rounded-3xl bg-card p-5 shadow-[var(--shadow-card)]">
      <div className="text-center">
        <div className="inline-flex items-center gap-1.5 rounded-full bg-foreground/5 px-2.5 py-1 text-[10px] font-semibold uppercase tracking-widest text-foreground/70">
          <Sparkles className="h-3 w-3" />
          Lucky Spin
        </div>
        <h2 className="mt-2 text-[20px] font-bold tracking-tight">
          Double or Nothing?
        </h2>
        <p className="mt-1 text-[12px] text-muted-foreground">
          50/50 — risk it for double, or play it safe.
        </p>
      </div>

      {/* Two giant labels (the perceived 50/50) */}
      <div className="mt-4 grid grid-cols-2 gap-2 text-center">
        <div className="rounded-2xl bg-gradient-to-br from-[oklch(0.95_0.08_60)] to-[oklch(0.92_0.1_50)] py-2">
          <div className="text-[9px] font-semibold uppercase tracking-widest text-foreground/60">
            Risk
          </div>
          <div className="text-[13px] font-bold text-foreground">
            Double or Nothing
          </div>
        </div>
        <div className="rounded-2xl bg-gradient-to-br from-[oklch(0.95_0.05_180)] to-[oklch(0.93_0.07_150)] py-2">
          <div className="text-[9px] font-semibold uppercase tracking-widest text-foreground/60">
            Safe
          </div>
          <div className="text-[13px] font-bold text-foreground">
            Guaranteed Reward
          </div>
        </div>
      </div>

      {/* Wheel */}
      <div className="relative mx-auto mt-5" style={{ width: size, height: size }}>
        {/* Outer glow ring */}
        <div className="absolute inset-0 rounded-full bg-gradient-to-br from-[oklch(0.92_0.05_60)] via-[oklch(0.95_0.04_180)] to-[oklch(0.93_0.06_300)] blur-xl opacity-60" />
        <div className="absolute inset-1 rounded-full bg-card shadow-[0_8px_30px_-10px_oklch(0_0_0/0.25)]" />

        {/* Pointer */}
        <div className="absolute left-1/2 top-[-6px] z-20 -translate-x-1/2">
          <div className="h-0 w-0 border-l-[10px] border-r-[10px] border-t-[16px] border-l-transparent border-r-transparent border-t-foreground drop-shadow" />
        </div>

        {/* Rotating wheel */}
        <motion.svg
          width={size}
          height={size}
          viewBox={`0 0 ${size} ${size}`}
          className="relative z-10"
          style={{ originX: 0.5, originY: 0.5 }}
          animate={{ rotate: rotation }}
          transition={{
            duration: reduce ? 0 : 4,
            ease: [0.16, 1, 0.3, 1],
          }}
        >
          {SLICES.map((s, i) => {
            const startAngle = i * 45 - 90 - 22.5; // align slice 0 center to top
            const endAngle = startAngle + 45;
            const path = arcPath(r, r, r - 6, startAngle, endAngle);
            const labelAngle = (startAngle + endAngle) / 2;
            const lx = r + Math.cos((labelAngle * Math.PI) / 180) * (r - 50);
            const ly = r + Math.sin((labelAngle * Math.PI) / 180) * (r - 50);
            return (
              <g key={i}>
                <path
                  d={path}
                  fill={s.color}
                  stroke="white"
                  strokeWidth={2}
                  opacity={0.95}
                />
                <text
                  x={lx}
                  y={ly}
                  textAnchor="middle"
                  dominantBaseline="middle"
                  transform={`rotate(${labelAngle + 90}, ${lx}, ${ly})`}
                  style={{
                    fontSize: 11,
                    fontWeight: 800,
                    fill: "oklch(0.2 0.02 270)",
                    letterSpacing: "0.05em",
                  }}
                >
                  {s.label}
                </text>
              </g>
            );
          })}
          {/* Center hub */}
          <circle cx={r} cy={r} r={26} fill="white" />
          <circle cx={r} cy={r} r={26} fill="none" stroke="oklch(0.9 0.01 90)" strokeWidth={1} />
          <text
            x={r}
            y={r}
            textAnchor="middle"
            dominantBaseline="middle"
            style={{ fontSize: 16 }}
          >
            🎰
          </text>
        </motion.svg>
      </div>

      {/* Actions */}
      <div className="mt-5 grid grid-cols-[1fr_auto] gap-2">
        <button
          onClick={spin}
          disabled={spinning}
          className="rounded-2xl bg-gradient-to-r from-foreground to-[oklch(0.28_0.04_270)] py-3 text-sm font-semibold text-background shadow-[0_8px_20px_-8px_oklch(0_0_0/0.4)] transition-transform active:scale-95 disabled:opacity-60"
        >
          {spinning ? "Spinning…" : "Spin the wheel"}
        </button>
        <button
          onClick={onSkip}
          disabled={spinning}
          className="rounded-2xl bg-foreground/5 px-4 text-sm font-medium text-foreground/70 transition-colors hover:bg-foreground/10 disabled:opacity-50"
        >
          Skip
        </button>
      </div>
    </div>
  );
}

// SVG arc/wedge path.
function arcPath(
  cx: number,
  cy: number,
  radius: number,
  startDeg: number,
  endDeg: number
) {
  const start = polar(cx, cy, radius, endDeg);
  const end = polar(cx, cy, radius, startDeg);
  const large = endDeg - startDeg <= 180 ? "0" : "1";
  return [
    `M ${cx} ${cy}`,
    `L ${start.x} ${start.y}`,
    `A ${radius} ${radius} 0 ${large} 0 ${end.x} ${end.y}`,
    "Z",
  ].join(" ");
}
function polar(cx: number, cy: number, radius: number, deg: number) {
  const rad = (deg * Math.PI) / 180;
  return { x: cx + radius * Math.cos(rad), y: cy + radius * Math.sin(rad) };
}

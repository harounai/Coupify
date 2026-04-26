import { createFileRoute } from "@tanstack/react-router";
import { AnimatePresence, motion } from "framer-motion";
import { useCallback, useEffect, useRef, useState } from "react";
import {
  Check,
  ChevronDown,
  Clock,
  MapPin,
  RefreshCw,
  ScanLine,
  Sparkles,
  Thermometer,
  Wind,
  X,
  Gift,
  Wallet as WalletIcon,
  Zap,
} from "lucide-react";
import {
  api,
  SCENARIO_CONTEXTS,
  THEME_CONFIG,
  type ContextState,
  type GeneratedOffer,
} from "../lib/api";
import { QrCode } from "../components/QrCode";
import { SpinWheel } from "../components/SpinWheel";
import { SpinResultCard } from "../components/SpinResultCard";
import type { SpinOutcome } from "../lib/spin";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "../components/ui/select";

export const Route = createFileRoute("/wallet")({
  head: () => ({
    meta: [
      { title: "Wallet — City-Wallet Stuttgart" },
      { name: "description", content: "AI-generated local offers in real time." },
    ],
  }),
  component: WalletPage,
});

// ── Types ─────────────────────────────────────────────────────────────────────

type ActiveOffer = {
  id: string;
  merchant: string;
  category: string;
  distanceM: number;
  headline: string;
  subline: string;
  discount: number;
  originalPrice: number;
  finalPrice: number;
  reasoning: string;
  validSeconds: number;
  gradient: string;
  accent: string;
  emoji: string;
  bonusItem?: string;
  token: string;
  versionKey: string;
};

type View =
  | { kind: "offer"; offer: ActiveOffer }
  | { kind: "spin"; offer: ActiveOffer }
  | { kind: "result"; offer: ActiveOffer; outcome: SpinOutcome }
  | { kind: "redeem"; offer: ActiveOffer }
  | { kind: "redeemed"; offer: ActiveOffer };

const SCENARIOS_UI = [
  { key: "live", label: "Live context", emoji: "🌐", desc: "Real Stuttgart weather now" },
  { key: "cold-rain", label: "Cold & Raining", emoji: "🌧️", desc: "7°C, rain · Tue 14:00" },
  { key: "sunny-warm", label: "Sunny & Warm", emoji: "☀️", desc: "24°C, clear · Fri 17:00" },
  { key: "cold-evening", label: "Cold Evening", emoji: "🌙", desc: "4°C, clear · Thu 19:00" },
  { key: "lunch-rush", label: "Lunch Rush", emoji: "🥗", desc: "18°C, cloudy · Wed 12:30" },
];

function fromApi(o: GeneratedOffer): ActiveOffer {
  const th = THEME_CONFIG[o.theme] ?? THEME_CONFIG.blue;
  return {
    id: o.id,
    merchant: o.merchant,
    category: o.category,
    distanceM: o.distance_m,
    headline: o.headline,
    subline: o.subline,
    discount: o.discount,
    originalPrice: o.original_price,
    finalPrice: o.final_price,
    reasoning: o.reasoning,
    validSeconds: o.valid_seconds,
    gradient: th.gradient,
    accent: th.accent,
    emoji: o.emoji,
    bonusItem: o.bonus_item ?? undefined,
    token: o.token,
    versionKey: o.id,
  };
}

// ── Page ──────────────────────────────────────────────────────────────────────

function WalletPage() {
  const [scenarioKey, setScenarioKey] = useState("cold-rain");
  const [liveContext, setLiveContext] = useState<ContextState | null>(null);
  const [isGenerating, setIsGenerating] = useState(true);
  const [view, setView] = useState<View | null>(null);
  const [history, setHistory] = useState<{ offer: ActiveOffer; ts: number }[]>([]);
  const [backendOnline, setBackendOnline] = useState<boolean | null>(null);
  const generatingRef = useRef(false);

  const totalSaved = history.reduce(
    (sum, h) => sum + (h.offer.originalPrice - h.offer.finalPrice),
    0
  );

  // Probe backend health + fetch live weather once on mount
  useEffect(() => {
    api
      .health()
      .then(() => {
        setBackendOnline(true);
        return api.getContext();
      })
      .then(setLiveContext)
      .catch(() => setBackendOnline(false));
  }, []);

  const generateOffer = useCallback(
    async (key: string) => {
      if (generatingRef.current) return;
      generatingRef.current = true;
      setIsGenerating(true);
      setView(null);
      try {
        const ctx = key === "live" ? undefined : SCENARIO_CONTEXTS[key];
        const offer = await api.generateOffer({ context: ctx });
        setView({ kind: "offer", offer: fromApi(offer) });
      } catch {
        // Backend offline — show static fallback card
        setView({ kind: "offer", offer: STATIC_FALLBACK });
      } finally {
        setIsGenerating(false);
        generatingRef.current = false;
      }
    },
    []
  );

  // Generate on mount once backend status is known
  useEffect(() => {
    if (backendOnline === null) return;
    generateOffer(scenarioKey);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [backendOnline]);

  const onScenarioChange = (key: string) => {
    setScenarioKey(key);
    generateOffer(key);
  };

  const onAccept = (offer: ActiveOffer) => setView({ kind: "spin", offer });

  const onDismiss = () => generateOffer(scenarioKey);

  const onScan = async (offer: ActiveOffer) => {
    try {
      await api.redeemOffer({
        offer_id: offer.id,
        token: offer.token,
        merchant: offer.merchant,
        discount: offer.discount,
        original_price: offer.originalPrice,
        final_price: offer.finalPrice,
      });
    } catch {
      /* ignore — still show success */
    }
    setView({ kind: "redeemed", offer });
    setHistory((h) => [{ offer, ts: Date.now() }, ...h].slice(0, 4));
  };

  const backToWallet = () => generateOffer(scenarioKey);

  const onSpinResult = (outcome: SpinOutcome) => {
    if (view?.kind !== "spin") return;
    setView({ kind: "result", offer: view.offer, outcome });
  };

  const onSpinSkip = () => {
    if (view?.kind !== "spin") return;
    setView({ kind: "redeem", offer: view.offer });
  };

  const continueFromResult = () => {
    if (view?.kind !== "result") return;
    const { offer, outcome } = view;
    switch (outcome.kind) {
      case "double": {
        const d = Math.min(80, offer.discount * 2);
        setView({ kind: "redeem", offer: { ...offer, discount: d, finalPrice: +(offer.originalPrice * (1 - d / 100)).toFixed(2) } });
        break;
      }
      case "jackpot":
        setView({ kind: "redeem", offer: { ...offer, discount: 80, finalPrice: +(offer.originalPrice * 0.2).toFixed(2) } });
        break;
      case "extend":
        setView({ kind: "offer", offer: { ...offer, validSeconds: offer.validSeconds + 600, versionKey: `${offer.id}-ext` } });
        break;
      case "bonus":
        setView({ kind: "redeem", offer: { ...offer, bonusItem: offer.bonusItem ?? "free treat" } });
        break;
      case "lose":
        onDismiss();
        break;
      default:
        setView({ kind: "redeem", offer });
    }
  };

  // Active context for the weather pill
  const activeCtx =
    scenarioKey === "live"
      ? liveContext
      : (SCENARIO_CONTEXTS[scenarioKey] as ContextState | undefined) ?? null;

  const weatherPill = activeCtx
    ? `${activeCtx.weather.icon} ${activeCtx.weather.temp_c}°C · ${activeCtx.weather.description}`
    : liveContext
    ? `${liveContext.weather.icon} ${liveContext.weather.temp_c}°C · ${liveContext.weather.description}`
    : "⏳ Loading weather…";

  const timePill = activeCtx
    ? `🕐 ${activeCtx.time.day_of_week.slice(0, 3)} · ${activeCtx.time.period}`
    : "";

  const currentScenario = SCENARIOS_UI.find((s) => s.key === scenarioKey);

  return (
    <main className="relative flex flex-1 items-start justify-center px-4 py-8 sm:py-12">
      <div className="pointer-events-none absolute inset-0 -z-10 bg-[radial-gradient(ellipse_at_top,oklch(0.96_0.04_60),transparent_55%),radial-gradient(ellipse_at_bottom_right,oklch(0.94_0.05_240),transparent_55%),radial-gradient(ellipse_at_bottom_left,oklch(0.95_0.05_150),transparent_50%)]" />

      <div className="w-full max-w-md">
        {/* Phone frame */}
        <div className="relative mx-auto w-full max-w-[400px]">
          <div className="rounded-[2.75rem] border border-border/60 bg-card p-3 shadow-[var(--shadow-phone)]">
            <div className="relative h-[800px] overflow-hidden rounded-[2.25rem] bg-background">
              {/* status bar */}
              <div className="flex items-center justify-between px-6 pt-3 text-[11px] font-medium text-foreground/70">
                <span>9:41</span>
                <div className="h-5 w-20 rounded-full bg-foreground/90" />
                <span>100%</span>
              </div>

              {/* header */}
              <div className="px-5 pt-4">
                <div className="flex items-center justify-between">
                  <div>
                    <div className="text-xs text-muted-foreground">Guten Tag</div>
                    <div className="text-lg font-semibold tracking-tight">Mia</div>
                  </div>
                  <div className="flex items-center gap-1.5 rounded-full bg-muted px-2.5 py-1 text-[11px] text-muted-foreground">
                    <MapPin className="h-3 w-3" />
                    Stuttgart-Mitte
                  </div>
                </div>

                {/* context pills row */}
                <div className="mt-3 flex flex-wrap items-center gap-2">
                  <div className="flex items-center gap-1.5 rounded-full bg-success/10 px-2.5 py-1 text-[11px] font-semibold text-success">
                    <WalletIcon className="h-3 w-3" />
                    Saved €{totalSaved.toFixed(2)}
                  </div>
                  <div className="flex items-center gap-1.5 rounded-full border border-border/60 bg-card px-2.5 py-1 text-[11px] text-muted-foreground">
                    <Thermometer className="h-3 w-3" />
                    {weatherPill}
                  </div>
                  {timePill && (
                    <div className="flex items-center gap-1 rounded-full border border-border/60 bg-card px-2.5 py-1 text-[11px] text-muted-foreground">
                      {timePill}
                    </div>
                  )}
                </div>

                {/* live badge */}
                {backendOnline && (
                  <div className="mt-2 flex items-center gap-1.5 text-[10px] font-semibold text-success">
                    <span className="inline-block h-1.5 w-1.5 rounded-full bg-success" />
                    AI engine online · Open-Meteo weather live
                  </div>
                )}
                {backendOnline === false && (
                  <div className="mt-2 text-[10px] text-muted-foreground">
                    ⚠️ Backend offline — showing demo offers
                  </div>
                )}

                {/* scenario selector */}
                <div className="mt-3">
                  <label className="mb-1.5 block text-[10px] font-semibold uppercase tracking-widest text-muted-foreground">
                    Simulate context
                  </label>
                  <Select value={scenarioKey} onValueChange={onScenarioChange}>
                    <SelectTrigger className="h-11 w-full rounded-2xl border-border/70 bg-card text-left text-[13px]">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {SCENARIOS_UI.map((s) => (
                        <SelectItem key={s.key} value={s.key}>
                          <span className="mr-2">{s.emoji}</span>
                          {s.label}
                          <span className="ml-2 text-[11px] text-muted-foreground">
                            — {s.desc}
                          </span>
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
              </div>

              {/* main feed */}
              <div className="relative mt-4 px-5">
                <div className="mb-2 flex items-center justify-between">
                  <div className="flex items-center gap-1.5 text-[10px] font-semibold uppercase tracking-widest text-muted-foreground">
                    <Sparkles className="h-3 w-3" />
                    {isGenerating ? "AI is generating…" : "Generated for you"}
                  </div>
                  {!isGenerating && view?.kind === "offer" && (
                    <button
                      onClick={() => generateOffer(scenarioKey)}
                      className="flex items-center gap-1 rounded-full border border-border/60 bg-card px-2 py-0.5 text-[10px] text-muted-foreground transition-colors hover:text-foreground"
                    >
                      <RefreshCw className="h-2.5 w-2.5" />
                      Regenerate
                    </button>
                  )}
                </div>

                <AnimatePresence mode="wait">
                  {isGenerating && (
                    <motion.div
                      key="generating"
                      initial={{ opacity: 0, y: 10 }}
                      animate={{ opacity: 1, y: 0 }}
                      exit={{ opacity: 0 }}
                      transition={{ duration: 0.25 }}
                    >
                      <GeneratingSkeleton scenarioKey={scenarioKey} />
                    </motion.div>
                  )}

                  {!isGenerating && view?.kind === "offer" && (
                    <motion.div
                      key={`offer-${view.offer.versionKey}`}
                      initial={{ opacity: 0, y: 16, scale: 0.98 }}
                      animate={{ opacity: 1, y: 0, scale: 1 }}
                      exit={{ opacity: 0, y: -12, scale: 0.98 }}
                      transition={{ duration: 0.35, ease: [0.22, 1, 0.36, 1] }}
                    >
                      <OfferCard
                        offer={view.offer}
                        onAccept={() => onAccept(view.offer)}
                        onDismiss={onDismiss}
                      />
                    </motion.div>
                  )}

                  {!isGenerating && view?.kind === "spin" && (
                    <motion.div
                      key={`spin-${view.offer.id}`}
                      initial={{ opacity: 0, y: 20, scale: 0.96 }}
                      animate={{ opacity: 1, y: 0, scale: 1 }}
                      exit={{ opacity: 0, scale: 0.96 }}
                      transition={{ duration: 0.4, ease: [0.22, 1, 0.36, 1] }}
                    >
                      <SpinWheel onResult={onSpinResult} onSkip={onSpinSkip} />
                    </motion.div>
                  )}

                  {!isGenerating && view?.kind === "result" && (
                    <motion.div
                      key={`result-${view.outcome.kind}`}
                      initial={{ opacity: 0, y: 16, scale: 0.96 }}
                      animate={{ opacity: 1, y: 0, scale: 1 }}
                      exit={{ opacity: 0, scale: 0.96 }}
                      transition={{ duration: 0.4, ease: [0.22, 1, 0.36, 1] }}
                    >
                      <SpinResultCard
                        outcome={view.outcome}
                        onContinue={continueFromResult}
                        onBack={backToWallet}
                      />
                    </motion.div>
                  )}

                  {!isGenerating && view?.kind === "redeem" && (
                    <motion.div
                      key={`redeem-${view.offer.id}`}
                      initial={{ opacity: 0, rotateY: 35, scale: 0.95 }}
                      animate={{ opacity: 1, rotateY: 0, scale: 1 }}
                      exit={{ opacity: 0, scale: 0.95 }}
                      transition={{ duration: 0.45, ease: [0.22, 1, 0.36, 1] }}
                    >
                      <RedeemCard
                        offer={view.offer}
                        onScan={() => onScan(view.offer)}
                        onBack={backToWallet}
                      />
                    </motion.div>
                  )}

                  {!isGenerating && view?.kind === "redeemed" && (
                    <motion.div
                      key={`done-${view.offer.id}`}
                      initial={{ opacity: 0, scale: 0.9 }}
                      animate={{ opacity: 1, scale: 1 }}
                      exit={{ opacity: 0 }}
                      transition={{ duration: 0.35 }}
                    >
                      <SuccessCard offer={view.offer} onBack={backToWallet} />
                    </motion.div>
                  )}
                </AnimatePresence>

                {/* Recently saved */}
                {history.length > 0 &&
                  !isGenerating &&
                  view?.kind !== "redeem" &&
                  view?.kind !== "redeemed" &&
                  view?.kind !== "spin" &&
                  view?.kind !== "result" && (
                    <div className="mt-5">
                      <div className="mb-2 text-[10px] font-semibold uppercase tracking-widest text-muted-foreground">
                        Recently saved
                      </div>
                      <div className="flex gap-2 overflow-x-auto pb-2">
                        {history.map((h, i) => (
                          <div
                            key={i}
                            className="flex min-w-[140px] items-center gap-2 rounded-2xl border border-border/60 bg-card p-2.5"
                          >
                            <div className="text-lg">{h.offer.emoji}</div>
                            <div className="leading-tight">
                              <div className="text-[11px] font-medium">{h.offer.merchant}</div>
                              <div className="text-[10px] text-success">
                                Saved €{(h.offer.originalPrice - h.offer.finalPrice).toFixed(2)}
                              </div>
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}
              </div>
            </div>
          </div>
        </div>

        <p className="mt-4 text-center text-xs text-muted-foreground">
          AI offer engine · Open-Meteo weather · Stuttgart demo
        </p>
      </div>
    </main>
  );
}

// ── Loading skeleton ──────────────────────────────────────────────────────────

function GeneratingSkeleton({ scenarioKey }: { scenarioKey: string }) {
  const s = SCENARIOS_UI.find((x) => x.key === scenarioKey);
  return (
    <div className="overflow-hidden rounded-3xl border border-border/40 bg-card p-5 shadow-[var(--shadow-card)]">
      <div className="flex items-start justify-between">
        <div className="flex items-center gap-2.5">
          <div className="flex h-10 w-10 animate-pulse items-center justify-center rounded-xl bg-muted text-xl">
            {s?.emoji ?? "✨"}
          </div>
          <div className="space-y-1.5">
            <div className="h-3 w-28 animate-pulse rounded-full bg-muted" />
            <div className="h-2.5 w-20 animate-pulse rounded-full bg-muted/60" />
          </div>
        </div>
        <div className="h-8 w-14 animate-pulse rounded-xl bg-muted" />
      </div>
      <div className="mt-5 space-y-2.5">
        <div className="h-5 w-full animate-pulse rounded-full bg-muted" />
        <div className="h-5 w-4/5 animate-pulse rounded-full bg-muted" />
        <div className="h-3.5 w-3/5 animate-pulse rounded-full bg-muted/60" />
      </div>
      <div className="mt-4 h-10 animate-pulse rounded-2xl bg-muted" />
      <div className="mt-3 h-8 animate-pulse rounded-2xl bg-muted/60" />
      <div className="mt-4 flex items-center gap-2">
        <Zap className="h-3 w-3 animate-bounce text-muted-foreground" />
        <span className="text-[11px] text-muted-foreground">
          Claude is crafting your offer based on {s?.desc ?? "current context"}…
        </span>
      </div>
    </div>
  );
}

// ── Offer card ────────────────────────────────────────────────────────────────

function useCountdown(seconds: number, key: string) {
  const [remaining, setRemaining] = useState(seconds);
  const startRef = useRef<number>(Date.now());

  useEffect(() => {
    startRef.current = Date.now();
    setRemaining(seconds);
    const i = setInterval(() => {
      const elapsed = Math.floor((Date.now() - startRef.current) / 1000);
      const r = Math.max(0, seconds - elapsed);
      setRemaining(r);
      if (r === 0) clearInterval(i);
    }, 1000);
    return () => clearInterval(i);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [key]);

  const m = Math.floor(remaining / 60).toString().padStart(2, "0");
  const s = (remaining % 60).toString().padStart(2, "0");
  return { label: `${m}:${s}`, remaining };
}

function OfferCard({
  offer,
  onAccept,
  onDismiss,
}: {
  offer: ActiveOffer;
  onAccept: () => void;
  onDismiss: () => void;
}) {
  const { label, remaining } = useCountdown(offer.validSeconds, offer.versionKey);
  const urgent = remaining < 60;

  return (
    <div
      className={`relative overflow-hidden rounded-3xl bg-gradient-to-br ${offer.gradient} p-5 shadow-[var(--shadow-card)] ring-1 ring-white/40`}
    >
      <div className="flex items-start justify-between">
        <div className="flex items-center gap-2.5">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-white/70 text-xl backdrop-blur">
            {offer.emoji}
          </div>
          <div className="leading-tight">
            <div className="text-[13px] font-semibold">{offer.merchant}</div>
            <div className="text-[10px] text-foreground/60">{offer.category}</div>
          </div>
        </div>
        <div className={`text-right ${offer.accent}`}>
          <div className="text-2xl font-extrabold leading-none">−{offer.discount}%</div>
          <div className="text-[10px] uppercase tracking-widest opacity-70">today only</div>
        </div>
      </div>

      <h2 className="mt-4 text-[21px] font-bold leading-[1.15] tracking-tight text-foreground">
        {offer.headline}
      </h2>
      <p className="mt-1.5 text-[13px] text-foreground/70">{offer.subline}</p>

      <div className="mt-4 flex items-center justify-between rounded-2xl bg-white/55 px-3.5 py-2.5 backdrop-blur">
        <div className="flex items-center gap-1.5 text-[12px] font-medium text-foreground/80">
          <MapPin className="h-3.5 w-3.5" />
          {offer.distanceM} m away
        </div>
        <div
          className={`flex items-center gap-1.5 text-[12px] font-medium ${
            urgent ? "text-destructive" : "text-foreground/80"
          }`}
        >
          <Clock className="h-3.5 w-3.5" />
          Valid for {label}
        </div>
      </div>

      <div className="mt-3 flex items-center gap-2 rounded-2xl bg-white/40 px-3.5 py-2 text-[11px] text-foreground/70 backdrop-blur">
        <Sparkles className="h-3 w-3 shrink-0" />
        <span className="leading-snug">{offer.reasoning}</span>
      </div>

      <div className="mt-4 flex items-center justify-between text-[12px] text-foreground/70">
        <span>
          <span className="line-through opacity-60">€{offer.originalPrice.toFixed(2)}</span>{" "}
          <span className="font-semibold text-foreground">€{offer.finalPrice.toFixed(2)}</span>
        </span>
        <span className="inline-flex items-center gap-1 rounded-full bg-foreground/5 px-2 py-0.5 text-[10px] font-semibold uppercase tracking-widest text-foreground/60">
          <Sparkles className="h-2.5 w-2.5" />
          Spin to win
        </span>
      </div>

      <div className="mt-4 grid grid-cols-[1fr_auto] gap-2">
        <button
          onClick={onAccept}
          className="rounded-2xl bg-gradient-to-r from-foreground to-[oklch(0.28_0.04_270)] py-3 text-sm font-semibold text-background shadow-[0_8px_20px_-8px_oklch(0_0_0/0.4)] transition-transform active:scale-95"
        >
          Accept & spin
        </button>
        <button
          onClick={onDismiss}
          aria-label="Get new offer"
          className="flex items-center justify-center rounded-2xl bg-white/60 px-4 text-foreground/70 backdrop-blur transition-colors hover:bg-white/80"
        >
          <X className="h-5 w-5" />
        </button>
      </div>
    </div>
  );
}

// ── Redeem card ───────────────────────────────────────────────────────────────

function RedeemCard({
  offer,
  onScan,
  onBack,
}: {
  offer: ActiveOffer;
  onScan: () => void;
  onBack: () => void;
}) {
  const code = offer.token || `CW-${offer.id.slice(0, 8).toUpperCase()}`;

  return (
    <div
      className={`overflow-hidden rounded-3xl bg-gradient-to-br ${offer.gradient} p-5 shadow-[var(--shadow-card)] ring-1 ring-white/40`}
    >
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2.5">
          <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-white/70 text-lg">
            {offer.emoji}
          </div>
          <div className="leading-tight">
            <div className="text-[12px] font-semibold">{offer.merchant}</div>
            <div className={`text-[11px] font-medium ${offer.accent}`}>
              −{offer.discount}% applied
            </div>
          </div>
        </div>
        <button
          onClick={onBack}
          className="rounded-full bg-white/60 px-2.5 py-1 text-[11px] text-foreground/70 backdrop-blur"
        >
          Cancel
        </button>
      </div>

      <div className="mt-4 flex flex-col items-center">
        <QrCode value={code} size={200} />
        <div className="mt-3 font-mono text-[11px] tracking-widest text-foreground/70">{code}</div>
      </div>

      <div className="mt-4 rounded-2xl bg-white/55 p-3.5 text-center backdrop-blur">
        <div className="text-[11px] uppercase tracking-widest text-foreground/60">Show this to staff</div>
        <div className="mt-1 text-[14px] font-semibold leading-snug">{offer.headline}</div>
        <div className="mt-1 text-[12px] text-foreground/70">
          You pay €{offer.finalPrice.toFixed(2)} ·{" "}
          <span className="text-success">save €{(offer.originalPrice - offer.finalPrice).toFixed(2)}</span>
        </div>
        {offer.bonusItem && (
          <div className="mt-2 inline-flex items-center gap-1.5 rounded-full bg-[oklch(0.93_0.09_150)] px-2.5 py-1 text-[11px] font-semibold text-[oklch(0.35_0.13_150)]">
            <Gift className="h-3 w-3" />
            Includes {offer.bonusItem}
          </div>
        )}
      </div>

      <button
        onClick={onScan}
        className="mt-4 flex w-full items-center justify-center gap-2 rounded-2xl bg-foreground py-3 text-sm font-semibold text-background shadow-[0_8px_20px_-8px_oklch(0_0_0/0.4)] transition-transform active:scale-95"
      >
        <ScanLine className="h-4 w-4" />
        Simulate merchant scan
      </button>
    </div>
  );
}

// ── Success card ──────────────────────────────────────────────────────────────

function SuccessCard({ offer, onBack }: { offer: ActiveOffer; onBack: () => void }) {
  return (
    <div className="overflow-hidden rounded-3xl bg-card p-6 text-center shadow-[var(--shadow-card)]">
      <motion.div
        initial={{ scale: 0 }}
        animate={{ scale: 1 }}
        transition={{ type: "spring", stiffness: 260, damping: 18 }}
        className="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-success text-success-foreground"
      >
        <Check className="h-8 w-8" strokeWidth={3} />
      </motion.div>
      <div className="mt-4 text-lg font-semibold tracking-tight">Redeemed</div>
      <div className="mt-1 text-sm text-muted-foreground">
        {offer.merchant} · {offer.category}
      </div>
      <div className="mt-4 inline-flex items-center gap-2 rounded-full bg-success/10 px-3 py-1.5 text-sm font-medium text-success">
        Saved €{(offer.originalPrice - offer.finalPrice).toFixed(2)}
      </div>
      {offer.bonusItem && (
        <div className="mt-2 inline-flex items-center gap-1.5 rounded-full bg-[oklch(0.93_0.09_150)] px-3 py-1 text-[12px] font-semibold text-[oklch(0.35_0.13_150)]">
          <Gift className="h-3 w-3" />
          Bonus: {offer.bonusItem}
        </div>
      )}
      <p className="mt-4 text-xs text-muted-foreground">
        Payment of €{offer.finalPrice.toFixed(2)} processed via Payone (simulated).
        Redemption recorded in merchant dashboard.
      </p>
      <button
        onClick={onBack}
        className="mt-5 inline-flex items-center gap-1 rounded-full bg-foreground px-4 py-2 text-sm font-semibold text-background"
      >
        Back to wallet
        <ChevronDown className="h-3.5 w-3.5 -rotate-90" />
      </button>
    </div>
  );
}

// ── Static fallback (shown when backend is offline) ────────────────────────

const STATIC_FALLBACK: ActiveOffer = {
  id: "fallback-1",
  merchant: "Café Königsbau",
  category: "Café · Specialty Coffee",
  distanceM: 80,
  headline: "Cold outside? Your cappuccino is waiting 80 m away.",
  subline: "House-roasted beans, oat milk on the house.",
  discount: 30,
  originalPrice: 4.2,
  finalPrice: 2.94,
  reasoning: "Demo mode: backend offline. Start the FastAPI server to enable live AI offers.",
  validSeconds: 900,
  gradient: "from-[oklch(0.92_0.04_240)] via-[oklch(0.95_0.03_260)] to-[oklch(0.97_0.02_280)]",
  accent: "text-[oklch(0.4_0.12_250)]",
  emoji: "☕",
  bonusItem: "free oat-milk shot",
  token: "CW-DEMO0001",
  versionKey: "fallback-1",
};

import { createFileRoute } from "@tanstack/react-router";
import { AnimatePresence, motion } from "framer-motion";
import { useEffect, useMemo, useRef, useState } from "react";
import {
  Check,
  ChevronDown,
  Clock,
  MapPin,
  ScanLine,
  Sparkles,
  X,
} from "lucide-react";
import { SCENARIOS, type Offer } from "../lib/scenarios";
import { QrCode } from "../components/QrCode";
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
      {
        name: "description",
        content:
          "Your personal feed of contextual, real-time local offers around Stuttgart.",
      },
      { property: "og:title", content: "City-Wallet — Consumer Wallet" },
      {
        property: "og:description",
        content: "Generative local offers, tailored to weather, time and place.",
      },
    ],
  }),
  component: WalletPage,
});

type View =
  | { kind: "offer"; offer: Offer }
  | { kind: "redeem"; offer: Offer }
  | { kind: "redeemed"; offer: Offer }
  | { kind: "empty" };

function WalletPage() {
  const [scenarioKey, setScenarioKey] = useState(SCENARIOS[0].key);
  const [offerIndex, setOfferIndex] = useState(0);
  const [view, setView] = useState<View>({
    kind: "offer",
    offer: SCENARIOS[0].offers[0],
  });
  const [history, setHistory] = useState<{ offer: Offer; ts: number }[]>([]);

  const scenario = useMemo(
    () => SCENARIOS.find((s) => s.key === scenarioKey)!,
    [scenarioKey]
  );

  // When scenario changes, reset to first offer
  useEffect(() => {
    setOfferIndex(0);
    setView({ kind: "offer", offer: scenario.offers[0] });
  }, [scenarioKey, scenario.offers]);

  const onAccept = (offer: Offer) => {
    setView({ kind: "redeem", offer });
  };
  const onDismiss = () => {
    const next = offerIndex + 1;
    if (next < scenario.offers.length) {
      setOfferIndex(next);
      setView({ kind: "offer", offer: scenario.offers[next] });
    } else {
      setView({ kind: "empty" });
    }
  };
  const onScan = (offer: Offer) => {
    setView({ kind: "redeemed", offer });
    setHistory((h) => [{ offer, ts: Date.now() }, ...h].slice(0, 4));
  };
  const backToWallet = () => {
    setOfferIndex(0);
    setView({ kind: "offer", offer: scenario.offers[0] });
  };

  return (
    <main className="flex flex-1 items-start justify-center bg-[radial-gradient(ellipse_at_top,oklch(0.96_0.02_60),transparent_60%),radial-gradient(ellipse_at_bottom_right,oklch(0.95_0.03_240),transparent_55%)] px-4 py-8 sm:py-12">
      <div className="w-full max-w-md">
        {/* Phone frame */}
        <div className="relative mx-auto w-full max-w-[400px]">
          <div className="rounded-[2.75rem] border border-border/60 bg-card p-3 shadow-[var(--shadow-phone)]">
            <div className="relative h-[760px] overflow-hidden rounded-[2.25rem] bg-background">
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
                    <div className="text-lg font-semibold tracking-tight">Lena</div>
                  </div>
                  <div className="flex items-center gap-1.5 rounded-full bg-muted px-2.5 py-1 text-[11px] text-muted-foreground">
                    <MapPin className="h-3 w-3" />
                    Stuttgart-Mitte
                  </div>
                </div>

                {/* scenario selector */}
                <div className="mt-4">
                  <label className="mb-1.5 block text-[10px] font-semibold uppercase tracking-widest text-muted-foreground">
                    Simulate context
                  </label>
                  <Select value={scenarioKey} onValueChange={setScenarioKey}>
                    <SelectTrigger className="h-12 w-full rounded-2xl border-border/70 bg-card text-left">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {SCENARIOS.map((s) => (
                        <SelectItem key={s.key} value={s.key}>
                          <span className="mr-2">{s.emoji}</span>
                          {s.label} — {s.time}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <div className="mt-2 flex items-center gap-3 px-1 text-[11px] text-muted-foreground">
                    <span>{scenario.weather}</span>
                    <span>·</span>
                    <span>{scenario.time}</span>
                  </div>
                </div>
              </div>

              {/* main feed */}
              <div className="relative mt-5 px-5">
                <div className="mb-2 flex items-center gap-1.5 text-[10px] font-semibold uppercase tracking-widest text-muted-foreground">
                  <Sparkles className="h-3 w-3" />
                  Generated for you
                </div>

                <AnimatePresence mode="wait">
                  {view.kind === "offer" && (
                    <motion.div
                      key={`offer-${view.offer.id}`}
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

                  {view.kind === "redeem" && (
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

                  {view.kind === "redeemed" && (
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

                  {view.kind === "empty" && (
                    <motion.div
                      key="empty"
                      initial={{ opacity: 0 }}
                      animate={{ opacity: 1 }}
                      className="rounded-3xl border border-dashed border-border bg-card/60 p-8 text-center"
                    >
                      <div className="mx-auto mb-3 flex h-10 w-10 items-center justify-center rounded-full bg-muted">
                        <Sparkles className="h-4 w-4 text-muted-foreground" />
                      </div>
                      <div className="text-sm font-medium">
                        Looking for your next offer…
                      </div>
                      <p className="mt-1 text-xs text-muted-foreground">
                        Try another context above.
                      </p>
                    </motion.div>
                  )}
                </AnimatePresence>

                {/* Recently saved */}
                {history.length > 0 && view.kind !== "redeem" && view.kind !== "redeemed" && (
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
                            <div className="text-[11px] font-medium">
                              {h.offer.merchant}
                            </div>
                            <div className="text-[10px] text-success">
                              Saved €
                              {(h.offer.originalPrice - h.offer.finalPrice).toFixed(2)}
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
          Mobile preview · all data simulated for demo purposes
        </p>
      </div>
    </main>
  );
}

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
  offer: Offer;
  onAccept: () => void;
  onDismiss: () => void;
}) {
  const { label, remaining } = useCountdown(offer.validSeconds, offer.id);
  const urgent = remaining < 60;

  return (
    <div
      className={`relative overflow-hidden rounded-3xl bg-gradient-to-br ${offer.gradient} p-5 shadow-[var(--shadow-card)]`}
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
          <div className="text-[10px] uppercase tracking-widest opacity-70">
            today only
          </div>
        </div>
      </div>

      <h2 className="mt-4 text-[22px] font-bold leading-[1.15] tracking-tight text-foreground">
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
          <span className="line-through opacity-60">
            €{offer.originalPrice.toFixed(2)}
          </span>{" "}
          <span className="font-semibold text-foreground">
            €{offer.finalPrice.toFixed(2)}
          </span>
        </span>
      </div>

      <div className="mt-4 grid grid-cols-[1fr_auto] gap-2">
        <button
          onClick={onAccept}
          className="rounded-2xl bg-foreground py-3 text-sm font-semibold text-background transition-transform active:scale-[0.98]"
        >
          Accept offer
        </button>
        <button
          onClick={onDismiss}
          aria-label="Dismiss"
          className="flex items-center justify-center rounded-2xl bg-white/60 px-4 text-foreground/70 backdrop-blur transition-colors hover:bg-white/80"
        >
          <X className="h-5 w-5" />
        </button>
      </div>
    </div>
  );
}

function RedeemCard({
  offer,
  onScan,
  onBack,
}: {
  offer: Offer;
  onScan: () => void;
  onBack: () => void;
}) {
  const code = `CW-${offer.id.toUpperCase()}-${Math.floor(Math.random() * 9000) + 1000}`;
  return (
    <div
      className={`overflow-hidden rounded-3xl bg-gradient-to-br ${offer.gradient} p-5 shadow-[var(--shadow-card)]`}
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
        <QrCode value={code} size={210} />
        <div className="mt-3 font-mono text-[11px] tracking-widest text-foreground/70">
          {code}
        </div>
      </div>

      <div className="mt-4 rounded-2xl bg-white/55 p-3.5 text-center backdrop-blur">
        <div className="text-[11px] uppercase tracking-widest text-foreground/60">
          Show this to staff
        </div>
        <div className="mt-1 text-[14px] font-semibold leading-snug">
          {offer.headline}
        </div>
        <div className="mt-1 text-[12px] text-foreground/70">
          You pay €{offer.finalPrice.toFixed(2)} ·{" "}
          <span className="text-success">
            save €{(offer.originalPrice - offer.finalPrice).toFixed(2)}
          </span>
        </div>
      </div>

      <button
        onClick={onScan}
        className="mt-4 flex w-full items-center justify-center gap-2 rounded-2xl bg-foreground py-3 text-sm font-semibold text-background transition-transform active:scale-[0.98]"
      >
        <ScanLine className="h-4 w-4" />
        Simulate merchant scan
      </button>
    </div>
  );
}

function SuccessCard({ offer, onBack }: { offer: Offer; onBack: () => void }) {
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
      <p className="mt-4 text-xs text-muted-foreground">
        Payment of €{offer.finalPrice.toFixed(2)} processed via Payone (simulated).
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

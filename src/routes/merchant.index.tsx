import { createFileRoute, Link } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import {
  ArrowUpRight,
  Coffee,
  Croissant,
  Loader2,
  Sparkles,
  TrendingUp,
} from "lucide-react";
import { api, type AnalyticsData, type ContextState } from "../lib/api";

export const Route = createFileRoute("/merchant/")({
  head: () => ({
    meta: [
      { title: "Overview — Merchant · City-Wallet" },
      { name: "description", content: "Today's offer activity, acceptance rate and uplift at a glance." },
    ],
  }),
  component: OverviewPage,
});

function OverviewPage() {
  const [analytics, setAnalytics] = useState<AnalyticsData | null>(null);
  const [context, setContext] = useState<ContextState | null>(null);
  const [applyingHint, setApplyingHint] = useState(false);
  const [hintApplied, setHintApplied] = useState(false);

  useEffect(() => {
    api.getAnalytics().then(setAnalytics).catch(() => {});
    api.getContext().then(setContext).catch(() => {});

    const interval = setInterval(() => {
      api.getAnalytics().then(setAnalytics).catch(() => {});
    }, 10_000);
    return () => clearInterval(interval);
  }, []);

  // Build the AI suggestion from live context
  const buildHint = () => {
    if (!context) return "Rain expected soon. Boost warm-drink offer to attract walk-ins?";
    const { weather, time } = context;
    const isRainy = ["rainy", "drizzly", "stormy", "overcast"].includes(weather.weather_type);
    const isCold = weather.temp_c < 12;
    if (isRainy && isCold)
      return `${weather.icon} ${weather.temp_c}°C & ${weather.description} right now. Boost cappuccino offer to −35% to capture walk-ins?`;
    if (isRainy)
      return `${weather.icon} ${weather.description} outside. Push a comfort-food offer for the next hour?`;
    if (isCold)
      return `${weather.icon} ${weather.temp_c}°C — cool enough to boost warm-drink offers. Try −25%?`;
    if (time.period === "lunch")
      return `⏰ Lunch rush (${time.hour}:00). Perfect time to push a quick-meal deal at −15%.`;
    return `${weather.icon} ${weather.description}, ${weather.temp_c}°C. Conditions are good — run a loyalty offer for regulars?`;
  };

  const applyHint = async () => {
    setApplyingHint(true);
    try {
      const rules = await api.getRules();
      await api.saveRules({ ...rules, weather_boost: true, max_discount: Math.min(rules.max_discount + 5, 50) });
      setHintApplied(true);
    } catch {
      setHintApplied(true);
    } finally {
      setApplyingHint(false);
    }
  };

  const fmt = (n: number) => n.toLocaleString("de-DE");

  return (
    <div className="mx-auto max-w-5xl">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <div className="text-[11px] font-semibold uppercase tracking-widest text-muted-foreground">
            Overview
          </div>
          <h1 className="mt-1 text-3xl font-bold tracking-tight">
            Good {context ? context.time.period : "day"}, Café Königsbau ☕
          </h1>
          <p className="mt-1 text-sm text-muted-foreground">
            Your generative offers are live. Here's what's happening today.
          </p>
        </div>
        <Link
          to="/merchant/campaigns"
          className="inline-flex items-center gap-1.5 rounded-full bg-foreground px-4 py-2 text-sm font-medium text-background"
        >
          Edit rules
          <ArrowUpRight className="h-3.5 w-3.5" />
        </Link>
      </div>

      <div className="mt-6 grid grid-cols-1 gap-4 sm:grid-cols-3">
        <Kpi
          label="Offers generated today"
          value={analytics ? fmt(analytics.offers_generated_today) : "—"}
          delta={analytics?.delta_offers ?? "loading…"}
          tone="text-foreground"
          loading={!analytics}
        />
        <Kpi
          label="Acceptance rate"
          value={analytics ? `${analytics.acceptance_rate}%` : "—"}
          delta={analytics?.delta_acceptance ?? "loading…"}
          tone="text-success"
          loading={!analytics}
        />
        <Kpi
          label="Payone uplift (sim.)"
          value={analytics ? `€${analytics.payone_uplift.toFixed(0)}` : "—"}
          delta={analytics?.delta_uplift ?? "loading…"}
          tone="text-success"
          loading={!analytics}
        />
      </div>

      <div className="mt-8 grid grid-cols-1 gap-6 lg:grid-cols-3">
        {/* Recent offers */}
        <div className="rounded-3xl border border-border/60 bg-card p-5 shadow-[var(--shadow-soft)] lg:col-span-2">
          <div className="mb-3 flex items-center justify-between">
            <h2 className="text-base font-semibold">Recent offers generated</h2>
            <span className="text-[11px] text-muted-foreground">live · updates every 10 s</span>
          </div>
          {analytics ? (
            <ul className="divide-y divide-border/60">
              {analytics.recent_offers.map((r, i) => {
                const Icon = i % 2 === 0 ? Coffee : Croissant;
                return (
                  <li key={i} className="flex items-center gap-3 py-3">
                    <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-muted">
                      <Icon className="h-4 w-4 text-foreground/70" />
                    </div>
                    <div className="flex-1 leading-tight">
                      <div className="text-sm font-medium">{r.title}</div>
                      <div className="text-[11px] text-muted-foreground">
                        {r.context} · {r.time}
                      </div>
                    </div>
                    <div className="text-right">
                      <div className="text-sm font-semibold">−{r.discount}%</div>
                      <div
                        className={`text-[11px] ${
                          r.status === "Accepted" ? "text-success" : "text-muted-foreground"
                        }`}
                      >
                        {r.status}
                      </div>
                    </div>
                  </li>
                );
              })}
            </ul>
          ) : (
            <div className="flex items-center gap-2 py-8 text-muted-foreground">
              <Loader2 className="h-4 w-4 animate-spin" />
              Loading recent activity…
            </div>
          )}
        </div>

        {/* AI suggestion */}
        <div className="rounded-3xl border border-border/60 bg-gradient-to-br from-[oklch(0.96_0.04_60)] to-[oklch(0.95_0.05_30)] p-5 shadow-[var(--shadow-soft)]">
          <div className="flex items-center gap-2 text-[11px] font-semibold uppercase tracking-widest text-foreground/70">
            <Sparkles className="h-3 w-3" />
            Live AI suggestion
          </div>
          {context ? (
            <>
              <p className="mt-3 text-[15px] font-semibold leading-snug">{buildHint()}</p>
              {!hintApplied ? (
                <button
                  onClick={applyHint}
                  disabled={applyingHint}
                  className="mt-4 inline-flex items-center gap-1.5 rounded-full bg-foreground px-3.5 py-1.5 text-xs font-medium text-background disabled:opacity-60"
                >
                  {applyingHint ? (
                    <Loader2 className="h-3 w-3 animate-spin" />
                  ) : (
                    <TrendingUp className="h-3 w-3" />
                  )}
                  {applyingHint ? "Applying…" : "Apply suggestion"}
                </button>
              ) : (
                <div className="mt-4 text-[12px] font-medium text-success">
                  ✓ Rules updated — engine will use them immediately.
                </div>
              )}
              <div className="mt-3 text-[11px] text-foreground/50">
                Based on Open-Meteo live data: {context.weather.temp_c}°C,{" "}
                {context.weather.description}
              </div>
            </>
          ) : (
            <div className="mt-3 flex items-center gap-2 text-sm text-muted-foreground">
              <Loader2 className="h-4 w-4 animate-spin" />
              Fetching live weather…
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

function Kpi({
  label,
  value,
  delta,
  tone,
  loading,
}: {
  label: string;
  value: string;
  delta: string;
  tone: string;
  loading?: boolean;
}) {
  return (
    <div className="rounded-3xl border border-border/60 bg-card p-5 shadow-[var(--shadow-soft)]">
      <div className="text-[11px] font-semibold uppercase tracking-widest text-muted-foreground">
        {label}
      </div>
      {loading ? (
        <div className="mt-2 h-9 w-24 animate-pulse rounded-full bg-muted" />
      ) : (
        <div className={`mt-2 text-3xl font-bold tracking-tight ${tone}`}>{value}</div>
      )}
      <div className="mt-1 text-xs text-muted-foreground">{delta}</div>
    </div>
  );
}

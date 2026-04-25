import { createFileRoute, Link } from "@tanstack/react-router";
import {
  ArrowUpRight,
  Coffee,
  Croissant,
  Sparkles,
  TrendingUp,
} from "lucide-react";

export const Route = createFileRoute("/merchant/")({
  head: () => ({
    meta: [
      { title: "Overview — Merchant · City-Wallet" },
      {
        name: "description",
        content: "Today's offer activity, acceptance rate and uplift at a glance.",
      },
    ],
  }),
  component: OverviewPage,
});

function OverviewPage() {
  return (
    <div className="mx-auto max-w-5xl">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <div className="text-[11px] font-semibold uppercase tracking-widest text-muted-foreground">
            Overview
          </div>
          <h1 className="mt-1 text-3xl font-bold tracking-tight">
            Good afternoon, Café Königsbau ☕
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
          value="247"
          delta="+12% vs yesterday"
          tone="text-foreground"
        />
        <Kpi
          label="Acceptance rate"
          value="38.2%"
          delta="+4.1 pp"
          tone="text-success"
        />
        <Kpi
          label="Payone uplift (sim.)"
          value="€312"
          delta="▲ 22%"
          tone="text-success"
        />
      </div>

      <div className="mt-8 grid grid-cols-1 gap-6 lg:grid-cols-3">
        <div className="rounded-3xl border border-border/60 bg-card p-5 shadow-[var(--shadow-soft)] lg:col-span-2">
          <div className="mb-3 flex items-center justify-between">
            <h2 className="text-base font-semibold">Recent offers generated</h2>
            <span className="text-[11px] text-muted-foreground">last hour</span>
          </div>
          <ul className="divide-y divide-border/60">
            {RECENT.map((r, i) => (
              <li key={i} className="flex items-center gap-3 py-3">
                <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-muted">
                  <r.icon className="h-4 w-4 text-foreground/70" />
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
                      r.status === "Accepted"
                        ? "text-success"
                        : "text-muted-foreground"
                    }`}
                  >
                    {r.status}
                  </div>
                </div>
              </li>
            ))}
          </ul>
        </div>

        <div className="rounded-3xl border border-border/60 bg-gradient-to-br from-[oklch(0.96_0.04_60)] to-[oklch(0.95_0.05_30)] p-5 shadow-[var(--shadow-soft)]">
          <div className="flex items-center gap-2 text-[11px] font-semibold uppercase tracking-widest text-foreground/70">
            <Sparkles className="h-3 w-3" />
            AI suggestion
          </div>
          <p className="mt-3 text-[15px] font-semibold leading-snug">
            Rain expected at 16:00. Boost cappuccino offer to −35% to capture
            walk-ins?
          </p>
          <button className="mt-4 inline-flex items-center gap-1 rounded-full bg-foreground px-3.5 py-1.5 text-xs font-medium text-background">
            Apply suggestion
            <TrendingUp className="h-3 w-3" />
          </button>
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
}: {
  label: string;
  value: string;
  delta: string;
  tone: string;
}) {
  return (
    <div className="rounded-3xl border border-border/60 bg-card p-5 shadow-[var(--shadow-soft)]">
      <div className="text-[11px] font-semibold uppercase tracking-widest text-muted-foreground">
        {label}
      </div>
      <div className={`mt-2 text-3xl font-bold tracking-tight ${tone}`}>{value}</div>
      <div className="mt-1 text-xs text-muted-foreground">{delta}</div>
    </div>
  );
}

const RECENT = [
  {
    icon: Coffee,
    title: "Cappuccino · −30% to user 80m away",
    context: "Cold/Rain trigger",
    time: "2 min ago",
    discount: 30,
    status: "Accepted",
  },
  {
    icon: Croissant,
    title: "Butter croissant · −20% lunchtime push",
    context: "Quiet-hours rule",
    time: "8 min ago",
    discount: 20,
    status: "Generated",
  },
  {
    icon: Coffee,
    title: "Flat white · −25% to passerby",
    context: "Foot-traffic goal",
    time: "14 min ago",
    discount: 25,
    status: "Accepted",
  },
  {
    icon: Croissant,
    title: "Pain au chocolat · −40% inventory clear",
    context: "End-of-day rule",
    time: "22 min ago",
    discount: 40,
    status: "Dismissed",
  },
];

import { createFileRoute } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { Loader2 } from "lucide-react";
import {
  Bar,
  BarChart,
  CartesianGrid,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { api, type AnalyticsData } from "../lib/api";

export const Route = createFileRoute("/merchant/analytics")({
  head: () => ({
    meta: [
      { title: "Analytics — Merchant · City-Wallet" },
      { name: "description", content: "Acceptance rate, hourly performance and Payone transaction uplift." },
    ],
  }),
  component: AnalyticsPage,
});

function AnalyticsPage() {
  const [data, setData] = useState<AnalyticsData | null>(null);

  useEffect(() => {
    api.getAnalytics().then(setData).catch(() => {});
    const interval = setInterval(() => {
      api.getAnalytics().then(setData).catch(() => {});
    }, 15_000);
    return () => clearInterval(interval);
  }, []);

  return (
    <div className="mx-auto max-w-6xl">
      <div>
        <div className="text-[11px] font-semibold uppercase tracking-widest text-muted-foreground">
          Analytics
        </div>
        <h1 className="mt-1 text-3xl font-bold tracking-tight">Performance</h1>
        <p className="mt-1 text-sm text-muted-foreground">
          What the generative engine drove for you today and this week.
        </p>
      </div>

      <div className="mt-6 grid grid-cols-1 gap-4 sm:grid-cols-3">
        <Kpi
          label="Offers generated today"
          value={data ? data.offers_generated_today.toLocaleString("de-DE") : "—"}
          delta={data?.delta_offers ?? "—"}
          loading={!data}
        />
        <Kpi
          label="Acceptance rate"
          value={data ? `${data.acceptance_rate}%` : "—"}
          delta={data?.delta_acceptance ?? "—"}
          tone="text-success"
          loading={!data}
        />
        <Kpi
          label="Payone uplift (sim.)"
          value={data ? `€${data.payone_uplift.toFixed(0)}` : "—"}
          delta={data?.delta_uplift ?? "—"}
          tone="text-success"
          loading={!data}
        />
      </div>

      <div className="mt-6 grid grid-cols-1 gap-6 lg:grid-cols-2">
        <ChartCard title="Offers vs accepted, by hour" subtitle="Today · live">
          {data ? (
            <ResponsiveContainer width="100%" height={240}>
              <BarChart data={data.hourly_data}>
                <CartesianGrid strokeDasharray="3 3" stroke="oklch(0.92 0.008 90)" />
                <XAxis dataKey="h" stroke="oklch(0.5 0.02 270)" fontSize={11} />
                <YAxis stroke="oklch(0.5 0.02 270)" fontSize={11} />
                <Tooltip
                  contentStyle={{ borderRadius: 12, border: "1px solid oklch(0.92 0.008 90)", fontSize: 12 }}
                />
                <Bar dataKey="offers" name="Generated" fill="oklch(0.85 0.05 240)" radius={[6, 6, 0, 0]} />
                <Bar dataKey="accepted" name="Accepted" fill="oklch(0.5 0.13 240)" radius={[6, 6, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <LoadingChart />
          )}
        </ChartCard>

        <ChartCard title="Payone uplift trend" subtitle="Last 7 days · sim.">
          {data ? (
            <ResponsiveContainer width="100%" height={240}>
              <LineChart data={data.weekly_uplift}>
                <CartesianGrid strokeDasharray="3 3" stroke="oklch(0.92 0.008 90)" />
                <XAxis dataKey="d" stroke="oklch(0.5 0.02 270)" fontSize={11} />
                <YAxis stroke="oklch(0.5 0.02 270)" fontSize={11} />
                <Tooltip
                  contentStyle={{ borderRadius: 12, border: "1px solid oklch(0.92 0.008 90)", fontSize: 12 }}
                  formatter={(v: number) => [`€${v}`, "Uplift"]}
                />
                <Line
                  type="monotone"
                  dataKey="uplift"
                  stroke="oklch(0.55 0.16 145)"
                  strokeWidth={2.5}
                  dot={{ r: 4, strokeWidth: 0, fill: "oklch(0.55 0.16 145)" }}
                />
              </LineChart>
            </ResponsiveContainer>
          ) : (
            <LoadingChart />
          )}
        </ChartCard>
      </div>

      <div className="mt-6 rounded-3xl border border-border/60 bg-card p-5 shadow-[var(--shadow-soft)]">
        <div className="mb-1 flex items-center justify-between">
          <h2 className="text-base font-semibold">Top-performing offer templates</h2>
          <span className="text-[11px] text-muted-foreground">refreshes every 15 s</span>
        </div>
        {data ? (
          <div className="mt-4 overflow-hidden rounded-2xl border border-border/60">
            <table className="w-full text-left text-sm">
              <thead className="bg-muted/60 text-[11px] uppercase tracking-widest text-muted-foreground">
                <tr>
                  <th className="px-4 py-2.5 font-semibold">Template</th>
                  <th className="px-4 py-2.5 font-semibold">Sent</th>
                  <th className="px-4 py-2.5 font-semibold">Accepted</th>
                  <th className="px-4 py-2.5 font-semibold">Rate</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border/60">
                {data.top_templates.map((row, i) => (
                  <tr key={i}>
                    <td className="px-4 py-3 font-medium">{row.template}</td>
                    <td className="px-4 py-3 text-muted-foreground">{row.sent}</td>
                    <td className="px-4 py-3 text-muted-foreground">{row.accepted}</td>
                    <td className="px-4 py-3 font-semibold text-success">{row.rate}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="mt-4 flex items-center gap-2 text-muted-foreground">
            <Loader2 className="h-4 w-4 animate-spin" />
            Loading analytics…
          </div>
        )}
      </div>
    </div>
  );
}

function Kpi({
  label,
  value,
  delta,
  tone = "text-foreground",
  loading,
}: {
  label: string;
  value: string;
  delta: string;
  tone?: string;
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

function ChartCard({
  title,
  subtitle,
  children,
}: {
  title: string;
  subtitle: string;
  children: React.ReactNode;
}) {
  return (
    <div className="rounded-3xl border border-border/60 bg-card p-5 shadow-[var(--shadow-soft)]">
      <div className="mb-3 flex items-end justify-between">
        <h2 className="text-base font-semibold">{title}</h2>
        <span className="text-[11px] text-muted-foreground">{subtitle}</span>
      </div>
      {children}
    </div>
  );
}

function LoadingChart() {
  return (
    <div className="flex h-60 items-center justify-center">
      <Loader2 className="h-5 w-5 animate-spin text-muted-foreground" />
    </div>
  );
}

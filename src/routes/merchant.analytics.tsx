import { createFileRoute } from "@tanstack/react-router";
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

export const Route = createFileRoute("/merchant/analytics")({
  head: () => ({
    meta: [
      { title: "Analytics — Merchant · City-Wallet" },
      {
        name: "description",
        content:
          "Acceptance rate, hourly performance and Payone transaction uplift trends.",
      },
    ],
  }),
  component: AnalyticsPage,
});

const HOURLY = [
  { h: "08", offers: 12, accepted: 5 },
  { h: "10", offers: 18, accepted: 7 },
  { h: "12", offers: 36, accepted: 16 },
  { h: "14", offers: 42, accepted: 18 },
  { h: "15", offers: 38, accepted: 17 },
  { h: "16", offers: 48, accepted: 21 },
  { h: "17", offers: 28, accepted: 11 },
  { h: "18", offers: 25, accepted: 9 },
];

const TREND = [
  { d: "Mon", uplift: 180 },
  { d: "Tue", uplift: 210 },
  { d: "Wed", uplift: 240 },
  { d: "Thu", uplift: 220 },
  { d: "Fri", uplift: 285 },
  { d: "Sat", uplift: 305 },
  { d: "Sun", uplift: 312 },
];

const TOP = [
  { template: "Cold rain → Cappuccino −30%", sent: 86, accepted: 41, rate: "47.6%" },
  { template: "Quiet hours → Croissant −20%", sent: 64, accepted: 22, rate: "34.4%" },
  { template: "Foot traffic → Flat white −25%", sent: 52, accepted: 19, rate: "36.5%" },
  { template: "End-of-day → Pastry −40%", sent: 45, accepted: 12, rate: "26.7%" },
];

function AnalyticsPage() {
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
        <Kpi label="Offers generated today" value="247" delta="+12% vs yesterday" />
        <Kpi
          label="Acceptance rate"
          value="38.2%"
          delta="+4.1 pp"
          tone="text-success"
        />
        <Kpi
          label="Payone uplift (sim.)"
          value="€312"
          delta="▲ 22% week-on-week"
          tone="text-success"
        />
      </div>

      <div className="mt-6 grid grid-cols-1 gap-6 lg:grid-cols-2">
        <ChartCard
          title="Offers vs accepted, by hour"
          subtitle="Today"
        >
          <ResponsiveContainer width="100%" height={240}>
            <BarChart data={HOURLY}>
              <CartesianGrid strokeDasharray="3 3" stroke="oklch(0.92 0.008 90)" />
              <XAxis dataKey="h" stroke="oklch(0.5 0.02 270)" fontSize={11} />
              <YAxis stroke="oklch(0.5 0.02 270)" fontSize={11} />
              <Tooltip
                contentStyle={{
                  borderRadius: 12,
                  border: "1px solid oklch(0.92 0.008 90)",
                  fontSize: 12,
                }}
              />
              <Bar
                dataKey="offers"
                fill="oklch(0.85 0.05 240)"
                radius={[6, 6, 0, 0]}
              />
              <Bar
                dataKey="accepted"
                fill="oklch(0.5 0.13 240)"
                radius={[6, 6, 0, 0]}
              />
            </BarChart>
          </ResponsiveContainer>
        </ChartCard>

        <ChartCard title="Payone uplift trend" subtitle="Last 7 days">
          <ResponsiveContainer width="100%" height={240}>
            <LineChart data={TREND}>
              <CartesianGrid strokeDasharray="3 3" stroke="oklch(0.92 0.008 90)" />
              <XAxis dataKey="d" stroke="oklch(0.5 0.02 270)" fontSize={11} />
              <YAxis stroke="oklch(0.5 0.02 270)" fontSize={11} />
              <Tooltip
                contentStyle={{
                  borderRadius: 12,
                  border: "1px solid oklch(0.92 0.008 90)",
                  fontSize: 12,
                }}
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
        </ChartCard>
      </div>

      <div className="mt-6 rounded-3xl border border-border/60 bg-card p-5 shadow-[var(--shadow-soft)]">
        <h2 className="text-base font-semibold">Top-performing offer templates</h2>
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
              {TOP.map((row, i) => (
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
      </div>
    </div>
  );
}

function Kpi({
  label,
  value,
  delta,
  tone = "text-foreground",
}: {
  label: string;
  value: string;
  delta: string;
  tone?: string;
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

import { createFileRoute } from "@tanstack/react-router";
import { useState } from "react";
import { Coffee, Croissant, Sparkles, Soup, GlassWater } from "lucide-react";
import { Slider } from "../components/ui/slider";
import { Switch } from "../components/ui/switch";
import { Input } from "../components/ui/input";
import { Label } from "../components/ui/label";
import { toast } from "sonner";
import { Toaster } from "../components/ui/sonner";

export const Route = createFileRoute("/merchant/campaigns")({
  head: () => ({
    meta: [
      { title: "Campaigns & Rules — Merchant · City-Wallet" },
      {
        name: "description",
        content:
          "Tune the generative offer engine: quiet hours, max discount, goal and triggers.",
      },
    ],
  }),
  component: CampaignsPage,
});

const GOALS = [
  { key: "traffic", label: "Drive foot traffic" },
  { key: "inventory", label: "Clear inventory" },
  { key: "loyalty", label: "Boost loyalty" },
] as const;

const FOCUS = [
  { key: "coffee", label: "Coffee", icon: Coffee },
  { key: "pastries", label: "Pastries", icon: Croissant },
  { key: "lunch", label: "Lunch", icon: Soup },
  { key: "cold", label: "Cold drinks", icon: GlassWater },
] as const;

function CampaignsPage() {
  const [quietStart, setQuietStart] = useState("14:00");
  const [quietEnd, setQuietEnd] = useState("17:00");
  const [maxDiscount, setMaxDiscount] = useState(30);
  const [goal, setGoal] = useState<(typeof GOALS)[number]["key"]>("traffic");
  const [focus, setFocus] = useState<string[]>(["coffee", "pastries"]);
  const [weatherBoost, setWeatherBoost] = useState(true);
  const [weekendBoost, setWeekendBoost] = useState(false);
  const [budget, setBudget] = useState("80");

  const toggleFocus = (k: string) =>
    setFocus((f) => (f.includes(k) ? f.filter((x) => x !== k) : [...f, k]));

  const save = () => {
    toast.success("Rules updated", {
      description: "Generative engine will use the new rules immediately.",
    });
  };

  return (
    <div className="mx-auto max-w-6xl">
      <Toaster richColors position="top-right" />
      <div>
        <div className="text-[11px] font-semibold uppercase tracking-widest text-muted-foreground">
          Campaigns
        </div>
        <h1 className="mt-1 text-3xl font-bold tracking-tight">Rules engine</h1>
        <p className="mt-1 text-sm text-muted-foreground">
          Set the constraints. The AI generates and times the offers for you.
        </p>
      </div>

      <div className="mt-6 grid grid-cols-1 gap-6 lg:grid-cols-[1.4fr_1fr]">
        {/* Form */}
        <div className="rounded-3xl border border-border/60 bg-card p-6 shadow-[var(--shadow-soft)]">
          <Section title="Quiet hours" hint="When you want to attract more guests">
            <div className="grid grid-cols-2 gap-3">
              <div>
                <Label className="text-xs text-muted-foreground">From</Label>
                <Input
                  type="time"
                  value={quietStart}
                  onChange={(e) => setQuietStart(e.target.value)}
                  className="mt-1 h-11 rounded-xl"
                />
              </div>
              <div>
                <Label className="text-xs text-muted-foreground">Until</Label>
                <Input
                  type="time"
                  value={quietEnd}
                  onChange={(e) => setQuietEnd(e.target.value)}
                  className="mt-1 h-11 rounded-xl"
                />
              </div>
            </div>
          </Section>

          <Section title="Max discount" hint="Cap on what the engine may offer">
            <div className="flex items-center gap-4">
              <Slider
                value={[maxDiscount]}
                onValueChange={([v]) => setMaxDiscount(v)}
                min={5}
                max={50}
                step={5}
                className="flex-1"
              />
              <div className="w-14 rounded-xl bg-muted px-3 py-1.5 text-center text-sm font-semibold">
                {maxDiscount}%
              </div>
            </div>
          </Section>

          <Section title="Goal">
            <div className="flex flex-wrap gap-2">
              {GOALS.map((g) => (
                <button
                  key={g.key}
                  onClick={() => setGoal(g.key)}
                  className={`rounded-full border px-4 py-2 text-sm font-medium transition-colors ${
                    goal === g.key
                      ? "border-foreground bg-foreground text-background"
                      : "border-border bg-card text-muted-foreground hover:text-foreground"
                  }`}
                >
                  {g.label}
                </button>
              ))}
            </div>
          </Section>

          <Section title="Inventory focus">
            <div className="flex flex-wrap gap-2">
              {FOCUS.map((f) => {
                const active = focus.includes(f.key);
                const Icon = f.icon;
                return (
                  <button
                    key={f.key}
                    onClick={() => toggleFocus(f.key)}
                    className={`inline-flex items-center gap-2 rounded-full border px-3.5 py-2 text-sm font-medium transition-colors ${
                      active
                        ? "border-foreground bg-foreground text-background"
                        : "border-border bg-card text-muted-foreground hover:text-foreground"
                    }`}
                  >
                    <Icon className="h-4 w-4" />
                    {f.label}
                  </button>
                );
              })}
            </div>
          </Section>

          <Section title="Trigger conditions">
            <div className="space-y-3">
              <Toggle
                label="Bad weather boost"
                hint="Push warm offers when rain is forecast"
                value={weatherBoost}
                onChange={setWeatherBoost}
              />
              <Toggle
                label="Weekend boost"
                hint="Increase visibility on Sat & Sun"
                value={weekendBoost}
                onChange={setWeekendBoost}
              />
            </div>
          </Section>

          <Section title="Daily budget cap (€)">
            <Input
              type="number"
              value={budget}
              onChange={(e) => setBudget(e.target.value)}
              className="h-11 max-w-[160px] rounded-xl"
            />
          </Section>

          <div className="mt-6 flex items-center justify-end gap-3 border-t border-border/60 pt-4">
            <button className="rounded-full px-4 py-2 text-sm text-muted-foreground hover:text-foreground">
              Cancel
            </button>
            <button
              onClick={save}
              className="rounded-full bg-foreground px-5 py-2 text-sm font-semibold text-background"
            >
              Save rules
            </button>
          </div>
        </div>

        {/* Live preview */}
        <div className="space-y-4">
          <div className="rounded-3xl border border-dashed border-border bg-card/60 p-4">
            <div className="mb-3 flex items-center gap-1.5 text-[10px] font-semibold uppercase tracking-widest text-muted-foreground">
              <Sparkles className="h-3 w-3" />
              Live preview
            </div>

            <div className="overflow-hidden rounded-3xl bg-gradient-to-br from-[oklch(0.92_0.04_240)] via-[oklch(0.95_0.03_260)] to-[oklch(0.97_0.02_280)] p-5 shadow-[var(--shadow-card)]">
              <div className="flex items-start justify-between">
                <div className="flex items-center gap-2.5">
                  <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-white/70 text-xl">
                    ☕
                  </div>
                  <div className="leading-tight">
                    <div className="text-[13px] font-semibold">Café Königsbau</div>
                    <div className="text-[10px] text-foreground/60">
                      {quietStart}–{quietEnd} window
                    </div>
                  </div>
                </div>
                <div className="text-right text-[oklch(0.4_0.12_250)]">
                  <div className="text-2xl font-extrabold leading-none">
                    −{maxDiscount}%
                  </div>
                  <div className="text-[10px] uppercase tracking-widest opacity-70">
                    cap
                  </div>
                </div>
              </div>
              <h3 className="mt-4 text-[18px] font-bold leading-[1.2] tracking-tight">
                {goal === "traffic" &&
                  "Cold outside? Your cappuccino is waiting 80 m away."}
                {goal === "inventory" &&
                  "Last batch of croissants — fresh, half off."}
                {goal === "loyalty" &&
                  "For our regulars: a free shot in your next flat white."}
              </h3>
              <p className="mt-2 text-[12px] text-foreground/70">
                Triggers: {weatherBoost ? "weather " : ""}
                {weekendBoost ? "weekend " : ""}
                {!weatherBoost && !weekendBoost ? "quiet-hours only" : ""}
              </p>
              <div className="mt-3 text-[11px] text-foreground/60">
                Focus: {focus.length ? focus.join(", ") : "all categories"}
              </div>
            </div>
          </div>

          <div className="rounded-3xl border border-border/60 bg-card p-4 text-[12px] text-muted-foreground">
            <div className="mb-1 text-[11px] font-semibold uppercase tracking-widest text-foreground">
              Estimated reach
            </div>
            ~{Math.round(40 + maxDiscount * 4 + (weatherBoost ? 30 : 0))} eligible
            users in the next quiet-hour window.
          </div>
        </div>
      </div>
    </div>
  );
}

function Section({
  title,
  hint,
  children,
}: {
  title: string;
  hint?: string;
  children: React.ReactNode;
}) {
  return (
    <div className="border-b border-border/60 py-5 first:pt-0 last:border-0 last:pb-0">
      <div className="mb-3">
        <div className="text-sm font-semibold">{title}</div>
        {hint && <div className="text-[12px] text-muted-foreground">{hint}</div>}
      </div>
      {children}
    </div>
  );
}

function Toggle({
  label,
  hint,
  value,
  onChange,
}: {
  label: string;
  hint: string;
  value: boolean;
  onChange: (v: boolean) => void;
}) {
  return (
    <label className="flex cursor-pointer items-center justify-between rounded-2xl border border-border/60 bg-background px-4 py-3">
      <div className="leading-tight">
        <div className="text-sm font-medium">{label}</div>
        <div className="text-[11px] text-muted-foreground">{hint}</div>
      </div>
      <Switch checked={value} onCheckedChange={onChange} />
    </label>
  );
}

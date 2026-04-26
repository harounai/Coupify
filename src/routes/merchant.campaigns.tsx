import { createFileRoute } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { Coffee, Croissant, Sparkles, Soup, GlassWater, Loader2 } from "lucide-react";
import { Slider } from "../components/ui/slider";
import { Switch } from "../components/ui/switch";
import { Input } from "../components/ui/input";
import { Label } from "../components/ui/label";
import { toast } from "sonner";
import { Toaster } from "../components/ui/sonner";
import { api, type MerchantRules } from "../lib/api";

export const Route = createFileRoute("/merchant/campaigns")({
  head: () => ({
    meta: [
      { title: "Campaigns & Rules — Merchant · City-Wallet" },
      {
        name: "description",
        content: "Tune the generative offer engine: quiet hours, max discount, goal and triggers.",
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

const FOCUS_OPTIONS = [
  { key: "coffee", label: "Coffee", icon: Coffee },
  { key: "pastries", label: "Pastries", icon: Croissant },
  { key: "lunch", label: "Lunch", icon: Soup },
  { key: "cold", label: "Cold drinks", icon: GlassWater },
] as const;

const DEFAULT_RULES: MerchantRules = {
  merchant_name: "Café Königsbau",
  merchant_emoji: "☕",
  category: "Café · Specialty Coffee",
  quiet_start: "14:00",
  quiet_end: "17:00",
  max_discount: 30,
  goal: "traffic",
  focus: ["coffee", "pastries"],
  weather_boost: true,
  weekend_boost: false,
  daily_budget: 80,
  distance_m: 80,
  typical_price: 4.20,
};

function CampaignsPage() {
  const [rules, setRules] = useState<MerchantRules>(DEFAULT_RULES);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [previewLoading, setPreviewLoading] = useState(false);
  const [previewHeadline, setPreviewHeadline] = useState<string | null>(null);

  useEffect(() => {
    api
      .getRules()
      .then(setRules)
      .catch(() => {/* use defaults */})
      .finally(() => setLoading(false));
  }, []);

  const patch = (partial: Partial<MerchantRules>) =>
    setRules((r) => ({ ...r, ...partial }));

  const toggleFocus = (k: string) =>
    patch({ focus: rules.focus.includes(k) ? rules.focus.filter((x) => x !== k) : [...rules.focus, k] });

  const save = async () => {
    setSaving(true);
    try {
      await api.saveRules(rules);
      toast.success("Rules saved", {
        description: "Generative engine will use the new rules immediately.",
      });
    } catch {
      toast.error("Could not reach backend — rules saved locally only.");
    } finally {
      setSaving(false);
    }
  };

  const generatePreview = async () => {
    setPreviewLoading(true);
    setPreviewHeadline(null);
    try {
      await api.saveRules(rules);
      const offer = await api.generateOffer({ rules });
      setPreviewHeadline(offer.headline);
    } catch {
      setPreviewHeadline("Backend offline — start the server to see live previews.");
    } finally {
      setPreviewLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex h-64 items-center justify-center">
        <Loader2 className="h-5 w-5 animate-spin text-muted-foreground" />
      </div>
    );
  }

  const estimated = Math.round(40 + rules.max_discount * 4 + (rules.weather_boost ? 30 : 0));

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
          <Section title="Merchant">
            <div className="grid grid-cols-2 gap-3">
              <div>
                <Label className="text-xs text-muted-foreground">Name</Label>
                <Input
                  value={rules.merchant_name}
                  onChange={(e) => patch({ merchant_name: e.target.value })}
                  className="mt-1 h-11 rounded-xl"
                />
              </div>
              <div>
                <Label className="text-xs text-muted-foreground">Emoji</Label>
                <Input
                  value={rules.merchant_emoji}
                  onChange={(e) => patch({ merchant_emoji: e.target.value })}
                  className="mt-1 h-11 rounded-xl"
                  maxLength={2}
                />
              </div>
            </div>
            <div className="mt-3 grid grid-cols-2 gap-3">
              <div>
                <Label className="text-xs text-muted-foreground">Typical price (€)</Label>
                <Input
                  type="number"
                  value={rules.typical_price}
                  onChange={(e) => patch({ typical_price: parseFloat(e.target.value) || 4.20 })}
                  className="mt-1 h-11 rounded-xl"
                  step="0.10"
                />
              </div>
              <div>
                <Label className="text-xs text-muted-foreground">Distance from user (m)</Label>
                <Input
                  type="number"
                  value={rules.distance_m}
                  onChange={(e) => patch({ distance_m: parseInt(e.target.value) || 80 })}
                  className="mt-1 h-11 rounded-xl"
                />
              </div>
            </div>
          </Section>

          <Section title="Quiet hours" hint="When you want to attract more guests">
            <div className="grid grid-cols-2 gap-3">
              <div>
                <Label className="text-xs text-muted-foreground">From</Label>
                <Input
                  type="time"
                  value={rules.quiet_start}
                  onChange={(e) => patch({ quiet_start: e.target.value })}
                  className="mt-1 h-11 rounded-xl"
                />
              </div>
              <div>
                <Label className="text-xs text-muted-foreground">Until</Label>
                <Input
                  type="time"
                  value={rules.quiet_end}
                  onChange={(e) => patch({ quiet_end: e.target.value })}
                  className="mt-1 h-11 rounded-xl"
                />
              </div>
            </div>
          </Section>

          <Section title="Max discount" hint="Cap on what the engine may offer">
            <div className="flex items-center gap-4">
              <Slider
                value={[rules.max_discount]}
                onValueChange={([v]) => patch({ max_discount: v })}
                min={5}
                max={50}
                step={5}
                className="flex-1"
              />
              <div className="w-14 rounded-xl bg-muted px-3 py-1.5 text-center text-sm font-semibold">
                {rules.max_discount}%
              </div>
            </div>
          </Section>

          <Section title="Goal">
            <div className="flex flex-wrap gap-2">
              {GOALS.map((g) => (
                <button
                  key={g.key}
                  onClick={() => patch({ goal: g.key })}
                  className={`rounded-full border px-4 py-2 text-sm font-medium transition-colors ${
                    rules.goal === g.key
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
              {FOCUS_OPTIONS.map((f) => {
                const active = rules.focus.includes(f.key);
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
                value={rules.weather_boost}
                onChange={(v) => patch({ weather_boost: v })}
              />
              <Toggle
                label="Weekend boost"
                hint="Increase visibility on Sat & Sun"
                value={rules.weekend_boost}
                onChange={(v) => patch({ weekend_boost: v })}
              />
            </div>
          </Section>

          <Section title="Daily budget cap (€)">
            <Input
              type="number"
              value={rules.daily_budget}
              onChange={(e) => patch({ daily_budget: parseFloat(e.target.value) || 80 })}
              className="h-11 max-w-[160px] rounded-xl"
            />
          </Section>

          <div className="mt-6 flex items-center justify-end gap-3 border-t border-border/60 pt-4">
            <button
              onClick={() => setRules(DEFAULT_RULES)}
              className="rounded-full px-4 py-2 text-sm text-muted-foreground hover:text-foreground"
            >
              Reset
            </button>
            <button
              onClick={save}
              disabled={saving}
              className="inline-flex items-center gap-2 rounded-full bg-foreground px-5 py-2 text-sm font-semibold text-background disabled:opacity-60"
            >
              {saving && <Loader2 className="h-3.5 w-3.5 animate-spin" />}
              Save rules
            </button>
          </div>
        </div>

        {/* Live preview */}
        <div className="space-y-4">
          <div className="rounded-3xl border border-dashed border-border bg-card/60 p-4">
            <div className="mb-3 flex items-center justify-between">
              <div className="flex items-center gap-1.5 text-[10px] font-semibold uppercase tracking-widest text-muted-foreground">
                <Sparkles className="h-3 w-3" />
                AI preview
              </div>
              <button
                onClick={generatePreview}
                disabled={previewLoading}
                className="inline-flex items-center gap-1.5 rounded-full border border-border/60 bg-card px-3 py-1 text-[11px] font-medium text-muted-foreground transition-colors hover:text-foreground disabled:opacity-60"
              >
                {previewLoading ? (
                  <Loader2 className="h-3 w-3 animate-spin" />
                ) : (
                  <Sparkles className="h-3 w-3" />
                )}
                {previewLoading ? "Generating…" : "Generate preview"}
              </button>
            </div>

            <div className="overflow-hidden rounded-3xl bg-gradient-to-br from-[oklch(0.92_0.04_240)] via-[oklch(0.95_0.03_260)] to-[oklch(0.97_0.02_280)] p-5 shadow-[var(--shadow-card)]">
              <div className="flex items-start justify-between">
                <div className="flex items-center gap-2.5">
                  <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-white/70 text-xl">
                    {rules.merchant_emoji}
                  </div>
                  <div className="leading-tight">
                    <div className="text-[13px] font-semibold">{rules.merchant_name}</div>
                    <div className="text-[10px] text-foreground/60">
                      {rules.quiet_start}–{rules.quiet_end} window
                    </div>
                  </div>
                </div>
                <div className="text-right text-[oklch(0.4_0.12_250)]">
                  <div className="text-2xl font-extrabold leading-none">−{rules.max_discount}%</div>
                  <div className="text-[10px] uppercase tracking-widest opacity-70">cap</div>
                </div>
              </div>
              <h3 className="mt-4 text-[17px] font-bold leading-[1.2] tracking-tight">
                {previewLoading ? (
                  <span className="inline-block h-5 w-3/4 animate-pulse rounded-full bg-foreground/10" />
                ) : previewHeadline ? (
                  previewHeadline
                ) : rules.goal === "traffic" ? (
                  "Cold outside? Your cappuccino is waiting 80 m away."
                ) : rules.goal === "inventory" ? (
                  "Last batch of croissants — fresh, half off."
                ) : (
                  "For our regulars: a free shot in your next flat white."
                )}
              </h3>
              <p className="mt-2 text-[12px] text-foreground/70">
                Triggers:{" "}
                {[rules.weather_boost && "weather", rules.weekend_boost && "weekend"]
                  .filter(Boolean)
                  .join(", ") || "quiet-hours only"}
              </p>
              <div className="mt-3 text-[11px] text-foreground/60">
                Focus: {rules.focus.length ? rules.focus.join(", ") : "all categories"}
              </div>
            </div>
          </div>

          <div className="rounded-3xl border border-border/60 bg-card p-4 text-[12px] text-muted-foreground">
            <div className="mb-1 text-[11px] font-semibold uppercase tracking-widest text-foreground">
              Estimated reach
            </div>
            ~{estimated} eligible users in the next quiet-hour window.
          </div>

          <div className="rounded-3xl border border-border/60 bg-card p-4 text-[12px] text-muted-foreground">
            <div className="mb-1 text-[11px] font-semibold uppercase tracking-widest text-foreground">
              How it works
            </div>
            <p>
              You set the rules above. The AI engine reads the live context (weather, time,
              transaction signal) and writes a tailored offer within your constraints — no templates.
            </p>
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

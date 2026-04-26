const BASE = import.meta.env.VITE_API_URL ?? "http://localhost:8000";

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${BASE}${path}`, {
    headers: { "Content-Type": "application/json", ...init?.headers },
    ...init,
  });
  if (!res.ok) throw new Error(`API ${path} → ${res.status}`);
  return res.json() as Promise<T>;
}

// ── Types ─────────────────────────────────────────────────────────────────────

export type WeatherCtx = {
  temp_c: number;
  description: string;
  icon: string;
  weather_type: string;
  precipitation_mm: number;
};

export type TimeCtx = {
  hour: number;
  period: string;
  day_of_week: string;
  is_weekend: boolean;
};

export type ContextState = {
  weather: WeatherCtx;
  time: TimeCtx;
  location: { city: string; district: string; latitude: number; longitude: number };
  composite_state: string[];
  context_label: string;
};

export type MerchantRules = {
  merchant_name: string;
  merchant_emoji: string;
  category: string;
  quiet_start: string;
  quiet_end: string;
  max_discount: number;
  goal: "traffic" | "inventory" | "loyalty";
  focus: string[];
  weather_boost: boolean;
  weekend_boost: boolean;
  daily_budget: number;
  distance_m: number;
  typical_price: number;
};

export type GeneratedOffer = {
  id: string;
  merchant: string;
  merchant_emoji: string;
  category: string;
  distance_m: number;
  headline: string;
  subline: string;
  discount: number;
  original_price: number;
  final_price: number;
  reasoning: string;
  valid_seconds: number;
  emoji: string;
  theme: "blue" | "amber" | "green" | "rose" | "purple";
  bonus_item?: string | null;
  token: string;
  generated_at: string;
};

export type RedeemPayload = {
  offer_id: string;
  token: string;
  merchant: string;
  discount: number;
  original_price: number;
  final_price: number;
};

export type AnalyticsData = {
  offers_generated_today: number;
  acceptance_rate: number;
  payone_uplift: number;
  delta_offers: string;
  delta_acceptance: string;
  delta_uplift: string;
  recent_offers: Array<{
    title: string;
    context: string;
    time: string;
    discount: number;
    status: string;
  }>;
  hourly_data: Array<{ h: string; offers: number; accepted: number }>;
  weekly_uplift: Array<{ d: string; uplift: number }>;
  top_templates: Array<{ template: string; sent: number; accepted: number; rate: string }>;
};

// ── API calls ─────────────────────────────────────────────────────────────────

export const api = {
  health: () => request<{ status: string }>("/health"),

  getContext: () => request<ContextState>("/context"),

  getRules: () => request<MerchantRules>("/merchant/rules"),

  saveRules: (rules: MerchantRules) =>
    request<MerchantRules>("/merchant/rules", {
      method: "POST",
      body: JSON.stringify(rules),
    }),

  generateOffer: (payload: { context?: ContextState | object; rules?: MerchantRules }) =>
    request<GeneratedOffer>("/offers/generate", {
      method: "POST",
      body: JSON.stringify(payload),
    }),

  redeemOffer: (payload: RedeemPayload) =>
    request<{ success: boolean; cashback: number; message: string }>("/offers/redeem", {
      method: "POST",
      body: JSON.stringify(payload),
    }),

  getAnalytics: () => request<AnalyticsData>("/merchant/analytics"),
};

// ── Scenario contexts (passed to backend for demo mode) ────────────────────────

export const SCENARIO_CONTEXTS: Record<string, object> = {
  "cold-rain": {
    weather: { temp_c: 7, description: "Light rain", icon: "🌧️", weather_type: "rainy", precipitation_mm: 2.1 },
    time: { hour: 14, period: "afternoon", day_of_week: "Tuesday", is_weekend: false },
    location: { city: "Stuttgart", district: "Mitte", latitude: 48.7758, longitude: 9.1829 },
    composite_state: ["cold", "rainy", "afternoon"],
    context_label: "Cold & Rainy · Tuesday Afternoon",
  },
  "sunny-warm": {
    weather: { temp_c: 24, description: "Clear sky", icon: "☀️", weather_type: "sunny", precipitation_mm: 0 },
    time: { hour: 17, period: "afternoon", day_of_week: "Friday", is_weekend: false },
    location: { city: "Stuttgart", district: "Mitte", latitude: 48.7758, longitude: 9.1829 },
    composite_state: ["warm", "sunny", "afternoon"],
    context_label: "Warm & Sunny · Friday Afternoon",
  },
  "cold-evening": {
    weather: { temp_c: 4, description: "Clear", icon: "🌙", weather_type: "clear", precipitation_mm: 0 },
    time: { hour: 19, period: "evening", day_of_week: "Thursday", is_weekend: false },
    location: { city: "Stuttgart", district: "Mitte", latitude: 48.7758, longitude: 9.1829 },
    composite_state: ["cold", "clear", "evening"],
    context_label: "Cold & Clear · Thursday Evening",
  },
  "lunch-rush": {
    weather: { temp_c: 18, description: "Partly cloudy", icon: "⛅", weather_type: "cloudy", precipitation_mm: 0 },
    time: { hour: 12, period: "lunch", day_of_week: "Wednesday", is_weekend: false },
    location: { city: "Stuttgart", district: "Mitte", latitude: 48.7758, longitude: 9.1829 },
    composite_state: ["cool", "cloudy", "lunch_rush"],
    context_label: "Partly Cloudy · Wednesday Lunch",
  },
};

// ── Theme → Tailwind gradient mapping ────────────────────────────────────────

export const THEME_CONFIG = {
  blue: {
    gradient: "from-[oklch(0.92_0.04_240)] via-[oklch(0.95_0.03_260)] to-[oklch(0.97_0.02_280)]",
    accent: "text-[oklch(0.4_0.12_250)]",
  },
  amber: {
    gradient: "from-[oklch(0.93_0.07_85)] via-[oklch(0.95_0.06_60)] to-[oklch(0.96_0.05_40)]",
    accent: "text-[oklch(0.4_0.13_60)]",
  },
  green: {
    gradient: "from-[oklch(0.94_0.06_150)] via-[oklch(0.96_0.05_130)] to-[oklch(0.97_0.03_110)]",
    accent: "text-[oklch(0.4_0.13_145)]",
  },
  rose: {
    gradient: "from-[oklch(0.88_0.07_350)] via-[oklch(0.92_0.06_330)] to-[oklch(0.95_0.04_310)]",
    accent: "text-[oklch(0.38_0.13_350)]",
  },
  purple: {
    gradient: "from-[oklch(0.92_0.05_300)] via-[oklch(0.94_0.04_280)] to-[oklch(0.96_0.03_260)]",
    accent: "text-[oklch(0.4_0.12_300)]",
  },
} as const;

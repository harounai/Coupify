export type Scenario = {
  key: string;
  label: string;
  weather: string;
  time: string;
  emoji: string;
  offers: Offer[];
};

export type Offer = {
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
  gradient: string; // tailwind classes
  accent: string; // tailwind text color
  emoji: string;
  bonusItem?: string; // optional add-on for "Bonus" spin outcome
};

export const SCENARIOS: Scenario[] = [
  {
    key: "cold-rain",
    label: "Cold & Raining",
    weather: "7°C, light rain",
    time: "Tuesday · 14:00",
    emoji: "🌧️",
    offers: [
      {
        id: "cold-rain-1", bonusItem: "free oat-milk shot",
        merchant: "Café Königsbau",
        category: "Café · Specialty Coffee",
        distanceM: 80,
        headline: "Cold outside? Your cappuccino is waiting 80 m away.",
        subline: "House-roasted beans, oat milk on the house.",
        discount: 30,
        originalPrice: 4.2,
        finalPrice: 2.94,
        reasoning: "Generated for you: rain detected + Café Königsbau is in quiet hours.",
        validSeconds: 15 * 60,
        gradient: "from-[oklch(0.92_0.04_240)] via-[oklch(0.95_0.03_260)] to-[oklch(0.97_0.02_280)]",
        accent: "text-[oklch(0.4_0.12_250)]",
        emoji: "☕",
      },
      {
        id: "cold-rain-2", bonusItem: "free Apfelschorle",
        merchant: "Maultaschen Manufaktur",
        category: "Schwäbisch · Comfort food",
        distanceM: 220,
        headline: "Warm up with house Maultaschen.",
        subline: "Hand-folded today. Brodelnd heiß serviert.",
        discount: 20,
        originalPrice: 12.5,
        finalPrice: 10.0,
        reasoning: "Generated for you: cold weather + your lunch pattern matches.",
        validSeconds: 25 * 60,
        gradient: "from-[oklch(0.9_0.05_30)] via-[oklch(0.93_0.04_50)] to-[oklch(0.96_0.03_70)]",
        accent: "text-[oklch(0.4_0.12_30)]",
        emoji: "🥟",
      },
    ],
  },
  {
    key: "sunny-warm",
    label: "Sunny & Warm",
    weather: "24°C, clear skies",
    time: "Friday · 17:00",
    emoji: "☀️",
    offers: [
      {
        id: "sunny-1", bonusItem: "free pretzel side",
        merchant: "Biergarten am Schlossplatz",
        category: "Biergarten · Outdoor",
        distanceM: 140,
        headline: "Sun's out. First Radler on us.",
        subline: "Schattenplatz reserved for the next 20 minutes.",
        discount: 50,
        originalPrice: 5.6,
        finalPrice: 2.8,
        reasoning: "Generated for you: sunny + Friday afterwork window.",
        validSeconds: 20 * 60,
        gradient: "from-[oklch(0.93_0.07_85)] via-[oklch(0.95_0.06_60)] to-[oklch(0.96_0.05_40)]",
        accent: "text-[oklch(0.4_0.13_60)]",
        emoji: "🍺",
      },
      {
        id: "sunny-2", bonusItem: "extra scoop",
        merchant: "Eiscafé Pinguin",
        category: "Gelato · Hand-made",
        distanceM: 60,
        headline: "Two scoops, one price. Right around the corner.",
        subline: "Pistazie & Stracciatella, freshly churned.",
        discount: 40,
        originalPrice: 4.8,
        finalPrice: 2.88,
        reasoning: "Generated for you: warm afternoon + foot traffic boost.",
        validSeconds: 12 * 60,
        gradient: "from-[oklch(0.94_0.05_180)] via-[oklch(0.95_0.04_150)] to-[oklch(0.97_0.03_120)]",
        accent: "text-[oklch(0.4_0.12_175)]",
        emoji: "🍨",
      },
    ],
  },
  {
    key: "cold-evening",
    label: "Cold Evening",
    weather: "4°C, clear",
    time: "Thursday · 19:00",
    emoji: "🌙",
    offers: [
      {
        id: "evening-1", bonusItem: "free olive plate",
        merchant: "Weinstube Fröhlich",
        category: "Weinstube · Local",
        distanceM: 310,
        headline: "Trollinger by the glass — half off until 20:00.",
        subline: "Cosy back room, candle-lit. Save your spot.",
        discount: 50,
        originalPrice: 6.5,
        finalPrice: 3.25,
        reasoning: "Generated for you: cold evening + dine-out preference.",
        validSeconds: 30 * 60,
        gradient: "from-[oklch(0.88_0.07_350)] via-[oklch(0.92_0.06_330)] to-[oklch(0.95_0.04_310)]",
        accent: "text-[oklch(0.38_0.13_350)]",
        emoji: "🍷",
      },
      {
        id: "evening-2", bonusItem: "+1 free pretzel",
        merchant: "Brezel Bub",
        category: "Bakery · Late hours",
        distanceM: 95,
        headline: "Last batch of butter pretzels. €1 each.",
        subline: "Out of the oven 12 minutes ago.",
        discount: 50,
        originalPrice: 2.0,
        finalPrice: 1.0,
        reasoning: "Generated for you: end-of-day inventory + you're nearby.",
        validSeconds: 18 * 60,
        gradient: "from-[oklch(0.93_0.05_70)] via-[oklch(0.95_0.04_60)] to-[oklch(0.96_0.03_50)]",
        accent: "text-[oklch(0.4_0.12_60)]",
        emoji: "🥨",
      },
    ],
  },
  {
    key: "lunch-rush",
    label: "Lunch Rush",
    weather: "18°C, partly cloudy",
    time: "Wednesday · 12:30",
    emoji: "🥗",
    offers: [
      {
        id: "lunch-1", bonusItem: "free kombucha",
        merchant: "Bowls & Bites",
        category: "Healthy · Order ahead",
        distanceM: 180,
        headline: "Skip the queue — order ahead, save 15%.",
        subline: "Ready in 8 minutes when you arrive.",
        discount: 15,
        originalPrice: 11.9,
        finalPrice: 10.12,
        reasoning: "Generated for you: midday + you usually eat near Charlottenplatz.",
        validSeconds: 10 * 60,
        gradient: "from-[oklch(0.94_0.06_150)] via-[oklch(0.96_0.05_130)] to-[oklch(0.97_0.03_110)]",
        accent: "text-[oklch(0.4_0.13_145)]",
        emoji: "🥗",
      },
      {
        id: "lunch-2", bonusItem: "free ayran",
        merchant: "Döner Palast",
        category: "Street food · Quick",
        distanceM: 240,
        headline: "Mittagsmenü: Döner + drink for €7.50.",
        subline: "Lamb, beef, or veggie. Five-minute prep.",
        discount: 25,
        originalPrice: 10.0,
        finalPrice: 7.5,
        reasoning: "Generated for you: lunch hour + value-oriented basket.",
        validSeconds: 15 * 60,
        gradient: "from-[oklch(0.92_0.06_25)] via-[oklch(0.94_0.05_45)] to-[oklch(0.96_0.04_65)]",
        accent: "text-[oklch(0.42_0.14_30)]",
        emoji: "🌯",
      },
    ],
  },
];

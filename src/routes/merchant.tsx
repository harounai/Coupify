import { createFileRoute, Link, Outlet, useLocation } from "@tanstack/react-router";
import { BarChart3, LayoutDashboard, Settings2, Store } from "lucide-react";

export const Route = createFileRoute("/merchant")({
  head: () => ({
    meta: [
      { title: "Merchant Dashboard — City-Wallet" },
      {
        name: "description",
        content:
          "Set rules, monitor offers, and track uplift for your local Stuttgart business.",
      },
      { property: "og:title", content: "City-Wallet — Merchant Dashboard" },
      {
        property: "og:description",
        content: "Configure your generative offer rules and track real-time performance.",
      },
    ],
  }),
  component: MerchantLayout,
});

const NAV = [
  { to: "/merchant", label: "Overview", icon: LayoutDashboard, exact: true },
  { to: "/merchant/campaigns", label: "Campaigns", icon: Settings2, exact: false },
  { to: "/merchant/analytics", label: "Analytics", icon: BarChart3, exact: false },
] as const;

function MerchantLayout() {
  const { pathname } = useLocation();

  return (
    <div className="flex flex-1">
      <aside className="hidden w-64 shrink-0 border-r border-border/60 bg-sidebar px-4 py-6 md:block">
        <div className="flex items-center gap-2.5 rounded-2xl border border-border/60 bg-card p-3 shadow-[var(--shadow-soft)]">
          <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-foreground text-background">
            <Store className="h-4 w-4" />
          </div>
          <div className="leading-tight">
            <div className="text-sm font-semibold">Café Königsbau</div>
            <div className="text-[11px] text-muted-foreground">Stuttgart-Mitte</div>
          </div>
        </div>

        <nav className="mt-6 space-y-1">
          {NAV.map((item) => {
            const active = item.exact
              ? pathname === item.to
              : pathname.startsWith(item.to) && item.to !== "/merchant";
            const Icon = item.icon;
            return (
              <Link
                key={item.to}
                to={item.to}
                className={`flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition-colors ${
                  active
                    ? "bg-foreground text-background"
                    : "text-muted-foreground hover:bg-muted hover:text-foreground"
                }`}
              >
                <Icon className="h-4 w-4" />
                {item.label}
              </Link>
            );
          })}
        </nav>

        <div className="mt-6 rounded-2xl border border-border/60 bg-card p-3 text-[11px] text-muted-foreground">
          Connected to Payone. Live orders are simulated for this demo.
        </div>
      </aside>

      {/* mobile nav */}
      <div className="md:hidden fixed bottom-4 left-1/2 z-30 -translate-x-1/2 rounded-full border border-border/60 bg-card p-1 shadow-[var(--shadow-card)]">
        {NAV.map((item) => {
          const active = item.exact
            ? pathname === item.to
            : pathname.startsWith(item.to) && item.to !== "/merchant";
          return (
            <Link
              key={item.to}
              to={item.to}
              className={`inline-flex items-center rounded-full px-3 py-1.5 text-xs font-medium ${
                active ? "bg-foreground text-background" : "text-muted-foreground"
              }`}
            >
              {item.label}
            </Link>
          );
        })}
      </div>

      <main className="flex-1 px-4 py-6 sm:px-8 sm:py-10">
        <Outlet />
      </main>
    </div>
  );
}

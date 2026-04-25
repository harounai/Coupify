import { Link, useLocation } from "@tanstack/react-router";
import { Sparkles } from "lucide-react";

export function TopNav() {
  const { pathname } = useLocation();
  const isMerchant = pathname.startsWith("/merchant");

  return (
    <header className="sticky top-0 z-40 w-full border-b border-border/60 bg-background/80 backdrop-blur-xl">
      <div className="mx-auto flex h-14 w-full max-w-7xl items-center justify-between px-4 sm:px-6">
        <Link to="/wallet" className="flex items-center gap-2">
          <div className="flex h-8 w-8 items-center justify-center rounded-xl bg-foreground text-background">
            <Sparkles className="h-4 w-4" />
          </div>
          <div className="leading-tight">
            <div className="text-sm font-semibold tracking-tight">City-Wallet</div>
            <div className="text-[10px] uppercase tracking-widest text-muted-foreground">
              Stuttgart
            </div>
          </div>
        </Link>

        <nav className="flex items-center gap-1 rounded-full border border-border/60 bg-card p-1 shadow-[var(--shadow-soft)]">
          <Link
            to="/wallet"
            className={`rounded-full px-4 py-1.5 text-sm font-medium transition-colors ${
              !isMerchant
                ? "bg-foreground text-background"
                : "text-muted-foreground hover:text-foreground"
            }`}
          >
            Consumer
          </Link>
          <Link
            to="/merchant"
            className={`rounded-full px-4 py-1.5 text-sm font-medium transition-colors ${
              isMerchant
                ? "bg-foreground text-background"
                : "text-muted-foreground hover:text-foreground"
            }`}
          >
            Merchant
          </Link>
        </nav>

        <div className="hidden text-xs text-muted-foreground sm:block">
          Demo · v0.1
        </div>
      </div>
    </header>
  );
}

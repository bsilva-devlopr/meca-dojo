"use client";

import { LogoutButton } from "@/app/app/LogoutButton";
import { type MeResponse } from "@/src/lib/auth/types";
import { cn } from "@/src/lib/utils/cn";
import { BookOpen, Building2, CalendarDays, GraduationCap, LayoutDashboard, MapPin, MoreHorizontal, UsersRound } from "lucide-react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { type ReactNode } from "react";

const navItems = [
  { href: "/app", label: "Dashboard", icon: LayoutDashboard, exact: true },
  { href: "/app/schedule", label: "Agenda", icon: CalendarDays },
  { href: "/app/classes", label: "Aulas", icon: BookOpen },
  { href: "/app/students", label: "Alunos", icon: GraduationCap },
  { href: "/app/staff", label: "Equipe", icon: UsersRound },
  { href: "/app/units", label: "Unidades", icon: MapPin },
];

const mobileNavItems = [
  { href: "/app", label: "Inicio", icon: LayoutDashboard, exact: true },
  { href: "/app/students", label: "Alunos", icon: GraduationCap },
  { href: "/app/schedule", label: "Agenda", icon: CalendarDays },
  { href: "/app/classes", label: "Mais", icon: MoreHorizontal },
];

export function AppChrome({ user, children }: { user: MeResponse; children: ReactNode }) {
  const pathname = usePathname();

  return (
    <main className="min-h-screen bg-background text-foreground">
      <aside className="fixed inset-y-0 left-0 z-30 hidden w-72 border-r border-border bg-foreground text-white lg:flex lg:flex-col">
        <div className="p-6">
          <div className="flex items-center gap-3">
            <div className="grid h-11 w-11 place-items-center rounded-2xl bg-primary text-primaryForeground shadow-card">
              <Building2 className="h-5 w-5" aria-hidden="true" />
            </div>
            <div>
              <p className="text-sm font-semibold uppercase tracking-[0.2em] text-white">MECA DOJO</p>
              <p className="mt-1 truncate text-xs text-white/55">{user.tenant.name}</p>
            </div>
          </div>
        </div>

        <nav className="flex-1 space-y-1 px-4" aria-label="Navegacao principal">
          {navItems.map((item) => {
            const active = item.exact ? pathname === item.href : pathname.startsWith(item.href);
            const Icon = item.icon;
            return (
              <Link key={item.href} href={item.href} className={cn(
                "focus-ring flex items-center gap-3 rounded-xl px-3 py-3 text-sm font-medium transition",
                active ? "bg-white text-foreground shadow-card" : "text-white/68 hover:bg-white/8 hover:text-white"
              )}>
                <Icon className="h-4 w-4" aria-hidden="true" />
                {item.label}
              </Link>
            );
          })}
        </nav>

        <div className="border-t border-white/10 p-4">
          <div className="mb-3 flex items-center gap-3 rounded-2xl bg-white/6 p-3">
            <Avatar name={user.name} />
            <div className="min-w-0">
              <p className="truncate text-sm font-semibold text-white">{user.name}</p>
              <p className="truncate text-xs text-white/55">{user.roles.join(", ")}</p>
            </div>
          </div>
          <LogoutButton />
        </div>
      </aside>

      <div className="lg:pl-72">
        <header className="sticky top-0 z-20 border-b border-border bg-background/88 backdrop-blur">
          <div className="mx-auto flex max-w-7xl items-center justify-between gap-4 px-4 py-4 sm:px-6 lg:px-8">
            <div>
              <p className="caption-text">{user.tenant.name}</p>
              <p className="mt-1 text-sm text-mutedForeground">Ola, {user.name}</p>
            </div>
            <div className="hidden items-center gap-3 sm:flex">
              <Avatar name={user.name} compact />
            </div>
          </div>
        </header>

        <section className="mx-auto max-w-7xl px-4 pb-28 pt-6 sm:px-6 lg:px-8 lg:pb-10">{children}</section>
      </div>

      <nav className="fixed inset-x-0 bottom-0 z-40 border-t border-border bg-surface/95 px-2 py-2 shadow-soft backdrop-blur lg:hidden" aria-label="Navegacao principal mobile">
        <div className="grid grid-cols-4 gap-1">
          {mobileNavItems.map((item) => {
            const active = item.exact ? pathname === item.href : pathname.startsWith(item.href);
            const Icon = item.icon;
            return (
              <Link key={item.href} href={item.href} className={cn(
                "focus-ring flex flex-col items-center justify-center gap-1 rounded-xl px-2 py-2 text-xs font-semibold transition",
                active ? "bg-primary text-primaryForeground" : "text-mutedForeground hover:bg-surfaceElevated hover:text-foreground"
              )}>
                <Icon className="h-4 w-4" aria-hidden="true" />
                {item.label}
              </Link>
            );
          })}
        </div>
      </nav>
    </main>
  );
}

function Avatar({ name, compact = false }: { name: string; compact?: boolean }) {
  const initials = name.split(" ").filter(Boolean).slice(0, 2).map((part) => part[0]?.toUpperCase()).join("") || "MD";
  return <div className={cn("grid shrink-0 place-items-center rounded-full bg-primary/12 font-semibold text-primary", compact ? "h-10 w-10 text-sm" : "h-11 w-11 text-sm")}>{initials}</div>;
}

import { getCurrentUser } from "@/src/lib/auth/server";
import Link from "next/link";
import { redirect } from "next/navigation";
import { type ReactNode } from "react";
import { LogoutButton } from "./LogoutButton";

export default async function AppLayout({ children }: { children: ReactNode }) {
  const user = await getCurrentUser();
  if (!user) {
    redirect("/login");
  }

  const navItems = [
    { href: "/app", label: "Dashboard" },
    { href: "/app/students", label: "Alunos" },
    { href: "/app/staff", label: "Equipe" },
    { href: "/app/units", label: "Unidades" },
  ];

  return (
    <main className="min-h-screen bg-[#f6f1e9]">
      <header className="border-b border-stone-200 bg-white">
        <div className="mx-auto flex max-w-6xl flex-col gap-4 px-5 py-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <p className="text-sm font-semibold uppercase tracking-[0.18em] text-red-800">MECA DOJO</p>
            <p className="mt-1 text-sm text-stone-600">{user.tenant.name}</p>
          </div>
          <div className="flex items-center gap-3">
            <span className="text-sm text-stone-700">Ola, {user.name}</span>
            <LogoutButton />
          </div>
        </div>
        <nav className="mx-auto flex max-w-6xl gap-2 overflow-x-auto px-5 pb-4">
          {navItems.map((item) => (
            <Link key={item.href} href={item.href} className="rounded border border-stone-200 px-3 py-2 text-sm font-medium text-stone-800">
              {item.label}
            </Link>
          ))}
        </nav>
      </header>
      <section className="mx-auto max-w-6xl px-5 py-6">{children}</section>
    </main>
  );
}

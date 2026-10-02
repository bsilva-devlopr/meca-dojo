import { getCurrentUser } from "@/src/lib/auth/server";
import { redirect } from "next/navigation";
import { LogoutButton } from "./LogoutButton";

export default async function AppPage() {
  const user = await getCurrentUser();
  if (!user) {
    redirect("/login");
  }

  return (
    <main className="min-h-screen px-5 py-6">
      <section className="mx-auto flex w-full max-w-2xl flex-col gap-8">
        <header className="flex items-center justify-between gap-4">
          <p className="text-sm font-semibold uppercase tracking-[0.18em] text-red-800">MECA DOJO</p>
          <LogoutButton />
        </header>

        <div className="rounded border border-stone-200 bg-white p-5 shadow-sm">
          <p className="text-lg text-stone-700">Ola, {user.name}</p>
          <h1 className="mt-2 text-3xl font-semibold text-stone-950">{user.tenant.name}</h1>
          <div className="mt-5 flex flex-wrap gap-2">
            {user.roles.map((role) => (
              <span key={role} className="rounded border border-stone-300 px-3 py-1 text-sm font-medium text-stone-800">
                {role}
              </span>
            ))}
          </div>
        </div>
      </section>
    </main>
  );
}

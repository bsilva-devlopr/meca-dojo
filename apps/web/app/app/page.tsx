import { getCurrentUser } from "@/src/lib/auth/server";
import { redirect } from "next/navigation";

export default async function AppPage() {
  const user = await getCurrentUser();
  if (!user) {
    redirect("/login");
  }

  return (
    <div className="rounded border border-stone-200 bg-white p-5 shadow-sm">
      <h1 className="text-2xl font-semibold text-stone-950">Dashboard</h1>
      <p className="mt-2 text-stone-700">Sessao ativa em {user.tenant.name}.</p>
      <div className="mt-5 flex flex-wrap gap-2">
        {user.roles.map((role) => (
          <span key={role} className="rounded border border-stone-300 px-3 py-1 text-sm font-medium text-stone-800">
            {role}
          </span>
        ))}
      </div>
    </div>
  );
}

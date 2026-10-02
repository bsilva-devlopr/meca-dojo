"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";

export function LogoutButton() {
  const router = useRouter();
  const [loading, setLoading] = useState(false);

  async function logout() {
    setLoading(true);
    await fetch("/api/auth/logout", { method: "POST" });
    router.replace("/login");
    router.refresh();
  }

  return (
    <button
      type="button"
      onClick={logout}
      disabled={loading}
      className="h-11 rounded border border-stone-300 bg-white px-4 text-sm font-semibold text-stone-950 transition hover:border-stone-950 disabled:cursor-not-allowed disabled:text-stone-500"
    >
      Sair
    </button>
  );
}

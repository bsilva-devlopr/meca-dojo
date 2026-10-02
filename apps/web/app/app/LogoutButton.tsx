"use client";

import { Button } from "@/src/components/ui/Button";
import { LogOut } from "lucide-react";
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
    <Button type="button" onClick={logout} disabled={loading} variant="secondary" className="w-full border-white/10 bg-white/8 text-white hover:bg-white/12 hover:text-white">
      <LogOut className="h-4 w-4" aria-hidden="true" />
      {loading ? "Saindo..." : "Sair"}
    </Button>
  );
}

import { getCurrentUser } from "@/src/lib/auth/server";
import { AppChrome } from "@/src/components/app/AppChrome";
import { redirect } from "next/navigation";
import { type ReactNode } from "react";

export default async function AppLayout({ children }: { children: ReactNode }) {
  const user = await getCurrentUser();
  if (!user) {
    redirect("/login");
  }

  return <AppChrome user={user}>{children}</AppChrome>;
}

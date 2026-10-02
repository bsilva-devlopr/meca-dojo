import { apiBaseUrl } from "@/src/lib/api/client";
import { accessCookieName } from "@/src/lib/auth/cookies";
import { type MeResponse } from "@/src/lib/auth/types";
import { cookies } from "next/headers";

export async function getCurrentUser(): Promise<MeResponse | null> {
  const cookieStore = await cookies();
  const accessToken = cookieStore.get(accessCookieName)?.value;
  if (!accessToken) {
    return null;
  }

  const response = await fetch(`${apiBaseUrl}/api/v1/auth/me`, {
    headers: { Authorization: `Bearer ${accessToken}` },
    cache: "no-store",
  });

  if (!response.ok) {
    return null;
  }

  return (await response.json()) as MeResponse;
}

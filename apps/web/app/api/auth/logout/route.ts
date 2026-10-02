import { apiBaseUrl } from "@/src/lib/api/client";
import { accessCookieName, refreshCookieName } from "@/src/lib/auth/cookies";
import { cookies } from "next/headers";
import { NextResponse } from "next/server";

export async function POST() {
  const cookieStore = await cookies();
  const accessToken = cookieStore.get(accessCookieName)?.value;
  const refreshToken = cookieStore.get(refreshCookieName)?.value;

  if (accessToken && refreshToken) {
    await fetch(`${apiBaseUrl}/api/v1/auth/logout`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${accessToken}`,
      },
      body: JSON.stringify({ refreshToken }),
      cache: "no-store",
    }).catch(() => undefined);
  }

  cookieStore.delete(accessCookieName);
  cookieStore.delete(refreshCookieName);

  return NextResponse.json({ ok: true });
}

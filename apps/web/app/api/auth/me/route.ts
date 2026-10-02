import { apiBaseUrl } from "@/src/lib/api/client";
import { accessCookieName, authCookieOptions, refreshCookieName, refreshMaxAgeSeconds } from "@/src/lib/auth/cookies";
import { type MeResponse, type TokenResponse } from "@/src/lib/auth/types";
import { cookies } from "next/headers";
import { NextResponse } from "next/server";

export async function GET() {
  const cookieStore = await cookies();
  let accessToken = cookieStore.get(accessCookieName)?.value;

  let me = accessToken ? await fetchMe(accessToken) : null;
  if (!me) {
    const refreshToken = cookieStore.get(refreshCookieName)?.value;
    if (refreshToken) {
      const tokens = await refresh(refreshToken);
      if (tokens) {
        cookieStore.set(accessCookieName, tokens.accessToken, authCookieOptions(tokens.expiresInSeconds));
        cookieStore.set(refreshCookieName, tokens.refreshToken, authCookieOptions(refreshMaxAgeSeconds()));
        accessToken = tokens.accessToken;
        me = await fetchMe(accessToken);
      }
    }
  }

  if (!me) {
    cookieStore.delete(accessCookieName);
    cookieStore.delete(refreshCookieName);
    return NextResponse.json({ message: "Authentication required" }, { status: 401 });
  }

  return NextResponse.json(me);
}

async function fetchMe(accessToken: string): Promise<MeResponse | null> {
  const response = await fetch(`${apiBaseUrl}/api/v1/auth/me`, {
    headers: { Authorization: `Bearer ${accessToken}` },
    cache: "no-store",
  });
  return response.ok ? ((await response.json()) as MeResponse) : null;
}

async function refresh(refreshToken: string): Promise<TokenResponse | null> {
  const response = await fetch(`${apiBaseUrl}/api/v1/auth/refresh`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ refreshToken }),
    cache: "no-store",
  });
  return response.ok ? ((await response.json()) as TokenResponse) : null;
}

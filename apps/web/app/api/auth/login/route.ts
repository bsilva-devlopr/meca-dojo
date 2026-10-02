import { apiBaseUrl } from "@/src/lib/api/client";
import { accessCookieName, authCookieOptions, refreshCookieName, refreshMaxAgeSeconds } from "@/src/lib/auth/cookies";
import { type TokenResponse } from "@/src/lib/auth/types";
import { cookies } from "next/headers";
import { NextResponse } from "next/server";

export async function POST(request: Request) {
  const body = await request.json();
  const response = await fetch(`${apiBaseUrl}/api/v1/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
    cache: "no-store",
  });

  if (!response.ok) {
    return NextResponse.json({ message: "Equipe, e-mail ou senha invalidos." }, { status: response.status });
  }

  const tokens = (await response.json()) as TokenResponse;
  const cookieStore = await cookies();
  cookieStore.set(accessCookieName, tokens.accessToken, authCookieOptions(tokens.expiresInSeconds));
  cookieStore.set(refreshCookieName, tokens.refreshToken, authCookieOptions(refreshMaxAgeSeconds()));

  return NextResponse.json({ ok: true });
}

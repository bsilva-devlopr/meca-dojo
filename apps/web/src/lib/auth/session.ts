import { apiBaseUrl } from "@/src/lib/api/client";
import { accessCookieName, authCookieOptions, refreshCookieName, refreshMaxAgeSeconds } from "@/src/lib/auth/cookies";
import { type MeResponse, type TokenResponse } from "@/src/lib/auth/types";
import { cookies } from "next/headers";

export async function getCurrentUser(): Promise<MeResponse | null> {
  const response = await authenticatedApiFetch("/api/v1/auth/me");
  if (!response.ok) {
    return null;
  }
  return (await response.json()) as MeResponse;
}

export async function authenticatedApiFetch(path: string, init: RequestInit = {}, retry = true): Promise<Response> {
  const cookieStore = await cookies();
  const accessToken = cookieStore.get(accessCookieName)?.value;
  const headers = new Headers(init.headers);

  if (accessToken) {
    headers.set("Authorization", `Bearer ${accessToken}`);
  }
  if (!headers.has("Content-Type") && init.body) {
    headers.set("Content-Type", "application/json");
  }

  const response = await fetch(`${apiBaseUrl}${path}`, {
    ...init,
    headers,
    cache: "no-store",
  });

  if (response.status !== 401 || !retry) {
    return response;
  }

  const refreshed = await refreshTokens();
  if (!refreshed) {
    cookieStore.delete(accessCookieName);
    cookieStore.delete(refreshCookieName);
    return response;
  }

  return authenticatedApiFetch(path, init, false);
}

async function refreshTokens(): Promise<boolean> {
  const cookieStore = await cookies();
  const refreshToken = cookieStore.get(refreshCookieName)?.value;
  if (!refreshToken) {
    return false;
  }

  const response = await fetch(`${apiBaseUrl}/api/v1/auth/refresh`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ refreshToken }),
    cache: "no-store",
  });

  if (!response.ok) {
    return false;
  }

  const tokens = (await response.json()) as TokenResponse;
  cookieStore.set(accessCookieName, tokens.accessToken, authCookieOptions(tokens.expiresInSeconds));
  cookieStore.set(refreshCookieName, tokens.refreshToken, authCookieOptions(refreshMaxAgeSeconds()));
  return true;
}

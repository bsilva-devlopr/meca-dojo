import { type ResponseCookie } from "next/dist/compiled/@edge-runtime/cookies";

export const accessCookieName = "meca_access_token";
export const refreshCookieName = "meca_refresh_token";

export function authCookieOptions(maxAge: number): Partial<ResponseCookie> {
  return {
    httpOnly: true,
    secure: process.env.NODE_ENV === "production",
    sameSite: "lax",
    path: "/",
    maxAge,
  };
}

export function refreshMaxAgeSeconds() {
  return 60 * 60 * 24 * 7;
}

export const apiBaseUrl = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

export async function getApiHealth(signal?: AbortSignal): Promise<boolean> {
  const response = await fetch(`${apiBaseUrl}/actuator/health`, {
    cache: "no-store",
    signal,
  });

  if (!response.ok) {
    return false;
  }

  const payload = await response.json() as { status?: string };
  return payload.status === "UP";
}

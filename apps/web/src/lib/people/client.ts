export async function bffJson<T>(path: string, init: RequestInit = {}): Promise<T> {
  const response = await fetch(`/api/bff${path}`, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...(init.headers ?? {}),
    },
  });

  if (!response.ok) {
    const payload = await response.json().catch(() => ({ message: "Erro inesperado." }));
    throw new Error(payload.message ?? "Erro inesperado.");
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}

export function hasPermission(permissions: string[], permission: string) {
  return permissions.includes(permission);
}

"use client";

import { useEffect, useState } from "react";
import { apiBaseUrl, getApiHealth } from "./client";

type ApiStatus = "checking" | "available" | "unavailable";

export function FoundationStatus() {
  const [status, setStatus] = useState<ApiStatus>("checking");

  useEffect(() => {
    const controller = new AbortController();

    getApiHealth(controller.signal)
      .then((available) => setStatus(available ? "available" : "unavailable"))
      .catch(() => setStatus("unavailable"));

    return () => controller.abort();
  }, []);

  const label = {
    checking: "API checking",
    available: "API available",
    unavailable: "API unavailable",
  }[status];

  return (
    <div className="inline-flex max-w-full items-center gap-3 rounded border border-neutral-300 bg-white px-4 py-3 text-sm text-neutral-800 shadow-sm">
      <span
        aria-hidden="true"
        className={`h-2.5 w-2.5 rounded-full ${status === "available" ? "bg-emerald-600" : status === "unavailable" ? "bg-red-600" : "bg-amber-500"}`}
      />
      <span>{label}</span>
      <span className="hidden text-neutral-500 sm:inline">{apiBaseUrl}</span>
    </div>
  );
}

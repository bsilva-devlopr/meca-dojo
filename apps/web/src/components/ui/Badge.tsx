import { cn } from "@/src/lib/utils/cn";
import { type ReactNode } from "react";

type Tone = "neutral" | "success" | "warning" | "danger" | "info" | "primary";

const tones: Record<Tone, string> = {
  neutral: "border-border bg-surfaceElevated text-mutedForeground",
  success: "border-success/20 bg-success/10 text-success",
  warning: "border-warning/20 bg-warning/10 text-warning",
  danger: "border-danger/20 bg-danger/10 text-danger",
  info: "border-info/20 bg-info/10 text-info",
  primary: "border-primary/20 bg-primary/10 text-primary",
};

export function Badge({ children, tone = "neutral", className }: { children: ReactNode; tone?: Tone; className?: string }) {
  return <span className={cn("inline-flex items-center rounded-full border px-2.5 py-1 text-xs font-semibold", tones[tone], className)}>{children}</span>;
}

export function StatusBadge({ status }: { status: string }) {
  const tone: Tone = status === "ACTIVE" ? "success" : status === "TRIAL" ? "info" : status === "SUSPENDED" || status === "LOCKED" ? "warning" : "neutral";
  return <Badge tone={tone}>{status}</Badge>;
}

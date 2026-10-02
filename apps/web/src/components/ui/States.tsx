import { cn } from "@/src/lib/utils/cn";
import { AlertCircle, Inbox } from "lucide-react";
import { type ReactNode } from "react";

export function PageHeader({ eyebrow, title, description, action }: { eyebrow?: string; title: string; description?: string; action?: ReactNode }) {
  return (
    <div className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
      <div>
        {eyebrow ? <p className="caption-text mb-2">{eyebrow}</p> : null}
        <h1 className="page-title">{title}</h1>
        {description ? <p className="mt-2 max-w-2xl text-sm leading-6 text-mutedForeground">{description}</p> : null}
      </div>
      {action}
    </div>
  );
}

export function EmptyState({ title, description, action, className }: { title: string; description: string; action?: ReactNode; className?: string }) {
  return (
    <div className={cn("rounded-2xl border border-dashed border-border bg-surfaceElevated p-6 text-center", className)}>
      <Inbox className="mx-auto h-8 w-8 text-mutedForeground" aria-hidden="true" />
      <h3 className="mt-3 font-semibold text-foreground">{title}</h3>
      <p className="mx-auto mt-1 max-w-md text-sm leading-6 text-mutedForeground">{description}</p>
      {action ? <div className="mt-4">{action}</div> : null}
    </div>
  );
}

export function ErrorState({ message }: { message: string }) {
  return (
    <div className="flex items-start gap-3 rounded-lg border border-danger/20 bg-danger/10 p-3 text-sm text-danger">
      <AlertCircle className="mt-0.5 h-4 w-4 shrink-0" aria-hidden="true" />
      <p>{message}</p>
    </div>
  );
}

export function LoadingState({ label = "Carregando" }: { label?: string }) {
  return (
    <div className="space-y-3" aria-busy="true" aria-live="polite">
      <p className="text-sm text-mutedForeground">{label}...</p>
      <div className="grid gap-3">
        <div className="h-20 animate-pulse rounded-2xl bg-border/45" />
        <div className="h-20 animate-pulse rounded-2xl bg-border/35" />
      </div>
    </div>
  );
}

import { cn } from "@/src/lib/utils/cn";
import { type HTMLAttributes, type ReactNode } from "react";

export function Card({ className, ...props }: HTMLAttributes<HTMLElement>) {
  return <section className={cn("rounded-2xl border border-border bg-surface p-5 shadow-card", className)} {...props} />;
}

export function CardHeader({ title, eyebrow, action, children }: { title: string; eyebrow?: string; action?: ReactNode; children?: ReactNode }) {
  return (
    <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
      <div>
        {eyebrow ? <p className="caption-text mb-2">{eyebrow}</p> : null}
        <h2 className="card-title">{title}</h2>
        {children ? <div className="mt-1 text-sm text-mutedForeground">{children}</div> : null}
      </div>
      {action}
    </div>
  );
}

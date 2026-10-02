import { cn } from "@/src/lib/utils/cn";
import { type InputHTMLAttributes, type ReactNode, type SelectHTMLAttributes, type TextareaHTMLAttributes } from "react";

const control = "focus-ring min-h-11 w-full rounded-lg border border-border bg-surface px-3 text-sm text-foreground transition placeholder:text-mutedForeground/75 focus:border-primary disabled:cursor-not-allowed disabled:opacity-60";

export function FormField({ label, hint, error, required, children }: { label: string; hint?: string; error?: string; required?: boolean; children: ReactNode }) {
  return (
    <label className="grid gap-2 text-sm font-medium text-foreground">
      <span>{label}{required ? <span className="text-danger"> *</span> : null}</span>
      {children}
      {error ? <span className="text-xs font-medium text-danger">{error}</span> : null}
      {hint && !error ? <span className="text-xs text-mutedForeground">{hint}</span> : null}
    </label>
  );
}

export function Input({ className, ...props }: InputHTMLAttributes<HTMLInputElement>) {
  return <input className={cn(control, "h-11", className)} {...props} />;
}

export function Select({ className, ...props }: SelectHTMLAttributes<HTMLSelectElement>) {
  return <select className={cn(control, "h-11", className)} {...props} />;
}

export function Textarea({ className, ...props }: TextareaHTMLAttributes<HTMLTextAreaElement>) {
  return <textarea className={cn(control, "min-h-28 py-3", className)} {...props} />;
}

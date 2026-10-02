import { cn } from "@/src/lib/utils/cn";
import Link from "next/link";
import { type AnchorHTMLAttributes, type ButtonHTMLAttributes, type ReactNode } from "react";

type Variant = "primary" | "secondary" | "ghost" | "danger";
type Size = "sm" | "md" | "lg";

const variants: Record<Variant, string> = {
  primary: "bg-primary text-primaryForeground shadow-card hover:bg-primary/90",
  secondary: "border border-border bg-surface text-foreground hover:border-primary/40 hover:bg-surfaceElevated",
  ghost: "text-mutedForeground hover:bg-surfaceElevated hover:text-foreground",
  danger: "bg-danger text-white hover:bg-danger/90",
};

const sizes: Record<Size, string> = {
  sm: "h-9 px-3 text-sm",
  md: "h-11 px-4 text-sm",
  lg: "h-12 px-5 text-base",
};

const base = "focus-ring inline-flex items-center justify-center gap-2 rounded-lg font-semibold transition disabled:pointer-events-none disabled:opacity-55";

export function Button({ className, variant = "primary", size = "md", ...props }: ButtonHTMLAttributes<HTMLButtonElement> & { variant?: Variant; size?: Size }) {
  return <button className={cn(base, variants[variant], sizes[size], className)} {...props} />;
}

export function ButtonLink({ className, variant = "primary", size = "md", children, ...props }: AnchorHTMLAttributes<HTMLAnchorElement> & { href: string; variant?: Variant; size?: Size; children: ReactNode }) {
  return <Link className={cn(base, variants[variant], sizes[size], className)} {...props}>{children}</Link>;
}

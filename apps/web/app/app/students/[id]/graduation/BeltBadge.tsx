import { cn } from "@/src/lib/utils/cn";
import { type BeltResponse } from "@/src/lib/graduation/types";

type BeltStyle = { shell: string; bar: string; rail: string; text: string };

const beltStyles: Record<string, BeltStyle> = {
  WHITE: { shell: "bg-stone-100", bar: "bg-white border border-stone-300", rail: "bg-stone-950", text: "text-stone-900" },
  BLUE: { shell: "bg-blue-50", bar: "bg-blue-700", rail: "bg-stone-950", text: "text-blue-900" },
  PURPLE: { shell: "bg-purple-50", bar: "bg-purple-700", rail: "bg-stone-950", text: "text-purple-900" },
  BROWN: { shell: "bg-amber-50", bar: "bg-amber-900", rail: "bg-stone-950", text: "text-amber-950" },
  BLACK: { shell: "bg-stone-100", bar: "bg-stone-950", rail: "bg-red-900", text: "text-stone-950" },
  GREY_WHITE: { shell: "bg-stone-100", bar: "bg-gradient-to-r from-stone-300 via-white to-stone-300 border border-stone-300", rail: "bg-stone-950", text: "text-stone-800" },
  GREY: { shell: "bg-stone-100", bar: "bg-stone-400", rail: "bg-stone-950", text: "text-stone-800" },
  GREY_BLACK: { shell: "bg-stone-100", bar: "bg-gradient-to-r from-stone-400 via-stone-500 to-stone-950", rail: "bg-stone-950", text: "text-stone-800" },
  YELLOW_WHITE: { shell: "bg-yellow-50", bar: "bg-gradient-to-r from-yellow-300 via-white to-yellow-300 border border-yellow-300", rail: "bg-stone-950", text: "text-yellow-900" },
  YELLOW: { shell: "bg-yellow-50", bar: "bg-yellow-300", rail: "bg-stone-950", text: "text-yellow-900" },
  YELLOW_BLACK: { shell: "bg-yellow-50", bar: "bg-gradient-to-r from-yellow-300 via-yellow-400 to-stone-950", rail: "bg-stone-950", text: "text-yellow-900" },
  ORANGE_WHITE: { shell: "bg-orange-50", bar: "bg-gradient-to-r from-orange-400 via-white to-orange-400 border border-orange-300", rail: "bg-stone-950", text: "text-orange-900" },
  ORANGE: { shell: "bg-orange-50", bar: "bg-orange-500", rail: "bg-stone-950", text: "text-orange-900" },
  ORANGE_BLACK: { shell: "bg-orange-50", bar: "bg-gradient-to-r from-orange-500 via-orange-600 to-stone-950", rail: "bg-stone-950", text: "text-orange-900" },
  GREEN_WHITE: { shell: "bg-green-50", bar: "bg-gradient-to-r from-green-600 via-white to-green-600 border border-green-400", rail: "bg-stone-950", text: "text-green-900" },
  GREEN: { shell: "bg-green-50", bar: "bg-green-700", rail: "bg-stone-950", text: "text-green-900" },
  GREEN_BLACK: { shell: "bg-green-50", bar: "bg-gradient-to-r from-green-700 via-green-800 to-stone-950", rail: "bg-stone-950", text: "text-green-900" },
};

export function BeltBadge({ belt, degree, hero = false, showCode = true }: { belt: Omit<BeltResponse, "id">; degree: number; hero?: boolean; showCode?: boolean }) {
  const style = beltStyles[belt.code] ?? beltStyles.WHITE;
  const maxMarkers = Math.max(belt.maxDegree, degree, 4);
  return (
    <div className={cn("rounded-2xl border border-border p-4", style.shell, hero && "p-5")} aria-label={`${belt.name}, ${degree} graus`}>
      <div className={cn("relative overflow-hidden rounded-full shadow-card", hero ? "h-12" : "h-8", style.bar)}>
        <div className={cn("absolute inset-y-0 right-4 my-auto flex items-center justify-center gap-1.5 rounded-full px-3", hero ? "h-8 min-w-32" : "h-6 min-w-24", style.rail)}>
          {Array.from({ length: maxMarkers }).map((_, index) => (
            <span key={index} className={cn("rounded-full border border-white", hero ? "h-3 w-3" : "h-2.5 w-2.5", index < degree ? "bg-white" : "bg-transparent")} aria-hidden="true" />
          ))}
        </div>
      </div>
      <div className="mt-4 flex flex-col gap-1 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p className={cn("font-semibold", hero ? "text-2xl" : "text-base", style.text)}>{belt.name}</p>
          <p className="text-sm text-mutedForeground">{degree} {degree === 1 ? "grau" : "graus"} · {belt.system === "KIDS" ? "Infantil" : "Adulto / geral"}</p>
        </div>
        {showCode && <p className="caption-text">{belt.code}</p>}
      </div>
    </div>
  );
}

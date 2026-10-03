"use client";

import { Badge } from "@/src/components/ui/Badge";
import { ButtonLink } from "@/src/components/ui/Button";
import { Card } from "@/src/components/ui/Card";
import { audienceLabels, modalityLabels } from "@/src/lib/schedule/types";
import { trainingStatusLabels, trainingStatusTone, type TrainingSession } from "@/src/lib/training/types";
import { Clock, MapPin, UserRound } from "lucide-react";

export function TrainingSessionCard({ session, canOpen, canComplete }: { session: TrainingSession; canOpen: boolean; canComplete: boolean }) {
  const lead = session.staff.find((item) => item.role === "LEAD") ?? session.staff[0];
  const primaryHref = `/app/training/${session.id}`;
  const primaryLabel = session.status === "SCHEDULED" && canOpen ? "Abrir treino" : session.status === "OPEN" && canComplete ? "Encerrar treino" : "Ver detalhes";

  return (
    <Card className="p-4">
      <div className="flex items-start gap-4">
        <div className="grid min-h-16 w-16 shrink-0 place-items-center rounded-xl bg-foreground text-white">
          <div className="text-center">
            <Clock className="mx-auto h-4 w-4 text-white/70" aria-hidden="true" />
            <p className="mt-1 text-sm font-bold">{time(session.startTime)}</p>
          </div>
        </div>
        <div className="min-w-0 flex-1">
          <div className="flex flex-col gap-2 sm:flex-row sm:items-start sm:justify-between">
            <div>
              <p className="text-xs font-semibold text-mutedForeground">{time(session.startTime)} - {time(session.endTime)}</p>
              <h2 className="mt-1 text-lg font-semibold text-foreground">{session.dojoClass.name}</h2>
            </div>
            <Badge tone={trainingStatusTone[session.status]}>{trainingStatusLabels[session.status]}</Badge>
          </div>
          <div className="mt-3 flex flex-wrap gap-2">
            <Badge tone={session.dojoClass.modality === "NO_GI" ? "primary" : "neutral"}>{modalityLabels[session.dojoClass.modality]}</Badge>
            <Badge>{audienceLabels[session.dojoClass.audience]}</Badge>
            {session.capacity ? <Badge>{session.capacity} vagas</Badge> : <Badge>Sem limite</Badge>}
          </div>
          <div className="mt-4 grid gap-2 text-sm text-mutedForeground">
            <p className="flex items-center gap-2"><UserRound className="h-4 w-4" aria-hidden="true" />{lead ? lead.displayName : "Professor a definir"}</p>
            <p className="flex items-center gap-2"><MapPin className="h-4 w-4" aria-hidden="true" />{session.unit.name}</p>
          </div>
          <div className="mt-4">
            <ButtonLink href={primaryHref} size="sm" variant={session.status === "SCHEDULED" && canOpen ? "primary" : "secondary"}>{primaryLabel}</ButtonLink>
          </div>
        </div>
      </div>
    </Card>
  );
}

function time(value: string) {
  return value.slice(0, 5);
}

"use client";

import { TrainingSessionCard } from "@/src/components/training/TrainingSessionCard";
import { Badge } from "@/src/components/ui/Badge";
import { Button, ButtonLink } from "@/src/components/ui/Button";
import { Card } from "@/src/components/ui/Card";
import { FormField, Input, Select } from "@/src/components/ui/Form";
import { EmptyState, ErrorState, LoadingState, PageHeader } from "@/src/components/ui/States";
import { bffJson, hasPermission } from "@/src/lib/people/client";
import { type PageResponse, type UnitSummary } from "@/src/lib/people/types";
import { type TrainingSession } from "@/src/lib/training/types";
import { CalendarPlus, ChevronLeft, ChevronRight, Plus, Wand2 } from "lucide-react";
import { type FormEvent, useCallback, useEffect, useMemo, useState } from "react";

type Me = { permissions: string[] };

export default function TrainingPage() {
  const [me, setMe] = useState<Me | null>(null);
  const [sessions, setSessions] = useState<PageResponse<TrainingSession> | null>(null);
  const [units, setUnits] = useState<UnitSummary[]>([]);
  const [date, setDate] = useState(todayLocalDate());
  const [unitId, setUnitId] = useState("");
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [generating, setGenerating] = useState(false);

  const load = useCallback(() => {
    const params = new URLSearchParams({ from: date, to: date, page: "0", size: "50" });
    if (unitId) params.set("unitId", unitId);
    setError("");
    bffJson<PageResponse<TrainingSession>>(`/training-sessions?${params.toString()}`).then(setSessions).catch((err: Error) => setError(err.message));
  }, [date, unitId]);

  useEffect(() => {
    fetch("/api/auth/me").then((r) => r.json()).then(setMe).catch(() => undefined);
    bffJson<UnitSummary[]>("/units").then(setUnits).catch(() => undefined);
  }, []);

  useEffect(() => { void Promise.resolve().then(load); }, [load]);

  const canCreate = me ? hasPermission(me.permissions, "TRAINING_SESSION_CREATE") : false;
  const canOpen = me ? hasPermission(me.permissions, "TRAINING_SESSION_OPEN") : false;
  const canComplete = me ? hasPermission(me.permissions, "TRAINING_SESSION_COMPLETE") : false;
  const readableDate = useMemo(() => formatDate(date), [date]);

  async function generate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
    setMessage("");
    setGenerating(true);
    const form = new FormData(event.currentTarget);
    const body = {
      from: String(form.get("from") ?? ""),
      to: String(form.get("to") ?? ""),
      unitId: blank(form.get("unitId")),
    };
    try {
      const result = await bffJson<{ created: number; existing: number }>("/training-sessions/generate", { method: "POST", body: JSON.stringify(body) });
      setMessage(`${result.created} treinos criados. ${result.existing} ja existentes.`);
      load();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Erro ao gerar treinos.");
    } finally {
      setGenerating(false);
    }
  }

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Operacao"
        title="Treinos"
        description="Ocorrencias reais da agenda, com professor, unidade, horario e estado operacional por data."
        action={canCreate ? <div className="flex flex-wrap gap-2"><ButtonLink href="/app/training/new" variant="secondary"><Plus className="h-4 w-4" aria-hidden="true" />Novo treino</ButtonLink></div> : null}
      />

      <Card className="p-4">
        <div className="grid gap-4 lg:grid-cols-[1fr_220px] lg:items-center">
          <div>
            <div className="flex items-center justify-between gap-3">
              <Button type="button" variant="secondary" size="sm" aria-label="Dia anterior" onClick={() => setDate(addDays(date, -1))}><ChevronLeft className="h-4 w-4" aria-hidden="true" /></Button>
              <div className="text-center">
                <p className="caption-text">{date === todayLocalDate() ? "Hoje" : readableDate.weekday}</p>
                <h2 className="text-2xl font-semibold text-foreground">{readableDate.dayMonth}</h2>
              </div>
              <Button type="button" variant="secondary" size="sm" aria-label="Proximo dia" onClick={() => setDate(addDays(date, 1))}><ChevronRight className="h-4 w-4" aria-hidden="true" /></Button>
            </div>
            <div className="mt-4 grid grid-cols-3 gap-2">
              {[-1, 0, 1].map((offset) => {
                const itemDate = addDays(todayLocalDate(), offset);
                const label = offset === -1 ? "Ontem" : offset === 0 ? "Hoje" : "Amanha";
                return <Button key={offset} type="button" variant={date === itemDate ? "primary" : "ghost"} size="sm" onClick={() => setDate(itemDate)}>{label}</Button>;
              })}
            </div>
          </div>
          <FormField label="Unidade">
            <Select value={unitId} onChange={(event) => setUnitId(event.target.value)}>
              <option value="">Todas as unidades</option>
              {units.map((unit) => <option key={unit.id} value={unit.id}>{unit.name}</option>)}
            </Select>
          </FormField>
        </div>
      </Card>

      {canCreate ? (
        <Card className="p-4">
          <form onSubmit={generate} className="grid gap-3 md:grid-cols-[1fr_1fr_1fr_auto] md:items-end">
            <FormField label="De" required><Input name="from" type="date" required defaultValue={date} /></FormField>
            <FormField label="Ate" required><Input name="to" type="date" required defaultValue={addDays(date, 6)} /></FormField>
            <FormField label="Unidade opcional"><Select name="unitId" defaultValue={unitId}><option value="">Todas</option>{units.map((unit) => <option key={unit.id} value={unit.id}>{unit.name}</option>)}</Select></FormField>
            <Button disabled={generating}><Wand2 className="h-4 w-4" aria-hidden="true" />{generating ? "Gerando..." : "Gerar treinos"}</Button>
          </form>
        </Card>
      ) : null}

      {message ? <div className="rounded-lg border border-success/20 bg-success/10 p-3 text-sm font-medium text-success">{message}</div> : null}
      {error ? <ErrorState message={error} /> : null}
      {!sessions ? <LoadingState label="Carregando treinos" /> : null}

      {sessions && sessions.content.length > 0 ? (
        <div className="space-y-3">
          <div className="flex items-center justify-between"><h2 className="section-title">{readableDate.weekday}</h2><Badge>{sessions.totalElements} treino(s)</Badge></div>
          {sessions.content.map((session) => <TrainingSessionCard key={session.id} session={session} canOpen={canOpen} canComplete={canComplete} />)}
        </div>
      ) : null}

      {sessions?.content.length === 0 ? (
        <EmptyState
          title={date === todayLocalDate() ? "Nenhum treino programado para hoje" : "Nenhum treino programado nesta data"}
          description="Voce pode gerar os treinos da grade ou cadastrar um treino extra."
          action={canCreate ? <div className="flex flex-col justify-center gap-2 sm:flex-row"><ButtonLink href="/app/training/new"><CalendarPlus className="h-4 w-4" aria-hidden="true" />Novo treino</ButtonLink></div> : null}
        />
      ) : null}
    </div>
  );
}

function todayLocalDate() {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, "0");
  const day = String(now.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

function addDays(value: string, days: number) {
  const date = new Date(`${value}T00:00:00`);
  date.setDate(date.getDate() + days);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
}

function formatDate(value: string) {
  const date = new Date(`${value}T00:00:00`);
  return {
    weekday: new Intl.DateTimeFormat("pt-BR", { weekday: "long" }).format(date),
    dayMonth: new Intl.DateTimeFormat("pt-BR", { day: "2-digit", month: "short" }).format(date),
  };
}

function blank(value: FormDataEntryValue | null) {
  const text = String(value ?? "").trim();
  return text ? text : null;
}

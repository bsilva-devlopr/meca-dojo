"use client";

import { Badge } from "@/src/components/ui/Badge";
import { Button, ButtonLink } from "@/src/components/ui/Button";
import { Card } from "@/src/components/ui/Card";
import { FormField, Input, Select } from "@/src/components/ui/Form";
import { EmptyState, ErrorState, LoadingState, PageHeader } from "@/src/components/ui/States";
import { bffJson, hasPermission } from "@/src/lib/people/client";
import { type PageResponse, type StaffResponse, type UnitSummary } from "@/src/lib/people/types";
import { audienceLabels, dayLabels, dayShortLabels, modalityLabels, orderedDays, type ClassSchedule, type DayOfWeek, type DojoClass, type WeeklySchedule } from "@/src/lib/schedule/types";
import { CalendarPlus, Clock, MapPin, UsersRound } from "lucide-react";
import { type FormEvent, useCallback, useEffect, useMemo, useState } from "react";

type Me = { permissions: string[] };

type Editing = ClassSchedule | null;

export default function SchedulePage() {
  const [me, setMe] = useState<Me | null>(null);
  const [weekly, setWeekly] = useState<WeeklySchedule | null>(null);
  const [classes, setClasses] = useState<DojoClass[]>([]);
  const [units, setUnits] = useState<UnitSummary[]>([]);
  const [staff, setStaff] = useState<StaffResponse[]>([]);
  const [unitId, setUnitId] = useState("");
  const [selectedDay, setSelectedDay] = useState<DayOfWeek>(todayDay());
  const [editing, setEditing] = useState<Editing>(null);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [saving, setSaving] = useState(false);

  const load = useCallback(() => {
    const params = new URLSearchParams();
    if (unitId) params.set("unitId", unitId);
    bffJson<WeeklySchedule>(`/schedules/weekly?${params.toString()}`).then(setWeekly).catch((err: Error) => setError(err.message));
  }, [unitId]);

  useEffect(() => {
    fetch("/api/auth/me").then((r) => r.json()).then(setMe).catch(() => undefined);
    bffJson<PageResponse<DojoClass>>("/classes?status=ACTIVE&page=0&size=100").then((page) => setClasses(page.content)).catch(() => undefined);
    bffJson<UnitSummary[]>("/units").then(setUnits).catch(() => undefined);
    bffJson<PageResponse<StaffResponse>>("/staff?status=ACTIVE&page=0&size=100").then((page) => setStaff(page.content)).catch(() => undefined);
  }, []);
  useEffect(() => { load(); }, [load]);

  const canCreate = me ? hasPermission(me.permissions, "SCHEDULE_CREATE") : false;
  const canUpdate = me ? hasPermission(me.permissions, "SCHEDULE_UPDATE") : false;
  const days = weekly?.days ?? orderedDays.map((dayOfWeek) => ({ dayOfWeek, schedules: [] }));
  const mobileSchedules = useMemo(() => days.find((day) => day.dayOfWeek === selectedDay)?.schedules ?? [], [days, selectedDay]);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
    setMessage("");
    setSaving(true);
    const form = new FormData(event.currentTarget);
    const lead = String(form.get("leadStaffId") ?? "").trim();
    const assistant = String(form.get("assistantStaffId") ?? "").trim();
    const capacity = String(form.get("capacityOverride") ?? "").trim();
    const assignments = [
      ...(lead ? [{ staffId: lead, role: "LEAD" }] : []),
      ...(assistant && assistant !== lead ? [{ staffId: assistant, role: "ASSISTANT" }] : []),
    ];
    const body = {
      classId: String(form.get("classId") ?? ""),
      unitId: String(form.get("unitId") ?? ""),
      dayOfWeek: String(form.get("dayOfWeek") ?? "MONDAY"),
      startTime: String(form.get("startTime") ?? ""),
      endTime: String(form.get("endTime") ?? ""),
      status: String(form.get("status") ?? "ACTIVE"),
      ...(capacity ? { capacityOverride: Number(capacity) } : {}),
      staff: assignments,
    };
    try {
      await bffJson(editing ? `/schedules/${editing.id}` : "/schedules", { method: editing ? "PATCH" : "POST", body: JSON.stringify(body) });
      setMessage(editing ? "Horario atualizado." : "Horario adicionado a grade.");
      setEditing(null);
      event.currentTarget.reset();
      load();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Erro ao salvar horario.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="space-y-6">
      <PageHeader eyebrow="Agenda" title="Grade semanal" description="Horarios recorrentes da academia. Sessoes reais por data entram no proximo ciclo." action={canCreate ? <a href="#schedule-form" className="focus-ring inline-flex h-11 items-center justify-center gap-2 rounded-lg bg-primary px-4 text-sm font-semibold text-primaryForeground shadow-card transition hover:bg-primary/90"><CalendarPlus className="h-4 w-4" aria-hidden="true" />Adicionar horario</a> : null} />

      <Card className="p-4">
        <div className="grid gap-3 md:grid-cols-[1fr_220px] md:items-center">
          <div>
            <p className="caption-text">Recorrencia semanal</p>
            <p className="mt-1 text-sm text-mutedForeground">Sem navegacao por semanas enquanto ainda nao existem TrainingSessions datadas.</p>
          </div>
          <Select value={unitId} onChange={(e) => setUnitId(e.target.value)} aria-label="Filtrar unidade"><option value="">Todas as unidades</option>{units.map((unit) => <option key={unit.id} value={unit.id}>{unit.name}</option>)}</Select>
        </div>
      </Card>

      {error ? <ErrorState message={error} /> : null}
      {message ? <div className="rounded-lg border border-success/20 bg-success/10 p-3 text-sm font-medium text-success">{message}</div> : null}
      {!weekly ? <LoadingState label="Carregando grade" /> : null}

      {weekly && days.every((day) => day.schedules.length === 0) ? (
        <EmptyState title="Sua grade ainda esta vazia" description="Cadastre as aulas e organize os horarios semanais da academia." action={<ButtonLink href={classes.length ? "#schedule-form" : "/app/classes/new"}>{classes.length ? "Adicionar horario" : "Criar aula"}</ButtonLink>} />
      ) : null}

      {weekly ? (
        <>
          <div className="lg:hidden">
            <div className="no-scrollbar -mx-4 flex gap-2 overflow-x-auto px-4 pb-2" role="tablist" aria-label="Dias da semana">
              {orderedDays.map((day) => <button key={day} type="button" onClick={() => setSelectedDay(day)} className={`focus-ring rounded-full px-4 py-2 text-xs font-bold transition ${selectedDay === day ? "bg-primary text-primaryForeground" : "border border-border bg-surface text-mutedForeground"}`}>{dayShortLabels[day]}</button>)}
            </div>
            <div className="mt-4 space-y-3">
              <h2 className="section-title">{dayLabels[selectedDay]}</h2>
              {mobileSchedules.map((schedule) => <ScheduleCard key={schedule.id} schedule={schedule} canUpdate={canUpdate} onEdit={setEditing} />)}
              {mobileSchedules.length === 0 ? <EmptyState title="Sem aulas neste dia" description="Escolha outro dia ou adicione um horario recorrente para esta unidade." /> : null}
            </div>
          </div>

          <div className="hidden gap-3 lg:grid lg:grid-cols-7">
            {days.map((day) => (
              <section key={day.dayOfWeek} className="min-w-0 rounded-2xl border border-border bg-surface p-3">
                <h2 className="mb-3 text-sm font-bold uppercase tracking-[0.14em] text-mutedForeground">{dayShortLabels[day.dayOfWeek]}</h2>
                <div className="space-y-3">{day.schedules.map((schedule) => <ScheduleCard key={schedule.id} schedule={schedule} compact canUpdate={canUpdate} onEdit={setEditing} />)}{day.schedules.length === 0 ? <div className="rounded-xl border border-dashed border-border p-3 text-xs text-mutedForeground">Sem horarios</div> : null}</div>
              </section>
            ))}
          </div>
        </>
      ) : null}

      {canCreate || (editing && canUpdate) ? (
        <Card id="schedule-form" className="p-5">
          <div className="mb-5 flex items-start justify-between gap-3">
            <div><p className="caption-text">Grade</p><h2 className="mt-1 text-xl font-semibold text-foreground">{editing ? "Editar horario" : "Adicionar horario"}</h2></div>
            {editing ? <Button variant="ghost" type="button" onClick={() => setEditing(null)}>Cancelar</Button> : null}
          </div>
          <form onSubmit={submit} className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
            <FormField label="Aula" required><Select name="classId" required defaultValue={editing?.dojoClass.id ?? ""}><option value="">Selecione</option>{classes.map((dojoClass) => <option key={dojoClass.id} value={dojoClass.id}>{dojoClass.name}</option>)}</Select></FormField>
            <FormField label="Unidade" required><Select name="unitId" required defaultValue={editing?.unit.id ?? ""}><option value="">Selecione</option>{units.map((unit) => <option key={unit.id} value={unit.id}>{unit.name}</option>)}</Select></FormField>
            <FormField label="Dia" required><Select name="dayOfWeek" defaultValue={editing?.dayOfWeek ?? selectedDay}>{orderedDays.map((day) => <option key={day} value={day}>{dayLabels[day]}</option>)}</Select></FormField>
            <FormField label="Status"><Select name="status" defaultValue={editing?.status ?? "ACTIVE"}><option value="ACTIVE">Ativo</option><option value="INACTIVE">Inativo</option></Select></FormField>
            <FormField label="Inicio" required><Input name="startTime" type="time" required defaultValue={trimTime(editing?.startTime) ?? "20:30"} /></FormField>
            <FormField label="Fim" required><Input name="endTime" type="time" required defaultValue={trimTime(editing?.endTime) ?? "22:00"} /></FormField>
            <FormField label="Capacidade especifica" hint="Vazio usa a capacidade padrao da aula."><Input name="capacityOverride" type="number" min={1} defaultValue={editing?.capacityOverride ?? ""} /></FormField>
            <div className="hidden xl:block" />
            <FormField label="Professor principal"><Select name="leadStaffId" defaultValue={editing?.staff.find((item) => item.role === "LEAD")?.id ?? ""}><option value="">A definir</option>{staff.map((person) => <option key={person.id} value={person.id}>{person.displayName} ? {person.type}</option>)}</Select></FormField>
            <FormField label="Assistente"><Select name="assistantStaffId" defaultValue={editing?.staff.find((item) => item.role === "ASSISTANT")?.id ?? ""}><option value="">Sem assistente</option>{staff.map((person) => <option key={person.id} value={person.id}>{person.displayName} ? {person.type}</option>)}</Select></FormField>
            <Button disabled={saving} className="md:col-span-2 xl:col-span-4 xl:justify-self-start">{saving ? "Salvando..." : editing ? "Salvar horario" : "Adicionar horario"}</Button>
          </form>
        </Card>
      ) : null}
    </div>
  );
}

function ScheduleCard({ schedule, compact = false, canUpdate, onEdit }: { schedule: ClassSchedule; compact?: boolean; canUpdate: boolean; onEdit: (schedule: ClassSchedule) => void }) {
  const lead = schedule.staff.find((item) => item.role === "LEAD") ?? schedule.staff[0];
  return (
    <article className={`rounded-2xl border border-border bg-surfaceElevated p-4 shadow-soft ${compact ? "text-sm" : ""}`}>
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="flex items-center gap-2 text-sm font-bold text-foreground"><Clock className="h-4 w-4 text-primary" aria-hidden="true" />{trimTime(schedule.startTime)} - {trimTime(schedule.endTime)}</p>
          <h3 className="mt-3 font-semibold text-foreground">{schedule.dojoClass.name}</h3>
        </div>
        <ModalityBadge modality={schedule.dojoClass.modality} />
      </div>
      <div className="mt-3 flex flex-wrap gap-2"><Badge>{audienceLabels[schedule.dojoClass.audience]}</Badge>{schedule.effectiveCapacity ? <Badge>{schedule.effectiveCapacity} vagas</Badge> : <Badge>Sem limite</Badge>}</div>
      <div className="mt-4 grid gap-2 text-sm text-mutedForeground">
        <p className="flex items-center gap-2"><UsersRound className="h-4 w-4" aria-hidden="true" />{lead ? lead.displayName : "Professor a definir"}</p>
        <p className="flex items-center gap-2"><MapPin className="h-4 w-4" aria-hidden="true" />{schedule.unit.name}</p>
      </div>
      {canUpdate ? <Button variant="ghost" size="sm" className="mt-3" onClick={() => onEdit(schedule)}>Editar</Button> : null}
    </article>
  );
}

function ModalityBadge({ modality }: { modality: DojoClass["modality"] }) {
  const tone = modality === "NO_GI" ? "primary" : modality === "MIXED" ? "info" : "neutral";
  return <Badge tone={tone}>{modalityLabels[modality]}</Badge>;
}

function trimTime(value?: string) {
  return value?.slice(0, 5);
}

function todayDay(): DayOfWeek {
  const map: DayOfWeek[] = ["SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"];
  return map[new Date().getDay()];
}

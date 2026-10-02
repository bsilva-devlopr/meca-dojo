"use client";

import { Badge } from "@/src/components/ui/Badge";
import { Button } from "@/src/components/ui/Button";
import { Card, CardHeader } from "@/src/components/ui/Card";
import { Input, Select, Textarea } from "@/src/components/ui/Form";
import { EmptyState, ErrorState, LoadingState } from "@/src/components/ui/States";
import { bffJson, hasPermission } from "@/src/lib/people/client";
import { type StaffResponse } from "@/src/lib/people/types";
import {
  getActiveGraduationStaff,
  getBelts,
  getCurrentGraduation,
  getGraduationHistory,
  type BeltResponse,
  type CurrentGraduation,
  type GraduationEvent,
} from "@/src/lib/graduation/types";
import { AlertTriangle, CheckCircle2, Circle, Plus, ShieldCheck } from "lucide-react";
import { type FormEvent, type ReactNode, useCallback, useEffect, useMemo, useState } from "react";
import { BeltBadge } from "./BeltBadge";

export function GraduationPanel({ studentId, permissions }: { studentId: string; permissions: string[] }) {
  const [current, setCurrent] = useState<CurrentGraduation | null>(null);
  const [history, setHistory] = useState<GraduationEvent[]>([]);
  const [belts, setBelts] = useState<BeltResponse[]>([]);
  const [staff, setStaff] = useState<StaffResponse[]>([]);
  const [mode, setMode] = useState<"initial" | "degree" | "belt" | "">("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  const canRead = hasPermission(permissions, "GRADUATION_READ");
  const canCreate = hasPermission(permissions, "GRADUATION_CREATE");

  const load = useCallback(async () => {
    if (!canRead) return;
    setLoading(true);
    setError("");
    try {
      const [currentData, historyData, beltData, staffData] = await Promise.all([
        getCurrentGraduation(studentId),
        getGraduationHistory(studentId),
        getBelts(),
        getActiveGraduationStaff(),
      ]);
      setCurrent(currentData);
      setHistory(historyData.content);
      setBelts(beltData);
      setStaff(staffData);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Erro ao carregar graduacao.");
    } finally {
      setLoading(false);
    }
  }, [canRead, studentId]);

  useEffect(() => {
    void Promise.resolve().then(load);
  }, [load]);

  const nextBelt = useMemo(() => {
    if (!current?.current) return null;
    return belts.find((belt) => belt.system === current.current?.belt.system && belt.sequence === current.current.belt.sequence + 1) ?? null;
  }, [belts, current]);

  if (!canRead) return null;

  async function submitInitial(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    await submit(`/students/${studentId}/graduations/initial`, {
      beltCode: String(form.get("beltCode") ?? ""),
      system: String(form.get("system") ?? "GENERAL"),
      degree: Number(form.get("degree") ?? 0),
      graduatedAt: blank(form.get("graduatedAt")),
      awardedByStaffId: blank(form.get("awardedByStaffId")),
      notes: blank(form.get("notes")),
    }, "Graduacao inicial registrada.");
  }

  async function submitDegree(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    await submit(`/students/${studentId}/graduations/degree`, {
      graduatedAt: String(form.get("graduatedAt") ?? ""),
      awardedByStaffId: blank(form.get("awardedByStaffId")),
      notes: blank(form.get("notes")),
    }, "Grau adicionado.");
  }

  async function submitBelt(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    await submit(`/students/${studentId}/graduations/belt`, {
      beltCode: String(form.get("beltCode") ?? nextBelt?.code ?? ""),
      system: String(form.get("system") ?? nextBelt?.system ?? "GENERAL"),
      graduatedAt: String(form.get("graduatedAt") ?? ""),
      awardedByStaffId: blank(form.get("awardedByStaffId")),
      notes: blank(form.get("notes")),
    }, "Faixa promovida.");
  }

  async function submit(path: string, body: unknown, message: string) {
    setSubmitting(true);
    setError("");
    setSuccess("");
    try {
      await bffJson(path, { method: "POST", body: JSON.stringify(body) });
      setMode("");
      setSuccess(message);
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Erro ao salvar graduacao.");
    } finally {
      setSubmitting(false);
    }
  }

  const today = new Date().toISOString().slice(0, 10);
  const canAddDegree = Boolean(current?.current && current.current.degree < current.current.belt.maxDegree);

  return (
    <div className="space-y-5">
      {error ? <ErrorState message={error} /> : null}
      {success ? <div className="flex items-center gap-2 rounded-lg border border-success/20 bg-success/10 p-3 text-sm font-medium text-success"><CheckCircle2 className="h-4 w-4" aria-hidden="true" />{success}</div> : null}

      {loading ? <LoadingState label="Carregando graduacao" /> : null}

      {!loading && current?.current ? (
        <Card className="bg-gradient-to-br from-surface to-surfaceElevated">
          <CardHeader
            eyebrow="Graduacao atual"
            title="Evolucao no tatame"
            action={canCreate ? (
              <div className="flex flex-wrap gap-2">
                {canAddDegree ? <Button onClick={() => setMode("degree")}><Plus className="h-4 w-4" aria-hidden="true" />Adicionar grau</Button> : null}
                {nextBelt ? <Button variant="secondary" onClick={() => setMode("belt")}>Promover faixa</Button> : null}
              </div>
            ) : null}
          />
          <div className="grid gap-5 lg:grid-cols-[1.25fr_0.75fr]">
            <div>
              <BeltBadge belt={current.current.belt} degree={current.current.degree} hero />
              <dl className="mt-5 grid gap-3 sm:grid-cols-2">
                <Meta label="Graduado em" value={formatDate(current.current.graduatedAt)} />
                <Meta label="Professor" value={current.current.awardedBy?.name ?? "Nao informado"} />
              </dl>
            </div>
            <EligibilityBox current={current} />
          </div>
        </Card>
      ) : null}

      {!loading && !current?.current ? (
        <EmptyState
          title="Graduacao ainda nao cadastrada"
          description="Registre a graduacao atual do aluno para iniciar um historico de evolucao confiavel."
          action={canCreate ? <Button onClick={() => setMode("initial")}>Registrar graduacao</Button> : null}
        />
      ) : null}

      {mode === "initial" ? <InitialForm belts={belts} staff={staff} today={today} submitting={submitting} onSubmit={submitInitial} onCancel={() => setMode("")} /> : null}
      {mode === "degree" && current?.current ? <DegreeForm current={current.current} staff={staff} today={today} submitting={submitting} onSubmit={submitDegree} onCancel={() => setMode("")} /> : null}
      {mode === "belt" && current?.current && nextBelt ? <BeltForm current={current.current} nextBelt={nextBelt} staff={staff} today={today} submitting={submitting} onSubmit={submitBelt} onCancel={() => setMode("")} /> : null}

      <Card>
        <CardHeader title="Historico de graduacao" eyebrow="Timeline" />
        {history.length > 0 ? (
          <div className="space-y-0">
            {history.map((item, index) => <TimelineItem key={item.id} item={item} last={index === history.length - 1} />)}
          </div>
        ) : (
          <p className="text-sm text-mutedForeground">Nenhum evento de graduacao registrado.</p>
        )}
      </Card>
    </div>
  );
}

function Meta({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-xl border border-border bg-surface/75 p-3">
      <dt className="caption-text">{label}</dt>
      <dd className="mt-1 text-sm font-semibold text-foreground">{value}</dd>
    </div>
  );
}

function EligibilityBox({ current }: { current: CurrentGraduation }) {
  const eligibility = current.eligibility;
  return (
    <div className="rounded-2xl border border-border bg-surface p-4">
      <div className="flex items-center gap-3">
        <div className="grid h-10 w-10 place-items-center rounded-full bg-primary/10 text-primary"><ShieldCheck className="h-5 w-5" aria-hidden="true" /></div>
        <div>
          <p className="caption-text">Elegibilidade para avaliacao</p>
          <p className="font-semibold text-foreground">{eligibility.eligible ? "Pode ser avaliado" : "Ainda nao elegivel"}</p>
        </div>
      </div>
      <p className="mt-3 text-sm leading-6 text-mutedForeground">Elegibilidade indica apenas possibilidade de avaliacao. A promocao sempre depende da decisao do professor.</p>
      <div className="mt-4 space-y-2 text-sm">
        {eligibility.reasons.map((reason) => <p key={reason} className="flex gap-2 text-warning"><AlertTriangle className="mt-0.5 h-4 w-4 shrink-0" aria-hidden="true" />{reason}</p>)}
        {eligibility.warnings.map((warning) => <p key={warning} className="flex gap-2 text-info"><AlertTriangle className="mt-0.5 h-4 w-4 shrink-0" aria-hidden="true" />{warning}</p>)}
        {eligibility.reasons.length === 0 && eligibility.warnings.length === 0 ? <p className="flex gap-2 text-success"><CheckCircle2 className="mt-0.5 h-4 w-4 shrink-0" aria-hidden="true" />Sem bloqueios configurados.</p> : null}
      </div>
    </div>
  );
}

function TimelineItem({ item, last }: { item: GraduationEvent; last: boolean }) {
  const correction = item.type === "CORRECTION";
  return (
    <div className="grid grid-cols-[28px_1fr] gap-3">
      <div className="flex flex-col items-center">
        <Circle className={correction ? "h-4 w-4 fill-warning text-warning" : "h-4 w-4 fill-primary text-primary"} aria-hidden="true" />
        {!last ? <div className="mt-1 h-full w-px bg-border" /> : null}
      </div>
      <div className="pb-5">
        <div className="flex flex-wrap items-center gap-2">
          <p className="text-sm font-semibold text-foreground">{formatDate(item.graduatedAt)}</p>
          <Badge tone={correction ? "warning" : "primary"}>{graduationTypeLabel(item.type)}</Badge>
        </div>
        <p className="mt-1 font-medium text-foreground">{item.belt.name} · {item.degree} {item.degree === 1 ? "grau" : "graus"}</p>
        <p className="text-sm text-mutedForeground">Professor: {item.awardedBy?.name ?? "Nao informado"}</p>
        {item.correctionReason ? <p className="mt-2 rounded-lg bg-warning/10 p-2 text-sm text-warning">Correcao: {item.correctionReason}</p> : null}
      </div>
    </div>
  );
}

function InitialForm({ belts, staff, today, submitting, onSubmit, onCancel }: FormProps & { belts: BeltResponse[] }) {
  return (
    <GraduationForm title="Registrar graduacao inicial" onSubmit={onSubmit} onCancel={onCancel} submitting={submitting}>
      <Select name="system" defaultValue="GENERAL" required><option value="GENERAL">Adulto / geral</option><option value="KIDS">Infantil</option></Select>
      <Select name="beltCode" required>{belts.map((belt) => <option key={`${belt.system}-${belt.code}`} value={belt.code}>{belt.name} ({belt.system})</option>)}</Select>
      <Input name="degree" type="number" min="0" max="6" defaultValue="0" />
      <SharedFields staff={staff} today={today} />
    </GraduationForm>
  );
}

function DegreeForm({ current, staff, today, submitting, onSubmit, onCancel }: FormProps & { current: GraduationEvent }) {
  return <GraduationForm title="Adicionar grau" onSubmit={onSubmit} onCancel={onCancel} submitting={submitting}><p className="rounded-xl bg-surfaceElevated p-3 text-sm">{current.belt.name}: {current.degree} para {current.degree + 1} graus</p><SharedFields staff={staff} today={today} /></GraduationForm>;
}

function BeltForm({ current, nextBelt, staff, today, submitting, onSubmit, onCancel }: FormProps & { current: GraduationEvent; nextBelt: BeltResponse }) {
  return <GraduationForm title="Promover faixa" onSubmit={onSubmit} onCancel={onCancel} submitting={submitting}><p className="rounded-xl bg-surfaceElevated p-3 text-sm">Atual: {current.belt.name} · {current.degree} graus<br />Nova: {nextBelt.name} · 0 graus</p><input type="hidden" name="beltCode" value={nextBelt.code} /><input type="hidden" name="system" value={nextBelt.system} /><SharedFields staff={staff} today={today} /></GraduationForm>;
}

type FormProps = { staff: StaffResponse[]; today: string; submitting: boolean; onSubmit: (event: FormEvent<HTMLFormElement>) => void; onCancel: () => void; };

function GraduationForm({ title, onSubmit, onCancel, submitting, children }: { title: string; submitting: boolean; onSubmit: (event: FormEvent<HTMLFormElement>) => void; onCancel: () => void; children: ReactNode }) {
  return <Card><form onSubmit={onSubmit} className="grid gap-3 sm:grid-cols-2"><h3 className="card-title sm:col-span-2">{title}</h3>{children}<div className="flex gap-2 sm:col-span-2"><Button disabled={submitting}>{submitting ? "Salvando..." : "Confirmar"}</Button><Button type="button" variant="secondary" onClick={onCancel}>Cancelar</Button></div></form></Card>;
}

function SharedFields({ staff, today }: { staff: StaffResponse[]; today: string }) {
  return <><Input name="graduatedAt" type="date" defaultValue={today} required /><Select name="awardedByStaffId"><option value="">Professor responsavel</option>{staff.map((person) => <option key={person.id} value={person.id}>{person.displayName} ({person.type})</option>)}</Select><Textarea name="notes" placeholder="Observacao" className="sm:col-span-2" /></>;
}

function graduationTypeLabel(type: GraduationEvent["type"]) {
  return { INITIAL: "Graduacao inicial", DEGREE_PROMOTION: "Grau", BELT_PROMOTION: "Nova faixa", CORRECTION: "Correcao" }[type];
}

function formatDate(value: string | null) {
  if (!value) return "Data nao informada";
  const [year, month, day] = value.split("-");
  return `${day}/${month}/${year}`;
}

function blank(value: FormDataEntryValue | null) {
  const text = String(value ?? "").trim();
  return text ? text : null;
}

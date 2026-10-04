"use client";

import { Badge, StatusBadge } from "@/src/components/ui/Badge";
import { Button, ButtonLink } from "@/src/components/ui/Button";
import { Card, CardHeader } from "@/src/components/ui/Card";
import { Input, Select } from "@/src/components/ui/Form";
import { EmptyState, ErrorState, LoadingState, PageHeader } from "@/src/components/ui/States";
import { bffJson, hasPermission } from "@/src/lib/people/client";
import { type StudentAttendanceHistoryItem, type StudentAttendanceSummary } from "@/src/lib/attendance";
import { type GuardianRelationship, type StudentResponse } from "@/src/lib/people/types";
import { CalendarDays, Mail, MapPin, Phone, UserRound } from "lucide-react";
import { useParams } from "next/navigation";
import { type FormEvent, type ReactNode, useCallback, useEffect, useState } from "react";
import { GraduationPanel } from "./graduation/GraduationPanel";

type Me = { permissions: string[] };

export default function StudentProfilePage() {
  const params = useParams<{ id: string }>();
  const [student, setStudent] = useState<StudentResponse | null>(null);
  const [me, setMe] = useState<Me | null>(null);
  const [error, setError] = useState("");
  const [attendanceSummary, setAttendanceSummary] = useState<StudentAttendanceSummary | null>(null);
  const [attendanceHistory, setAttendanceHistory] = useState<StudentAttendanceHistoryItem[]>([]);
  const [guardianMessage, setGuardianMessage] = useState("");

  const load = useCallback(() => {
    bffJson<StudentResponse>(`/students/${params.id}`).then(setStudent).catch((err: Error) => setError(err.message));
    bffJson<StudentAttendanceSummary>(`/students/${params.id}/attendance/summary`).then(setAttendanceSummary).catch(() => undefined);
    bffJson<StudentAttendanceHistoryItem[]>(`/students/${params.id}/attendance`).then(setAttendanceHistory).catch(() => undefined);
  }, [params.id]);

  useEffect(() => {
    fetch("/api/auth/me").then((r) => r.json()).then(setMe).catch(() => undefined);
    load();
  }, [load]);

  async function addGuardian(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
    setGuardianMessage("");
    const form = new FormData(event.currentTarget);
    try {
      await bffJson(`/students/${params.id}/guardians/new`, {
        method: "POST",
        body: JSON.stringify({
          fullName: String(form.get("fullName") ?? "").trim(),
          email: blank(form.get("email")),
          phone: String(form.get("phone") ?? "").trim(),
          relationship: String(form.get("relationship") ?? "OTHER") as GuardianRelationship,
          primary: form.get("primary") === "on",
        }),
      });
      event.currentTarget.reset();
      setGuardianMessage("Responsavel adicionado.");
      load();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Erro ao adicionar responsavel.");
    }
  }

  if (!student) {
    return error ? <ErrorState message={error} /> : <LoadingState label="Carregando perfil" />;
  }

  const canUpdate = me ? hasPermission(me.permissions, "STUDENT_UPDATE") : false;
  const canCreateGuardian = me ? hasPermission(me.permissions, "GUARDIAN_CREATE") : false;

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Perfil do aluno"
        title={student.fullName}
        description={`${student.preferredName || "Aluno"} Â· aluno desde ${formatDate(student.joinDate)} Â· ${student.primaryUnit.name}`}
        action={canUpdate ? <ButtonLink href={`/app/students/${student.id}/edit`} variant="secondary">Editar aluno</ButtonLink> : null}
      />

      {error ? <ErrorState message={error} /> : null}

      <Card className="overflow-hidden bg-foreground p-0 text-white">
        <div className="grid gap-6 p-6 lg:grid-cols-[1fr_auto] lg:p-8">
          <div>
            <div className="flex flex-wrap items-center gap-3"><StatusBadge status={student.status} /><Badge tone="primary">{student.primaryUnit.name}</Badge></div>
            <h2 className="mt-6 text-4xl font-semibold tracking-tight">{student.fullName}</h2>
            <p className="mt-2 text-white/62">{student.preferredName || "Sem nome preferido"}</p>
          </div>
          <div className="grid gap-3 sm:grid-cols-2 lg:w-96">
            <HeroMetric icon={<CalendarDays className="h-4 w-4" />} label="Desde" value={formatDate(student.joinDate)} />
            <HeroMetric icon={<MapPin className="h-4 w-4" />} label="Unidade" value={student.primaryUnit.name} />
          </div>
        </div>
      </Card>

      <section className="grid gap-4 lg:grid-cols-3">
        <InfoCard title="Dados pessoais" items={[['Nome preferido', student.preferredName || '-'], ['Nascimento', formatDate(student.birthDate)]]} />
        <InfoCard title="Contato" items={[['E-mail', student.email || '-'], ['Telefone', student.phone || '-']]} />
        <InfoCard title="Acesso ao sistema" items={[[student.user ? student.user.name : 'Status', student.user ? student.user.email : 'Sem usuario vinculado']]} />
      </section>

      {attendanceSummary ? <FrequencySection summary={attendanceSummary} history={attendanceHistory} /> : null}

      {me ? <GraduationPanel studentId={student.id} permissions={me.permissions} /> : null}

      <Card>
        <CardHeader title="Responsaveis" eyebrow="Familia e contatos" />
        {guardianMessage ? <div className="mb-4 rounded-lg border border-success/20 bg-success/10 p-3 text-sm font-medium text-success">{guardianMessage}</div> : null}
        <div className="grid gap-3 md:grid-cols-2">
          {student.guardians.map((guardian) => (
            <div key={guardian.id} className="rounded-2xl border border-border bg-surfaceElevated p-4">
              <div className="flex items-start justify-between gap-3">
                <div>
                  <p className="font-semibold text-foreground">{guardian.fullName}</p>
                  <p className="mt-1 text-sm text-mutedForeground">{guardian.relationship} Â· {guardian.phone}</p>
                </div>
                {guardian.primary ? <Badge tone="primary">Principal</Badge> : null}
              </div>
            </div>
          ))}
        </div>
        {student.guardians.length === 0 ? <EmptyState title="Nenhum responsavel vinculado" description="Adicione responsaveis quando o aluno precisar de contato familiar ou legal." className="mt-3" /> : null}

        {canCreateGuardian ? (
          <form onSubmit={addGuardian} className="mt-5 grid gap-3 border-t border-border pt-5 md:grid-cols-2">
            <Input name="fullName" required placeholder="Nome do responsavel" />
            <Input name="phone" required placeholder="Telefone" />
            <Input name="email" type="email" placeholder="E-mail" />
            <Select name="relationship">
              {(["FATHER", "MOTHER", "LEGAL_GUARDIAN", "GRANDPARENT", "OTHER"] satisfies GuardianRelationship[]).map((value) => <option key={value}>{value}</option>)}
            </Select>
            <label className="flex items-center gap-2 text-sm text-mutedForeground"><input name="primary" type="checkbox" className="h-4 w-4 accent-primary" /> Responsavel principal</label>
            <Button className="md:justify-self-start">Adicionar responsavel</Button>
          </form>
        ) : null}
      </Card>

      <section className="grid gap-4 lg:grid-cols-2">
        <Card>
          <CardHeader title="Contato de emergencia" eyebrow="Seguranca" />
          {student.emergencyContact ? (
            <div className="space-y-2 text-sm text-mutedForeground">
              <p className="font-semibold text-foreground">{student.emergencyContact.name}</p>
              <p>{student.emergencyContact.relationship} Â· {student.emergencyContact.phone}</p>
            </div>
          ) : <p className="text-sm text-mutedForeground">Nao informado.</p>}
        </Card>
        <Card>
          <CardHeader title="Observacoes" eyebrow="Administrativo" />
          <p className="whitespace-pre-wrap text-sm leading-6 text-mutedForeground">{student.notes || "-"}</p>
        </Card>
      </section>
    </div>
  );
}


function FrequencySection({ summary, history }: { summary: StudentAttendanceSummary; history: StudentAttendanceHistoryItem[] }) {
  return (
    <Card>
      <CardHeader title="Frequencia" eyebrow="Dados reais de treino" />
      <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
        <Metric label="Total de treinos" value={summary.totalTrainings} />
        <Metric label="Ultimos 30 dias" value={summary.last30Days} />
        <Metric label="Ultimos 90 dias" value={summary.last90Days} />
        <Metric label="Este mes" value={summary.currentMonth} />
      </div>
      <div className="mt-4 grid gap-3 sm:grid-cols-2">
        <Metric label="Gi" value={summary.giCount} subtle />
        <Metric label="No-Gi" value={summary.noGiCount} subtle />
      </div>
      <div className="mt-6 border-t border-border pt-5">
        <h3 className="font-semibold text-foreground">Historico de treinos</h3>
        {history.length === 0 ? <p className="mt-2 text-sm text-mutedForeground">Nenhum treino registrado ainda.</p> : (
          <div className="mt-3 grid gap-3">
            {history.slice(0, 6).map((item) => (
              <div key={item.attendanceId} className="rounded-2xl border border-border bg-surfaceElevated p-4">
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <p className="font-semibold text-foreground">{item.trainingSession.dojoClass.name}</p>
                    <p className="mt-1 text-sm text-mutedForeground">{formatDate(item.trainingSession.sessionDate)} Â· {item.trainingSession.startTime.slice(0, 5)} Â· {item.trainingSession.unit.name}</p>
                  </div>
                  <Badge tone={item.source === "QR" ? "primary" : "neutral"}>{item.source}</Badge>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </Card>
  );
}

function Metric({ label, value, subtle = false }: { label: string; value: number; subtle?: boolean }) {
  return <div className={`rounded-2xl border border-border p-4 ${subtle ? "bg-surfaceElevated" : "bg-foreground text-white"}`}><p className={subtle ? "caption-text" : "text-xs uppercase tracking-[0.16em] text-white/55"}>{label}</p><p className={`mt-2 text-3xl font-semibold ${subtle ? "text-foreground" : "text-white"}`}>{value}</p></div>;
}
function HeroMetric({ icon, label, value }: { icon: ReactNode; label: string; value: string }) {
  return <div className="rounded-2xl border border-white/10 bg-white/6 p-4"><div className="flex items-center gap-2 text-white/55">{icon}<span className="text-xs uppercase tracking-[0.16em]">{label}</span></div><p className="mt-2 font-semibold text-white">{value}</p></div>;
}

function InfoCard({ title, items }: { title: string; items: Array<[string, string]> }) {
  const icons = [UserRound, Mail, Phone];
  return <Card><CardHeader title={title} /> <div className="space-y-3">{items.map(([label, value], index) => { const Icon = icons[index] ?? UserRound; return <div key={label} className="flex gap-3"><div className="grid h-9 w-9 shrink-0 place-items-center rounded-full bg-primary/10 text-primary"><Icon className="h-4 w-4" aria-hidden="true" /></div><div><p className="caption-text">{label}</p><p className="text-sm font-medium text-foreground">{value}</p></div></div>; })}</div></Card>;
}

function formatDate(value: string | null) {
  if (!value) return "-";
  const [year, month, day] = value.split("-");
  return `${day}/${month}/${year}`;
}

function blank(value: FormDataEntryValue | null) {
  const text = String(value ?? "").trim();
  return text ? text : null;
}


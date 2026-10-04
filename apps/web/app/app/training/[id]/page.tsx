"use client";

import { Badge } from "@/src/components/ui/Badge";
import { Button, ButtonLink } from "@/src/components/ui/Button";
import { Card, CardHeader } from "@/src/components/ui/Card";
import { FormField, Input, Textarea } from "@/src/components/ui/Form";
import { EmptyState, ErrorState, LoadingState, PageHeader } from "@/src/components/ui/States";
import { attendanceSourceLabels, type AttendanceResponse, type CheckInTokenResponse } from "@/src/lib/attendance";
import { bffJson, hasPermission } from "@/src/lib/people/client";
import { type PageResponse, type StudentListItem } from "@/src/lib/people/types";
import { audienceLabels, modalityLabels } from "@/src/lib/schedule/types";
import { trainingStatusLabels, trainingStatusTone, type TrainingSession } from "@/src/lib/training/types";
import { CheckCircle2, Pencil, Play, QrCode, Search, UserPlus, XCircle } from "lucide-react";
import Image from "next/image";
import { useParams } from "next/navigation";
import QRCode from "qrcode";
import { type FormEvent, useCallback, useEffect, useMemo, useState } from "react";

type Me = { permissions: string[] };
type PendingAction = "open" | "complete" | "cancel" | null;

export default function TrainingSessionDetailPage() {
  const params = useParams<{ id: string }>();
  const [me, setMe] = useState<Me | null>(null);
  const [session, setSession] = useState<TrainingSession | null>(null);
  const [pendingAction, setPendingAction] = useState<PendingAction>(null);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [saving, setSaving] = useState(false);

  const load = useCallback(() => {
    setError("");
    bffJson<TrainingSession>(`/training-sessions/${params.id}`).then(setSession).catch((err: Error) => setError(err.message));
  }, [params.id]);

  useEffect(() => { fetch("/api/auth/me").then((r) => r.json()).then(setMe).catch(() => undefined); }, []);
  useEffect(() => { void Promise.resolve().then(load); }, [load]);

  const permissions = me?.permissions ?? [];
  const canOpen = hasPermission(permissions, "TRAINING_SESSION_OPEN");
  const canComplete = hasPermission(permissions, "TRAINING_SESSION_COMPLETE");
  const canCancel = hasPermission(permissions, "TRAINING_SESSION_CANCEL");
  const canUpdate = hasPermission(permissions, "TRAINING_SESSION_UPDATE");
  const canAttendanceRead = hasPermission(permissions, "ATTENDANCE_READ");
  const canAttendanceCreate = hasPermission(permissions, "ATTENDANCE_CREATE");
  const canAttendanceVoid = hasPermission(permissions, "ATTENDANCE_VOID");
  const canQr = hasPermission(permissions, "CHECKIN_TOKEN_CREATE");

  async function execute(action: "open" | "complete") {
    setSaving(true);
    setError("");
    setMessage("");
    try {
      const updated = await bffJson<TrainingSession>(`/training-sessions/${params.id}/${action}`, { method: "POST" });
      setSession(updated);
      setPendingAction(null);
      setMessage(action === "open" ? "Treino aberto." : "Treino encerrado.");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Erro ao atualizar treino.");
    } finally {
      setSaving(false);
    }
  }

  async function cancel(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSaving(true);
    setError("");
    setMessage("");
    const form = new FormData(event.currentTarget);
    try {
      const updated = await bffJson<TrainingSession>(`/training-sessions/${params.id}/cancel`, { method: "POST", body: JSON.stringify({ reason: String(form.get("reason") ?? "").trim() }) });
      setSession(updated);
      setPendingAction(null);
      setMessage("Treino cancelado.");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Erro ao cancelar treino.");
    } finally {
      setSaving(false);
    }
  }

  if (!session && !error) return <LoadingState label="Carregando treino" />;

  return (
    <div className="space-y-6">
      {session ? (
        <>
          <PageHeader eyebrow="Treino" title={session.dojoClass.name} description={`${formatDate(session.sessionDate)} - ${time(session.startTime)} a ${time(session.endTime)}`} action={canUpdate && session.status === "SCHEDULED" ? <ButtonLink href={`/app/training/${session.id}/edit`} variant="secondary"><Pencil className="h-4 w-4" aria-hidden="true" />Editar</ButtonLink> : null} />
          {message ? <div className="rounded-lg border border-success/20 bg-success/10 p-3 text-sm font-medium text-success">{message}</div> : null}
          {error ? <ErrorState message={error} /> : null}
          <Card className="p-5">
            <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
              <div>
                <Badge tone={trainingStatusTone[session.status]}>{trainingStatusLabels[session.status]}</Badge>
                <div className="mt-4 flex flex-wrap gap-2"><Badge tone={session.dojoClass.modality === "NO_GI" ? "primary" : "neutral"}>{modalityLabels[session.dojoClass.modality]}</Badge><Badge>{audienceLabels[session.dojoClass.audience]}</Badge>{session.classSchedule ? <Badge>Gerado da grade</Badge> : <Badge>Treino manual</Badge>}</div>
              </div>
              <div className="flex flex-col gap-2 sm:flex-row">
                {session.status === "SCHEDULED" && canOpen ? <Button type="button" onClick={() => setPendingAction("open")}><Play className="h-4 w-4" aria-hidden="true" />Abrir treino</Button> : null}
                {session.status === "OPEN" && canComplete ? <Button type="button" onClick={() => setPendingAction("complete")}><CheckCircle2 className="h-4 w-4" aria-hidden="true" />Encerrar treino</Button> : null}
                {session.status === "OPEN" && canQr ? <Button type="button" variant="secondary" onClick={() => document.getElementById("checkin-qr")?.scrollIntoView({ behavior: "smooth" })}><QrCode className="h-4 w-4" aria-hidden="true" />Exibir QR</Button> : null}
                {(session.status === "SCHEDULED" || session.status === "OPEN") && canCancel ? <Button type="button" variant="ghost" onClick={() => setPendingAction("cancel")}><XCircle className="h-4 w-4" aria-hidden="true" />Cancelar</Button> : null}
              </div>
            </div>
          </Card>
          {pendingAction ? <Card className="border-primary/30 p-5">{pendingAction === "open" ? <Confirm title="Abrir treino?" description="O treino ficara em andamento e podera receber presencas." disabled={saving} onCancel={() => setPendingAction(null)} onConfirm={() => execute("open")} /> : null}{pendingAction === "complete" ? <Confirm title="Encerrar treino?" description="As presencas registradas continuarao contando no historico do aluno." disabled={saving} onCancel={() => setPendingAction(null)} onConfirm={() => execute("complete")} /> : null}{pendingAction === "cancel" ? <form onSubmit={cancel} className="grid gap-4"><div><h2 className="card-title">Cancelar treino</h2><p className="mt-1 text-sm text-mutedForeground">Informe o motivo para preservar o historico operacional.</p></div><Textarea name="reason" required placeholder="Professor indisponivel." /><div className="flex flex-col gap-2 sm:flex-row"><Button disabled={saving} variant="danger">Confirmar cancelamento</Button><Button type="button" variant="secondary" onClick={() => setPendingAction(null)}>Voltar</Button></div></form> : null}</Card> : null}
          <div className="grid gap-4 lg:grid-cols-2"><InfoCard title="Professor" value={session.staff.find((item) => item.role === "LEAD")?.displayName ?? "A definir"} /><InfoCard title="Assistente" value={session.staff.filter((item) => item.role === "ASSISTANT").map((item) => item.displayName).join(", ") || "Sem assistente"} /><InfoCard title="Unidade" value={session.unit.name} /><InfoCard title="Capacidade" value={session.capacity ? `${session.capacity} vagas` : "Sem limite configurado"} /></div>
          {session.status === "OPEN" && canQr ? <QrPanel session={session} /> : null}
          {canAttendanceRead ? <AttendancePanel session={session} canCreate={canAttendanceCreate && session.status === "OPEN"} canVoid={canAttendanceVoid} /> : null}
          <Card><p className="caption-text">Observacoes</p><p className="mt-2 text-sm leading-6 text-foreground">{session.notes || "Sem observacoes operacionais."}</p></Card>
          {session.status === "CANCELLED" ? <Card className="border-danger/20 bg-danger/5"><p className="caption-text text-danger">Cancelamento</p><p className="mt-2 text-sm leading-6 text-foreground">{session.cancellationReason}</p><p className="mt-2 text-xs text-mutedForeground">{session.cancelledBy ? `Cancelado por ${session.cancelledBy.name}` : "Cancelado"}{session.cancelledAt ? ` em ${new Date(session.cancelledAt).toLocaleString("pt-BR")}` : ""}</p></Card> : null}
        </>
      ) : null}
      {!session && error ? <ErrorState message={error} /> : null}
    </div>
  );
}

function AttendancePanel({ session, canCreate, canVoid }: { session: TrainingSession; canCreate: boolean; canVoid: boolean }) {
  const [items, setItems] = useState<AttendanceResponse[]>([]);
  const [students, setStudents] = useState<StudentListItem[]>([]);
  const [query, setQuery] = useState("");
  const [selected, setSelected] = useState("");
  const [voiding, setVoiding] = useState<AttendanceResponse | null>(null);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);
  const load = useCallback(() => { bffJson<AttendanceResponse[]>(`/training-sessions/${session.id}/attendance`).then(setItems).catch((err: Error) => setError(err.message)); }, [session.id]);
  useEffect(() => { load(); }, [load]);
  useEffect(() => { if (!canCreate || query.trim().length < 2) { const emptyTimer = window.setTimeout(() => setStudents([]), 0); return () => window.clearTimeout(emptyTimer); } const timer = window.setTimeout(() => { bffJson<PageResponse<StudentListItem>>(`/students?search=${encodeURIComponent(query.trim())}&page=0&size=8`).then((page) => setStudents(page.content.filter((student) => student.status === "ACTIVE" || student.status === "TRIAL"))).catch(() => setStudents([])); }, 250); return () => window.clearTimeout(timer); }, [canCreate, query]);
  async function addAttendance(event: FormEvent<HTMLFormElement>) { event.preventDefault(); if (!selected) return; setSaving(true); setError(""); setMessage(""); try { await bffJson<AttendanceResponse>(`/training-sessions/${session.id}/attendance`, { method: "POST", body: JSON.stringify({ studentId: selected }) }); setQuery(""); setSelected(""); setStudents([]); setMessage("Presenca registrada."); load(); } catch (err) { setError(err instanceof Error ? err.message : "Erro ao registrar presenca."); } finally { setSaving(false); } }
  async function voidAttendance(event: FormEvent<HTMLFormElement>) { event.preventDefault(); if (!voiding) return; const form = new FormData(event.currentTarget); setSaving(true); setError(""); setMessage(""); try { await bffJson<AttendanceResponse>(`/attendance/${voiding.id}/void`, { method: "POST", body: JSON.stringify({ reason: String(form.get("reason") ?? "").trim() }) }); setVoiding(null); setMessage("Presenca corrigida e removida da contagem."); load(); } catch (err) { setError(err instanceof Error ? err.message : "Erro ao corrigir presenca."); } finally { setSaving(false); } }
  return <Card className="space-y-5"><div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between"><CardHeader title="Presencas" eyebrow="Tatame" /><Badge tone="success">{items.length} presente(s)</Badge></div>{message ? <div className="rounded-lg border border-success/20 bg-success/10 p-3 text-sm font-medium text-success">{message}</div> : null}{error ? <ErrorState message={error} /> : null}{canCreate ? <form onSubmit={addAttendance} className="rounded-2xl border border-border bg-surfaceElevated p-4"><FormField label="Adicionar aluno" hint="Busque por nome; alunos inativos ou suspensos nao aparecem aqui."><div className="relative"><Search className="pointer-events-none absolute left-3 top-3 h-4 w-4 text-mutedForeground" aria-hidden="true" /><Input value={query} onChange={(event) => { setQuery(event.target.value); setSelected(""); }} className="pl-9" placeholder="Digite pelo menos 2 letras" /></div></FormField>{students.length > 0 ? <div className="mt-3 grid gap-2">{students.map((student) => <button key={student.id} type="button" onClick={() => { setSelected(student.id); setQuery(student.fullName); }} className={`focus-ring rounded-xl border p-3 text-left text-sm ${selected === student.id ? "border-primary bg-primary/10" : "border-border bg-surface"}`}><span className="font-semibold text-foreground">{student.fullName}</span><span className="ml-2 text-mutedForeground">{student.status === "TRIAL" ? "Trial" : "Ativo"}</span></button>)}</div> : null}<Button disabled={!selected || saving} className="mt-3"><UserPlus className="h-4 w-4" aria-hidden="true" />Registrar presenca</Button></form> : null}{items.length === 0 ? <EmptyState title="Nenhuma presenca registrada" description="Quando o treino estiver aberto, registre alunos manualmente ou exiba o QR de check-in." /> : <div className="grid gap-3">{items.map((attendance) => <div key={attendance.id} className="flex items-center justify-between gap-3 rounded-2xl border border-border bg-surfaceElevated p-4"><div className="flex min-w-0 items-center gap-3"><div className="grid h-10 w-10 shrink-0 place-items-center rounded-full bg-primary/10 text-sm font-semibold text-primary">{initials(attendance.student.fullName)}</div><div className="min-w-0"><p className="truncate font-semibold text-foreground">{attendance.student.fullName}</p><p className="text-xs text-mutedForeground">{new Date(attendance.checkedInAt).toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" })} · {attendanceSourceLabels[attendance.source]}</p></div></div>{canVoid ? <Button type="button" size="sm" variant="ghost" onClick={() => setVoiding(attendance)}>Corrigir</Button> : null}</div>)}</div>}{voiding ? <form onSubmit={voidAttendance} className="rounded-2xl border border-danger/20 bg-danger/5 p-4"><h3 className="font-semibold text-foreground">Corrigir presenca</h3><p className="mt-1 text-sm text-mutedForeground">Esta presenca deixara de contar no historico do aluno. Informe o motivo.</p><Textarea name="reason" required className="mt-3" placeholder="Presenca lancada no aluno errado." /><div className="mt-3 flex flex-col gap-2 sm:flex-row"><Button disabled={saving} variant="danger">Confirmar correcao</Button><Button type="button" variant="secondary" onClick={() => setVoiding(null)}>Voltar</Button></div></form> : null}</Card>;
}

function QrPanel({ session }: { session: TrainingSession }) {
  const [token, setToken] = useState<CheckInTokenResponse | null>(null);
  const [qr, setQr] = useState("");
  const [now, setNow] = useState(() => Date.now());
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const secondsLeft = useMemo(() => token ? Math.max(0, Math.ceil((new Date(token.expiresAt).getTime() - now) / 1000)) : 0, [now, token]);
  const renew = useCallback(async () => { setLoading(true); setError(""); try { const next = await bffJson<CheckInTokenResponse>(`/training-sessions/${session.id}/check-in-token`, { method: "POST" }); setToken(next); setQr(await QRCode.toDataURL(next.checkInUrl, { width: 420, margin: 2, errorCorrectionLevel: "M" })); } catch (err) { setError(err instanceof Error ? err.message : "Erro ao gerar QR."); } finally { setLoading(false); } }, [session.id]);
  useEffect(() => { const id = window.setTimeout(() => { void renew(); }, 0); return () => window.clearTimeout(id); }, [renew]);
  useEffect(() => { const id = window.setInterval(() => setNow(Date.now()), 1000); return () => window.clearInterval(id); }, []);
  useEffect(() => { if (!token || secondsLeft !== 0) return; const id = window.setTimeout(() => { void renew(); }, 0); return () => window.clearTimeout(id); }, [renew, secondsLeft, token]);
  return <Card id="checkin-qr" className="border-primary/20 bg-primary/5"><div className="grid gap-5 lg:grid-cols-[1fr_auto] lg:items-center"><div><p className="caption-text text-primary">CHECK-IN</p><h2 className="mt-2 text-2xl font-semibold tracking-tight text-foreground">Exibir QR</h2><p className="mt-1 text-sm text-mutedForeground">{session.dojoClass.name} · {time(session.startTime)} · {secondsLeft > 0 ? `QR valido por ${secondsLeft}s` : "Renovando QR"}</p>{error ? <div className="mt-3"><ErrorState message={error} /></div> : null}<Button type="button" variant="secondary" onClick={() => void renew()} disabled={loading} className="mt-4">{loading ? "Gerando..." : "Renovar QR"}</Button></div><div className="mx-auto w-full max-w-xs rounded-3xl border border-border bg-white p-4 shadow-card sm:max-w-sm">{qr ? <Image src={qr} alt="QR Code para check-in dos alunos" width={420} height={420} unoptimized className="h-auto w-full" /> : <div className="grid aspect-square place-items-center text-sm text-mutedForeground">Gerando QR...</div>}</div></div></Card>;
}

function Confirm({ title, description, disabled, onCancel, onConfirm }: { title: string; description: string; disabled: boolean; onCancel: () => void; onConfirm: () => void }) { return <div className="grid gap-4"><div><h2 className="card-title">{title}</h2><p className="mt-1 text-sm text-mutedForeground">{description}</p></div><div className="flex flex-col gap-2 sm:flex-row"><Button type="button" disabled={disabled} onClick={onConfirm}>Confirmar</Button><Button type="button" variant="secondary" onClick={onCancel}>Voltar</Button></div></div>; }
function InfoCard({ title, value }: { title: string; value: string }) { return <Card><p className="caption-text">{title}</p><p className="mt-2 text-lg font-semibold text-foreground">{value}</p></Card>; }
function time(value: string) { return value.slice(0, 5); }
function formatDate(value: string) { const date = new Date(`${value}T00:00:00`); return new Intl.DateTimeFormat("pt-BR", { weekday: "long", day: "2-digit", month: "short" }).format(date); }
function initials(name: string) { return name.split(" ").filter(Boolean).slice(0, 2).map((part) => part[0]?.toUpperCase()).join("") || "AL"; }

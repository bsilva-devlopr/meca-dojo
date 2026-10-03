"use client";

import { Badge } from "@/src/components/ui/Badge";
import { Button, ButtonLink } from "@/src/components/ui/Button";
import { Card } from "@/src/components/ui/Card";
import { Textarea } from "@/src/components/ui/Form";
import { ErrorState, LoadingState, PageHeader } from "@/src/components/ui/States";
import { bffJson, hasPermission } from "@/src/lib/people/client";
import { audienceLabels, modalityLabels } from "@/src/lib/schedule/types";
import { trainingStatusLabels, trainingStatusTone, type TrainingSession } from "@/src/lib/training/types";
import { CheckCircle2, Pencil, Play, XCircle } from "lucide-react";
import { useParams } from "next/navigation";
import { type FormEvent, useCallback, useEffect, useState } from "react";

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

  useEffect(() => {
    fetch("/api/auth/me").then((r) => r.json()).then(setMe).catch(() => undefined);
  }, []);

  useEffect(() => { void Promise.resolve().then(load); }, [load]);

  const canOpen = me ? hasPermission(me.permissions, "TRAINING_SESSION_OPEN") : false;
  const canComplete = me ? hasPermission(me.permissions, "TRAINING_SESSION_COMPLETE") : false;
  const canCancel = me ? hasPermission(me.permissions, "TRAINING_SESSION_CANCEL") : false;
  const canUpdate = me ? hasPermission(me.permissions, "TRAINING_SESSION_UPDATE") : false;

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
          <PageHeader
            eyebrow="Treino"
            title={session.dojoClass.name}
            description={`${formatDate(session.sessionDate)} - ${time(session.startTime)} a ${time(session.endTime)}`}
            action={canUpdate && session.status === "SCHEDULED" ? <ButtonLink href={`/app/training/${session.id}/edit`} variant="secondary"><Pencil className="h-4 w-4" aria-hidden="true" />Editar</ButtonLink> : null}
          />

          {message ? <div className="rounded-lg border border-success/20 bg-success/10 p-3 text-sm font-medium text-success">{message}</div> : null}
          {error ? <ErrorState message={error} /> : null}

          <Card className="p-5">
            <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
              <div>
                <Badge tone={trainingStatusTone[session.status]}>{trainingStatusLabels[session.status]}</Badge>
                <div className="mt-4 flex flex-wrap gap-2">
                  <Badge tone={session.dojoClass.modality === "NO_GI" ? "primary" : "neutral"}>{modalityLabels[session.dojoClass.modality]}</Badge>
                  <Badge>{audienceLabels[session.dojoClass.audience]}</Badge>
                  {session.classSchedule ? <Badge>Gerado da grade</Badge> : <Badge>Treino manual</Badge>}
                </div>
              </div>
              <div className="flex flex-col gap-2 sm:flex-row">
                {session.status === "SCHEDULED" && canOpen ? <Button type="button" onClick={() => setPendingAction("open")}><Play className="h-4 w-4" aria-hidden="true" />Abrir treino</Button> : null}
                {session.status === "OPEN" && canComplete ? <Button type="button" onClick={() => setPendingAction("complete")}><CheckCircle2 className="h-4 w-4" aria-hidden="true" />Encerrar treino</Button> : null}
                {(session.status === "SCHEDULED" || session.status === "OPEN") && canCancel ? <Button type="button" variant="ghost" onClick={() => setPendingAction("cancel")}><XCircle className="h-4 w-4" aria-hidden="true" />Cancelar</Button> : null}
              </div>
            </div>
          </Card>

          {pendingAction ? (
            <Card className="border-primary/30 p-5">
              {pendingAction === "open" ? <Confirm title="Abrir treino?" description="O treino ficara em andamento para operacao." disabled={saving} onCancel={() => setPendingAction(null)} onConfirm={() => execute("open")} /> : null}
              {pendingAction === "complete" ? <Confirm title="Encerrar treino?" description="Ainda nao ha presenca neste ciclo; a acao apenas conclui a sessao." disabled={saving} onCancel={() => setPendingAction(null)} onConfirm={() => execute("complete")} /> : null}
              {pendingAction === "cancel" ? (
                <form onSubmit={cancel} className="grid gap-4">
                  <div><h2 className="card-title">Cancelar treino</h2><p className="mt-1 text-sm text-mutedForeground">Informe o motivo para preservar o historico operacional.</p></div>
                  <Textarea name="reason" required placeholder="Professor indisponivel." />
                  <div className="flex flex-col gap-2 sm:flex-row"><Button disabled={saving} variant="danger">Confirmar cancelamento</Button><Button type="button" variant="secondary" onClick={() => setPendingAction(null)}>Voltar</Button></div>
                </form>
              ) : null}
            </Card>
          ) : null}

          <div className="grid gap-4 lg:grid-cols-2">
            <InfoCard title="Professor" value={session.staff.find((item) => item.role === "LEAD")?.displayName ?? "A definir"} />
            <InfoCard title="Assistente" value={session.staff.filter((item) => item.role === "ASSISTANT").map((item) => item.displayName).join(", ") || "Sem assistente"} />
            <InfoCard title="Unidade" value={session.unit.name} />
            <InfoCard title="Capacidade" value={session.capacity ? `${session.capacity} vagas` : "Sem limite configurado"} />
          </div>

          <Card>
            <p className="caption-text">Observacoes</p>
            <p className="mt-2 text-sm leading-6 text-foreground">{session.notes || "Sem observacoes operacionais."}</p>
          </Card>

          {session.status === "CANCELLED" ? (
            <Card className="border-danger/20 bg-danger/5">
              <p className="caption-text text-danger">Cancelamento</p>
              <p className="mt-2 text-sm leading-6 text-foreground">{session.cancellationReason}</p>
              <p className="mt-2 text-xs text-mutedForeground">{session.cancelledBy ? `Cancelado por ${session.cancelledBy.name}` : "Cancelado"}{session.cancelledAt ? ` em ${new Date(session.cancelledAt).toLocaleString("pt-BR")}` : ""}</p>
            </Card>
          ) : null}
        </>
      ) : null}
      {!session && error ? <ErrorState message={error} /> : null}
    </div>
  );
}

function Confirm({ title, description, disabled, onCancel, onConfirm }: { title: string; description: string; disabled: boolean; onCancel: () => void; onConfirm: () => void }) {
  return (
    <div className="grid gap-4">
      <div><h2 className="card-title">{title}</h2><p className="mt-1 text-sm text-mutedForeground">{description}</p></div>
      <div className="flex flex-col gap-2 sm:flex-row"><Button type="button" disabled={disabled} onClick={onConfirm}>Confirmar</Button><Button type="button" variant="secondary" onClick={onCancel}>Voltar</Button></div>
    </div>
  );
}

function InfoCard({ title, value }: { title: string; value: string }) {
  return <Card><p className="caption-text">{title}</p><p className="mt-2 text-lg font-semibold text-foreground">{value}</p></Card>;
}

function time(value: string) {
  return value.slice(0, 5);
}

function formatDate(value: string) {
  const date = new Date(`${value}T00:00:00`);
  return new Intl.DateTimeFormat("pt-BR", { weekday: "long", day: "2-digit", month: "short" }).format(date);
}

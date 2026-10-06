"use client";
import { useEffect, useState } from "react";
import { LogoutButton } from "@/app/app/LogoutButton";
import { BeltBadge } from "@/app/app/students/[id]/graduation/BeltBadge";
import { Card } from "@/src/components/ui/Card";
import { Badge } from "@/src/components/ui/Badge";
import { Button, ButtonLink } from "@/src/components/ui/Button";
import { EmptyState, ErrorState, LoadingState, PageHeader } from "@/src/components/ui/States";
import { bffJson } from "@/src/lib/people/client";
import { sessionDateLabel, shortDate } from "@/src/lib/student/dates";
import type { Dashboard, Graduation, HistoryPage, Session } from "@/src/lib/student/types";

export function StudentExperience({ view = "dashboard" }: { view?: "dashboard" | "history" | "progress" | "profile" }) {
  const [data, setData] = useState<Dashboard | null>(null);
  const [history, setHistory] = useState<HistoryPage | null>(null);
  const [page, setPage] = useState(0);
  const [error, setError] = useState("");
  const [retry, setRetry] = useState(0);
  useEffect(() => {
    let active = true;
    bffJson<Dashboard>("/student/me/dashboard").then(value => { if (active) setData(value); }).catch((cause: Error) => { if (active) setError(cause.message.includes("not linked") ? "unlinked" : "api"); });
    return () => { active = false; };
  }, [retry]);
  useEffect(() => {
    if (view !== "history") return;
    let active = true;
    bffJson<HistoryPage>(`/student/me/training-history?page=${page}&size=10`).then(value => { if (active) setHistory(value); }).catch(() => { if (active) setError("Não foi possível carregar seu histórico."); });
    return () => { active = false; };
  }, [view, page, retry]);
  if (error) return <div className="space-y-4"><ErrorState message={error === "unlinked" ? "Seu acesso ainda não está vinculado a um aluno. Procure a recepção ou professor." : "Não foi possível carregar sua área. Tente novamente em instantes."} /><Button onClick={() => { setError(""); setRetry(value => value + 1); }}>Tentar novamente</Button></div>;
  if (!data || (view === "history" && !history)) return <LoadingState label="Carregando sua jornada" />;
  const { student, currentBelt, frequency } = data;
  const limited = student.status === "INACTIVE" || student.status === "SUSPENDED";
  return <div className="space-y-5">
    <PageHeader eyebrow="Sua jornada no tatame" title={view === "dashboard" ? `Olá, ${student.displayName.split(" ")[0]}` : view === "history" ? "Seus treinos" : view === "progress" ? "Sua evolução" : "Seu perfil"} description={view === "dashboard" ? student.primaryUnit?.name ?? "Unidade não informada" : undefined} />
    {limited && <Card><p className="text-sm">Seu acesso está disponível para consulta. Para voltar a registrar presenças, converse com a academia.</p></Card>}
    {view === "dashboard" && <>
      <p className="text-sm font-semibold">{currentBelt ? `${currentBelt.belt.name} · ${currentBelt.degree} graus · ` : ""}{frequency.totalTrainings} {frequency.totalTrainings === 1 ? "treino concluído" : "treinos concluídos"}</p>
      <section aria-label="Próximo treino">
        {data.upcomingSessions.length ? <SessionTile session={data.upcomingSessions[0]} featured /> : <EmptyState title="Nenhum próximo treino disponível" description="Os treinos da sua unidade aparecerão aqui quando forem programados." />}
      </section>
      <div className="grid gap-5 md:grid-cols-2"><CurrentBelt graduation={currentBelt} /><FrequencyTiles frequency={frequency} /></div>
      {data.upcomingSessions.length > 1 && <section aria-label="Outros treinos disponíveis" className="grid gap-3 md:grid-cols-2">{data.upcomingSessions.slice(1).map(session => <SessionTile key={session.id} session={session} featured={false} />)}</section>}
      <Card><div className="mb-4 flex items-center justify-between gap-3"><h2 className="card-title">Últimos treinos</h2><ButtonLink href="/app/history" variant="ghost">Ver histórico</ButtonLink></div><TrainingList items={data.recentTrainings} /></Card>
      <Card><div className="mb-4 flex items-center justify-between gap-3"><h2 className="card-title">Sua evolução</h2><ButtonLink href="/app/progress" variant="ghost">Ver evolução</ButtonLink></div><GraduationTimeline items={data.graduationHistory} /></Card>
    </>}
    {view === "history" && history && <>
      <p className="text-sm text-mutedForeground">Somente treinos concluídos com presença válida entram no seu histórico oficial.</p>
      <Card><TrainingList items={history.content} /></Card>
      <div className="flex items-center justify-between gap-2"><Button variant="secondary" disabled={page === 0} onClick={() => { setHistory(null); setPage(value => value - 1); }}>Anterior</Button><span className="text-sm" aria-live="polite">Página {page + 1}</span><Button variant="secondary" disabled={page + 1 >= history.totalPages} onClick={() => { setHistory(null); setPage(value => value + 1); }}>Próxima</Button></div>
    </>}
    {view === "progress" && <>
      <div className="grid gap-5 md:grid-cols-2"><CurrentBelt graduation={currentBelt} /><FrequencyTiles frequency={frequency} /></div>
      {currentBelt?.criteria && <Card><h2 className="card-title">Critérios da academia</h2><div className="mt-4 space-y-2 text-sm">{currentBelt.criteria.minimumTrainingCount !== null && <p>Treinos concluídos: <strong>{currentBelt.criteria.completedTrainings} / {currentBelt.criteria.minimumTrainingCount}</strong></p>}{currentBelt.criteria.minimumDaysInGrade !== null && <p>Tempo na graduação: <strong>{currentBelt.criteria.daysInGrade ?? "Data não informada"} / {currentBelt.criteria.minimumDaysInGrade} dias</strong></p>}<p className="text-mutedForeground">Os critérios indicam quando uma avaliação pode ser considerada. A graduação é decisão do professor.</p></div></Card>}
      <Card><h2 className="mb-4 card-title">Histórico de graduação</h2><GraduationTimeline /></Card>
    </>}
    {view === "profile" && <Card><dl className="grid gap-5 sm:grid-cols-2">{[["Nome", student.fullName], ["E-mail", student.email], ["Telefone", student.phone], ["Unidade", student.primaryUnit?.name ?? "Unidade não informada"], ["Início na academia", shortDate(student.joinDate)], ["Contato de emergência", student.emergencyContact ? `${student.emergencyContact.name} · ${student.emergencyContact.relationship} · ${student.emergencyContact.phone}` : null]].map(([label, value]) => <div key={label}><dt className="caption-text">{label}</dt><dd className="mt-1 break-words text-sm font-medium">{value || "Não informado"}</dd></div>)}</dl><p className="mt-6 text-sm text-mutedForeground">Para atualizar seus dados, procure a recepção.</p><div className="mt-5"><LogoutButton onDark={false} /></div></Card>}
  </div>;
}
function SessionTile({ session, featured }: { session: Session; featured: boolean }) {
  const [showCheckInHelp, setShowCheckInHelp] = useState(false);
  const open = session.status === "OPEN";
  return <Card className={featured ? "border-primary/30 bg-primary/5" : ""}>
    <div className="flex flex-wrap items-center justify-between gap-2"><p className="caption-text">{featured ? "Próximo treino disponível" : "Na sua unidade"}</p><Badge tone={open ? "success" : "neutral"}>{open ? "Em andamento" : "Planejado"}</Badge></div>
    <p className="mt-3 text-xl font-semibold">{sessionDateLabel(session.sessionDate)} · {session.startTime.slice(0, 5)}</p>
    <h2 className="mt-1 break-words font-semibold">{session.className}</h2>
    <p className="mt-2 text-sm text-mutedForeground">{session.modality === "NO_GI" ? "No-Gi" : session.modality === "GI" ? "Gi" : session.modality} · {session.unitName}</p>
    {session.staff.length > 0 && <p className="mt-1 break-words text-sm text-mutedForeground">{session.staff.map(person => person.name).join(" · ")}</p>}
    {open && (session.attendanceRegistered ? <p className="mt-4 text-sm font-semibold text-success">Presença registrada · aguardando conclusão</p> : <Button className="mt-4 w-full" onClick={() => setShowCheckInHelp(value => !value)} aria-expanded={showCheckInHelp}>Fazer check-in</Button>)}
    {showCheckInHelp && <p className="mt-3 text-sm">Escaneie com a câmera do seu celular o QR exibido pelo professor. O código válido da aula é necessário para confirmar sua presença.</p>}
  </Card>;
}
function CurrentBelt({ graduation }: { graduation: Graduation | null }) {
  return <Card><h2 className="mb-3 card-title">Graduação atual</h2>{graduation ? <><BeltBadge belt={graduation.belt} degree={graduation.degree} showCode={false} /><p className="mt-3 text-sm text-mutedForeground">Última graduação · {shortDate(graduation.graduatedAt)}</p></> : <EmptyState title="Graduação ainda não cadastrada" description="Seu professor pode registrar sua faixa e graus." />}</Card>;
}
function FrequencyTiles({ frequency }: { frequency: Dashboard["frequency"] }) {
  return <Card><h2 className="card-title">Frequência</h2><dl className="mt-4 grid grid-cols-2 gap-4">{[["Total de treinos", frequency.totalTrainings], ["Últimos 30 dias", frequency.last30Days], ["Este mês", frequency.currentMonth], ["Últimos 90 dias", frequency.last90Days]].map(([label, value]) => <div key={label}><dd className="text-2xl font-semibold tabular-nums">{value}</dd><dt className="text-xs text-mutedForeground">{label}</dt></div>)}</dl><p className="mt-4 text-xs text-mutedForeground">{frequency.giCount} Gi · {frequency.noGiCount} No-Gi · treinos concluídos</p></Card>;
}
function TrainingList({ items }: { items: Dashboard["recentTrainings"] }) {
  if (!items.length) return <EmptyState title="Seu histórico começa no primeiro treino" description="Quando sua presença for registrada e o treino concluído, ele aparecerá aqui." />;
  return <ul className="divide-y divide-border">{items.map(item => <li key={item.attendanceId} className="py-4 first:pt-0 last:pb-0"><p className="caption-text">{shortDate(item.session.sessionDate)} · {item.session.startTime.slice(0, 5)}–{item.session.endTime.slice(0, 5)}</p><h3 className="mt-1 break-words font-semibold">{item.session.className}</h3><p className="mt-1 break-words text-sm text-mutedForeground">{item.session.modality === "NO_GI" ? "No-Gi" : item.session.modality === "GI" ? "Gi" : item.session.modality} · {item.session.unitName}</p>{item.session.staff.length > 0 && <p className="text-sm text-mutedForeground">{item.session.staff.map(person => person.name).join(" · ")}</p>}</li>)}</ul>;
}
function GraduationTimeline({ items }: { items?: Graduation[] }) {
  const [fetchedHistory, setHistory] = useState<Graduation[] | null>(null);
  const [error, setError] = useState(false);
  useEffect(() => { if (items) return; let active = true; bffJson<{ history: Graduation[] }>("/student/me/graduation").then(result => { if (active) setHistory(result.history); }).catch(() => { if (active) setError(true); }); return () => { active = false; }; }, [items]);
  const history = items ?? fetchedHistory;
  if (error) return <ErrorState message="Não foi possível carregar suas graduações." />;
  if (!history) return <LoadingState />;
  if (!history.length) return <EmptyState title="Sua evolução começa aqui" description="As graduações registradas pelo professor aparecerão nesta linha do tempo." />;
  return <ol className="space-y-4">{history.map((item, index) => <li key={index} className="border-l-2 border-primary/30 pl-4"><p className="caption-text">{shortDate(item.graduatedAt)}</p><h3 className="mt-1 font-semibold">{item.belt.name} · {item.degree} graus</h3>{item.awardedBy && <p className="text-sm text-mutedForeground">{item.awardedBy.name}</p>}</li>)}</ol>;
}

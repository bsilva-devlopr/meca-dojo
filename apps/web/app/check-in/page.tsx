"use client";

import { Badge } from "@/src/components/ui/Badge";
import { Button, ButtonLink } from "@/src/components/ui/Button";
import { Card } from "@/src/components/ui/Card";
import { ErrorState, LoadingState } from "@/src/components/ui/States";
import { type SelfCheckInResponse } from "@/src/lib/attendance";
import { bffJson } from "@/src/lib/people/client";
import { CheckCircle2, Clock, Dumbbell, RotateCcw, ShieldAlert } from "lucide-react";
import { useRouter, useSearchParams } from "next/navigation";
import { Suspense, useEffect, useState } from "react";

export default function CheckInPage() {
  return <Suspense fallback={<Shell><LoadingState label="Validando check-in" /></Shell>}><CheckInContent /></Suspense>;
}

function CheckInContent() {
  const params = useSearchParams();
  const router = useRouter();
  const token = params.get("token") ?? "";
  const [result, setResult] = useState<SelfCheckInResponse | null>(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;
    async function run() {
      if (!token) { setError("QR invalido ou ausente."); setLoading(false); return; }
      const me = await fetch("/api/auth/me");
      if (me.status === 401) {
        router.replace(`/login?returnTo=${encodeURIComponent(`/check-in?token=${token}`)}`);
        return;
      }
      try {
        const response = await bffJson<SelfCheckInResponse>("/check-ins", { method: "POST", body: JSON.stringify({ token }) });
        if (active) setResult(response);
      } catch (err) {
        if (active) setError(err instanceof Error ? err.message : "Erro ao registrar check-in.");
      } finally {
        if (active) setLoading(false);
      }
    }
    void run();
    return () => { active = false; };
  }, [router, token]);

  if (loading) return <Shell><LoadingState label="Validando check-in" /></Shell>;
  if (error) return <Shell><ErrorState message={error} /><ButtonLink href="/app" variant="secondary">Voltar ao app</ButtonLink></Shell>;
  if (!result) return <Shell><ErrorState message="Nao foi possivel validar este check-in." /></Shell>;

  const success = result.status === "ACCEPTED";
  const duplicate = result.status === "DUPLICATE";
  const expired = result.status === "EXPIRED";
  const closed = result.status === "SESSION_CLOSED";

  return (
    <Shell>
      <Card className="overflow-hidden p-0">
        <div className={`${success || duplicate ? "bg-success" : "bg-foreground"} p-6 text-white`}>
          <div className="grid h-14 w-14 place-items-center rounded-2xl bg-white/12">
            {success ? <CheckCircle2 className="h-7 w-7" aria-hidden="true" /> : expired ? <Clock className="h-7 w-7" aria-hidden="true" /> : <ShieldAlert className="h-7 w-7" aria-hidden="true" />}
          </div>
          <h1 className="mt-5 text-3xl font-semibold tracking-tight">{titleFor(result.status)}</h1>
          <p className="mt-2 text-sm leading-6 text-white/72">{result.message}</p>
        </div>
        {result.trainingSession ? (
          <div className="space-y-3 p-5">
            <Badge tone={success || duplicate ? "success" : closed ? "warning" : "danger"}>{labelFor(result.status)}</Badge>
            <div className="flex gap-3 rounded-2xl border border-border bg-surfaceElevated p-4">
              <div className="grid h-10 w-10 shrink-0 place-items-center rounded-full bg-primary/10 text-primary"><Dumbbell className="h-4 w-4" aria-hidden="true" /></div>
              <div><p className="font-semibold text-foreground">{result.trainingSession.dojoClass.name}</p><p className="mt-1 text-sm text-mutedForeground">{time(result.trainingSession.startTime)} · {result.trainingSession.unit.name}</p></div>
            </div>
            {success ? <p className="text-center text-sm font-medium text-foreground">Bom treino!</p> : null}
          </div>
        ) : null}
      </Card>
      <div className="flex flex-col gap-2 sm:flex-row"><Button type="button" variant="secondary" onClick={() => window.location.reload()}><RotateCcw className="h-4 w-4" aria-hidden="true" />Tentar novamente</Button><ButtonLink href="/app" variant="ghost">Ir para o app</ButtonLink></div>
    </Shell>
  );
}

function Shell({ children }: { children: React.ReactNode }) { return <main className="min-h-screen bg-background px-4 py-8 text-foreground"><div className="mx-auto flex min-h-[calc(100vh-4rem)] w-full max-w-md flex-col justify-center gap-4"><p className="caption-text text-center">MECA DOJO CHECK-IN</p>{children}</div></main>; }
function titleFor(status: string) { if (status === "ACCEPTED") return "Presença confirmada!"; if (status === "DUPLICATE") return "Sua presença já está registrada"; if (status === "EXPIRED") return "QR expirado"; if (status === "SESSION_CLOSED") return "Treino fechado"; if (status === "UNLINKED_USER") return "Conta sem aluno vinculado"; return "Check-in nao realizado"; }
function labelFor(status: string) { if (status === "ACCEPTED") return "Registrado"; if (status === "DUPLICATE") return "Ja registrado"; if (status === "EXPIRED") return "Expirado"; if (status === "SESSION_CLOSED") return "Fechado"; return "Atencao"; }
function time(value: string) { return value.slice(0, 5); }

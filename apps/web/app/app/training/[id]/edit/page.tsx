"use client";

import { TrainingSessionForm } from "@/app/app/training/TrainingSessionForm";
import { ErrorState, LoadingState, PageHeader } from "@/src/components/ui/States";
import { bffJson } from "@/src/lib/people/client";
import { type TrainingSession } from "@/src/lib/training/types";
import { useParams } from "next/navigation";
import { useEffect, useState } from "react";

export default function EditTrainingSessionPage() {
  const params = useParams<{ id: string }>();
  const [session, setSession] = useState<TrainingSession | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    bffJson<TrainingSession>(`/training-sessions/${params.id}`).then(setSession).catch((err: Error) => setError(err.message));
  }, [params.id]);

  return (
    <div className="space-y-6">
      <PageHeader eyebrow="Treinos" title="Editar treino" description="Altere data, horario, unidade, capacidade, professores e observacoes desta ocorrencia." />
      {error ? <ErrorState message={error} /> : null}
      {!session && !error ? <LoadingState label="Carregando treino" /> : null}
      {session ? <TrainingSessionForm initial={session} /> : null}
    </div>
  );
}

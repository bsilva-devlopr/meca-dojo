import { ButtonLink } from "@/src/components/ui/Button";
import { ErrorState, PageHeader } from "@/src/components/ui/States";
import { authenticatedApiFetch } from "@/src/lib/auth/session";
import { type DojoClass } from "@/src/lib/schedule/types";
import { ClassForm } from "../../ClassForm";

async function loadClass(id: string) {
  const response = await authenticatedApiFetch(`/api/v1/classes/${id}`);
  if (!response.ok) throw new Error("Aula nao encontrada ou sem permissao.");
  return (await response.json()) as DojoClass;
}

export default async function EditClassPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  let dojoClass: DojoClass | null = null;
  let error = "";
  try {
    dojoClass = await loadClass(id);
  } catch (err) {
    error = err instanceof Error ? err.message : "Erro ao carregar aula.";
  }

  return (
    <div className="space-y-6">
      <PageHeader eyebrow="Aulas" title="Editar aula" description="Atualize somente a definicao da aula. Horarios recorrentes continuam na Agenda." action={<ButtonLink href="/app/classes" variant="secondary">Voltar</ButtonLink>} />
      {dojoClass ? <ClassForm initial={dojoClass} /> : <ErrorState message={error} />}
    </div>
  );
}

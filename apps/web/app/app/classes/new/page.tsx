import { ButtonLink } from "@/src/components/ui/Button";
import { PageHeader } from "@/src/components/ui/States";
import { ClassForm } from "../ClassForm";

export default function NewClassPage() {
  return <div className="space-y-6"><PageHeader eyebrow="Aulas" title="Nova aula" description="Cadastre o programa da academia. Os horarios recorrentes serao organizados na Agenda." action={<ButtonLink href="/app/classes" variant="secondary">Voltar</ButtonLink>} /><ClassForm /></div>;
}

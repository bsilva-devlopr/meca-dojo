import { PageHeader } from "@/src/components/ui/States";
import { StudentForm } from "../StudentForm";

export default function NewStudentPage() {
  return (
    <div className="space-y-6">
      <PageHeader eyebrow="Alunos" title="Novo aluno" description="Cadastre a ficha administrativa do aluno antes de liberar acesso ao sistema." />
      <StudentForm />
    </div>
  );
}

import { PageHeader } from "@/src/components/ui/States";
import { TrainingSessionForm } from "@/app/app/training/TrainingSessionForm";

export default function NewTrainingSessionPage() {
  return (
    <div className="space-y-6">
      <PageHeader eyebrow="Treinos" title="Novo treino" description="Crie uma ocorrencia manual para aulao, treino extra ou evento especial." />
      <TrainingSessionForm />
    </div>
  );
}

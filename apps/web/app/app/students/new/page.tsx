import { StudentForm } from "../StudentForm";

export default function NewStudentPage() {
  return (
    <div className="space-y-5">
      <h1 className="text-2xl font-semibold text-stone-950">Novo aluno</h1>
      <StudentForm />
    </div>
  );
}

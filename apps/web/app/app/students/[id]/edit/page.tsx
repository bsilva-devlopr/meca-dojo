"use client";

import { LoadingState, PageHeader, ErrorState } from "@/src/components/ui/States";
import { bffJson } from "@/src/lib/people/client";
import { type StudentResponse } from "@/src/lib/people/types";
import { useParams } from "next/navigation";
import { useEffect, useState } from "react";
import { StudentForm } from "../../StudentForm";

export default function EditStudentPage() {
  const params = useParams<{ id: string }>();
  const [student, setStudent] = useState<StudentResponse | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    bffJson<StudentResponse>(`/students/${params.id}`).then(setStudent).catch((err: Error) => setError(err.message));
  }, [params.id]);

  if (!student) {
    return error ? <ErrorState message={error} /> : <LoadingState label="Carregando aluno" />;
  }

  return (
    <div className="space-y-6">
      <PageHeader eyebrow="Alunos" title="Editar aluno" description={`Atualize os dados administrativos de ${student.fullName}.`} />
      <StudentForm student={student} />
    </div>
  );
}

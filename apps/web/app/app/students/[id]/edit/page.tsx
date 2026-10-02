"use client";

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
    return <p className="text-sm text-stone-600">{error || "Carregando aluno..."}</p>;
  }

  return (
    <div className="space-y-5">
      <h1 className="text-2xl font-semibold text-stone-950">Editar aluno</h1>
      <StudentForm student={student} />
    </div>
  );
}

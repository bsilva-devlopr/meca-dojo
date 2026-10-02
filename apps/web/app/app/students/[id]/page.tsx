"use client";

import { bffJson, hasPermission } from "@/src/lib/people/client";
import { type GuardianRelationship, type StudentResponse } from "@/src/lib/people/types";
import Link from "next/link";
import { useParams } from "next/navigation";
import { type FormEvent, type ReactNode, useCallback, useEffect, useState } from "react";

type Me = { permissions: string[] };

export default function StudentProfilePage() {
  const params = useParams<{ id: string }>();
  const [student, setStudent] = useState<StudentResponse | null>(null);
  const [me, setMe] = useState<Me | null>(null);
  const [error, setError] = useState("");

  const load = useCallback(() => {
    bffJson<StudentResponse>(`/students/${params.id}`).then(setStudent).catch((err: Error) => setError(err.message));
  }, [params.id]);

  useEffect(() => {
    fetch("/api/auth/me").then((r) => r.json()).then(setMe).catch(() => undefined);
    load();
  }, [load]);

  async function addGuardian(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    try {
      await bffJson(`/students/${params.id}/guardians/new`, {
        method: "POST",
        body: JSON.stringify({
          fullName: String(form.get("fullName") ?? "").trim(),
          email: blank(form.get("email")),
          phone: String(form.get("phone") ?? "").trim(),
          relationship: String(form.get("relationship") ?? "OTHER") as GuardianRelationship,
          primary: form.get("primary") === "on",
        }),
      });
      event.currentTarget.reset();
      load();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Erro ao adicionar responsavel.");
    }
  }

  if (!student) {
    return <p className="text-sm text-stone-600">{error || "Carregando aluno..."}</p>;
  }

  const canUpdate = me ? hasPermission(me.permissions, "STUDENT_UPDATE") : false;
  const canCreateGuardian = me ? hasPermission(me.permissions, "GUARDIAN_CREATE") : false;

  return (
    <div className="space-y-5">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-stone-950">{student.fullName}</h1>
          <p className="mt-1 text-sm text-stone-600">{student.status} · Aluno desde {student.joinDate}</p>
        </div>
        {canUpdate ? <Link href={`/app/students/${student.id}/edit`} className="rounded border border-stone-300 px-4 py-2 text-sm font-semibold">Editar</Link> : null}
      </div>

      {error ? <p className="text-sm text-red-700">{error}</p> : null}

      <section className="grid gap-4 sm:grid-cols-2">
        <Panel title="Dados pessoais">
          <p>Nome preferido: {student.preferredName || "-"}</p>
          <p>Nascimento: {student.birthDate || "-"}</p>
        </Panel>
        <Panel title="Contato">
          <p>E-mail: {student.email || "-"}</p>
          <p>Telefone: {student.phone || "-"}</p>
        </Panel>
        <Panel title="Unidade">
          <p>{student.primaryUnit.name}</p>
        </Panel>
        <Panel title="Acesso ao sistema">
          <p>{student.user ? `${student.user.name} · ${student.user.email}` : "Sem usuario vinculado"}</p>
        </Panel>
      </section>

      <Panel title="Responsaveis">
        <div className="space-y-2">
          {student.guardians.map((guardian) => (
            <div key={guardian.id} className="rounded border border-stone-200 p-3 text-sm">
              <p className="font-medium">{guardian.fullName}{guardian.primary ? " · principal" : ""}</p>
              <p>{guardian.relationship} · {guardian.phone}</p>
            </div>
          ))}
          {student.guardians.length === 0 ? <p className="text-sm text-stone-600">Nenhum responsavel vinculado.</p> : null}
        </div>
        {canCreateGuardian ? (
          <form onSubmit={addGuardian} className="mt-4 grid gap-3 border-t border-stone-200 pt-4 sm:grid-cols-2">
            <input name="fullName" required placeholder="Nome do responsavel" className="h-11 rounded border border-stone-300 px-3" />
            <input name="phone" required placeholder="Telefone" className="h-11 rounded border border-stone-300 px-3" />
            <input name="email" type="email" placeholder="E-mail" className="h-11 rounded border border-stone-300 px-3" />
            <select name="relationship" className="h-11 rounded border border-stone-300 px-3">
              {(["FATHER", "MOTHER", "LEGAL_GUARDIAN", "GRANDPARENT", "OTHER"] satisfies GuardianRelationship[]).map((value) => <option key={value}>{value}</option>)}
            </select>
            <label className="flex items-center gap-2 text-sm"><input name="primary" type="checkbox" /> Responsavel principal</label>
            <button className="rounded bg-stone-950 px-4 py-2 text-sm font-semibold text-white">Adicionar responsavel</button>
          </form>
        ) : null}
      </Panel>

      <Panel title="Contato de emergencia">
        {student.emergencyContact ? (
          <p>{student.emergencyContact.name} · {student.emergencyContact.relationship} · {student.emergencyContact.phone}</p>
        ) : (
          <p className="text-sm text-stone-600">Nao informado.</p>
        )}
      </Panel>

      <Panel title="Observacoes">
        <p className="whitespace-pre-wrap">{student.notes || "-"}</p>
      </Panel>
    </div>
  );
}

function Panel({ title, children }: { title: string; children: ReactNode }) {
  return (
    <section className="rounded border border-stone-200 bg-white p-4 shadow-sm">
      <h2 className="mb-3 text-sm font-semibold uppercase tracking-wide text-stone-700">{title}</h2>
      <div className="space-y-1 text-sm text-stone-800">{children}</div>
    </section>
  );
}

function blank(value: FormDataEntryValue | null) {
  const text = String(value ?? "").trim();
  return text ? text : null;
}

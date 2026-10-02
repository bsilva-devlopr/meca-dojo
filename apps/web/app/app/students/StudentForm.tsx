"use client";

import { bffJson } from "@/src/lib/people/client";
import { type StudentResponse, type StudentStatus, type UnitSummary } from "@/src/lib/people/types";
import { useRouter } from "next/navigation";
import { FormEvent, useEffect, useState } from "react";

type Props = {
  student?: StudentResponse;
};

export function StudentForm({ student }: Props) {
  const router = useRouter();
  const [units, setUnits] = useState<UnitSummary[]>([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    bffJson<UnitSummary[]>("/units").then(setUnits).catch(() => setError("Nao foi possivel carregar unidades."));
  }, []);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setLoading(true);
    setError("");
    const form = new FormData(event.currentTarget);
    const payload = {
      fullName: String(form.get("fullName") ?? "").trim(),
      preferredName: blank(form.get("preferredName")),
      birthDate: blank(form.get("birthDate")),
      email: blank(form.get("email")),
      phone: blank(form.get("phone")),
      primaryUnitId: String(form.get("primaryUnitId") ?? ""),
      joinDate: String(form.get("joinDate") ?? ""),
      status: String(form.get("status") ?? "ACTIVE") as StudentStatus,
      notes: blank(form.get("notes")),
    };

    try {
      const saved = await bffJson<StudentResponse>(student ? `/students/${student.id}` : "/students", {
        method: student ? "PATCH" : "POST",
        body: JSON.stringify(payload),
      });

      const ecName = String(form.get("emergencyName") ?? "").trim();
      const ecRelationship = String(form.get("emergencyRelationship") ?? "").trim();
      const ecPhone = String(form.get("emergencyPhone") ?? "").trim();
      if (ecName || ecRelationship || ecPhone) {
        await bffJson(`/students/${saved.id}/emergency-contact`, {
          method: "PUT",
          body: JSON.stringify({ name: ecName, relationship: ecRelationship, phone: ecPhone }),
        });
      }
      router.replace(`/app/students/${saved.id}`);
      router.refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Erro ao salvar aluno.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <form onSubmit={submit} className="space-y-5 rounded border border-stone-200 bg-white p-5 shadow-sm">
      {error ? <p className="text-sm text-red-700">{error}</p> : null}
      <div className="grid gap-4 sm:grid-cols-2">
        <Field name="fullName" label="Nome completo *" defaultValue={student?.fullName} required />
        <Field name="preferredName" label="Nome preferido" defaultValue={student?.preferredName ?? ""} />
        <Field name="birthDate" label="Data de nascimento" type="date" defaultValue={student?.birthDate ?? ""} />
        <Field name="email" label="E-mail" type="email" defaultValue={student?.email ?? ""} />
        <Field name="phone" label="Telefone" defaultValue={student?.phone ?? ""} />
        <label className="flex flex-col gap-2 text-sm font-medium text-stone-800">Unidade principal *
          <select name="primaryUnitId" required defaultValue={student?.primaryUnit.id ?? ""} className="h-11 rounded border border-stone-300 px-3">
            <option value="">Selecione</option>
            {units.map((unit) => <option key={unit.id} value={unit.id}>{unit.name}</option>)}
          </select>
        </label>
        <Field name="joinDate" label="Data de inicio *" type="date" defaultValue={student?.joinDate ?? new Date().toISOString().slice(0, 10)} required />
        <label className="flex flex-col gap-2 text-sm font-medium text-stone-800">Status *
          <select name="status" defaultValue={student?.status ?? "ACTIVE"} className="h-11 rounded border border-stone-300 px-3">
            {(["ACTIVE", "TRIAL", "INACTIVE", "SUSPENDED"] satisfies StudentStatus[]).map((value) => <option key={value}>{value}</option>)}
          </select>
        </label>
      </div>

      <section className="grid gap-4 border-t border-stone-200 pt-4 sm:grid-cols-3">
        <Field name="emergencyName" label="Contato de emergencia" defaultValue={student?.emergencyContact?.name ?? ""} />
        <Field name="emergencyRelationship" label="Relacao" defaultValue={student?.emergencyContact?.relationship ?? ""} />
        <Field name="emergencyPhone" label="Telefone emergencia" defaultValue={student?.emergencyContact?.phone ?? ""} />
      </section>

      <label className="flex flex-col gap-2 text-sm font-medium text-stone-800">Observacoes administrativas
        <textarea name="notes" defaultValue={student?.notes ?? ""} className="min-h-28 rounded border border-stone-300 p-3" />
      </label>

      <button disabled={loading} className="rounded bg-stone-950 px-4 py-2 text-sm font-semibold text-white disabled:bg-stone-500">{loading ? "Salvando..." : "Salvar"}</button>
    </form>
  );
}

function Field(props: { name: string; label: string; type?: string; defaultValue?: string; required?: boolean }) {
  return (
    <label className="flex flex-col gap-2 text-sm font-medium text-stone-800">{props.label}
      <input {...props} className="h-11 rounded border border-stone-300 px-3" />
    </label>
  );
}

function blank(value: FormDataEntryValue | null) {
  const text = String(value ?? "").trim();
  return text ? text : null;
}

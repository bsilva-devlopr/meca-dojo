"use client";

import { Button } from "@/src/components/ui/Button";
import { Card, CardHeader } from "@/src/components/ui/Card";
import { FormField, Input, Select, Textarea } from "@/src/components/ui/Form";
import { ErrorState } from "@/src/components/ui/States";
import { bffJson } from "@/src/lib/people/client";
import { type StudentResponse, type StudentStatus, type UnitSummary } from "@/src/lib/people/types";
import { Save } from "lucide-react";
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
    <form onSubmit={submit} className="space-y-5">
      {error ? <ErrorState message={error} /> : null}

      <Card>
        <CardHeader title="Dados do aluno" eyebrow="Cadastro" >
          Mantenha apenas dados operacionais. Informacoes medicas terao fluxo proprio.
        </CardHeader>
        <div className="grid gap-4 sm:grid-cols-2">
          <FormField label="Nome completo" required><Input name="fullName" defaultValue={student?.fullName} required /></FormField>
          <FormField label="Nome preferido"><Input name="preferredName" defaultValue={student?.preferredName ?? ""} /></FormField>
          <FormField label="Data de nascimento"><Input name="birthDate" type="date" defaultValue={student?.birthDate ?? ""} /></FormField>
          <FormField label="E-mail"><Input name="email" type="email" defaultValue={student?.email ?? ""} /></FormField>
          <FormField label="Telefone"><Input name="phone" defaultValue={student?.phone ?? ""} /></FormField>
          <FormField label="Unidade principal" required>
            <Select name="primaryUnitId" required defaultValue={student?.primaryUnit.id ?? ""}>
              <option value="">Selecione</option>
              {units.map((unit) => <option key={unit.id} value={unit.id}>{unit.name}</option>)}
            </Select>
          </FormField>
          <FormField label="Data de inicio" required><Input name="joinDate" type="date" defaultValue={student?.joinDate ?? new Date().toISOString().slice(0, 10)} required /></FormField>
          <FormField label="Status" required>
            <Select name="status" defaultValue={student?.status ?? "ACTIVE"}>
              {(["ACTIVE", "TRIAL", "INACTIVE", "SUSPENDED"] satisfies StudentStatus[]).map((value) => <option key={value}>{value}</option>)}
            </Select>
          </FormField>
        </div>
      </Card>

      <Card>
        <CardHeader title="Contato de emergencia" eyebrow="Seguranca operacional" />
        <div className="grid gap-4 sm:grid-cols-3">
          <FormField label="Nome"><Input name="emergencyName" defaultValue={student?.emergencyContact?.name ?? ""} /></FormField>
          <FormField label="Relacao"><Input name="emergencyRelationship" defaultValue={student?.emergencyContact?.relationship ?? ""} /></FormField>
          <FormField label="Telefone"><Input name="emergencyPhone" defaultValue={student?.emergencyContact?.phone ?? ""} /></FormField>
        </div>
      </Card>

      <Card>
        <CardHeader title="Observacoes administrativas" eyebrow="Uso interno" />
        <FormField label="Observacoes" hint="Nao use este campo como prontuario medico.">
          <Textarea name="notes" defaultValue={student?.notes ?? ""} />
        </FormField>
      </Card>

      <div className="flex justify-end">
        <Button disabled={loading}>
          <Save className="h-4 w-4" aria-hidden="true" />
          {loading ? "Salvando..." : "Salvar aluno"}
        </Button>
      </div>
    </form>
  );
}

function blank(value: FormDataEntryValue | null) {
  const text = String(value ?? "").trim();
  return text ? text : null;
}

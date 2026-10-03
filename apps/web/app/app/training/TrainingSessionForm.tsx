"use client";

import { Button } from "@/src/components/ui/Button";
import { Card } from "@/src/components/ui/Card";
import { FormField, Input, Select, Textarea } from "@/src/components/ui/Form";
import { ErrorState, LoadingState } from "@/src/components/ui/States";
import { bffJson } from "@/src/lib/people/client";
import { type PageResponse, type StaffResponse, type UnitSummary } from "@/src/lib/people/types";
import { type DojoClass } from "@/src/lib/schedule/types";
import { type TrainingSession } from "@/src/lib/training/types";
import { useRouter } from "next/navigation";
import { type FormEvent, useEffect, useState } from "react";

export function TrainingSessionForm({ initial }: { initial?: TrainingSession }) {
  const router = useRouter();
  const [classes, setClasses] = useState<DojoClass[]>([]);
  const [units, setUnits] = useState<UnitSummary[]>([]);
  const [staff, setStaff] = useState<StaffResponse[]>([]);
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      bffJson<PageResponse<DojoClass>>("/classes?status=ACTIVE&page=0&size=100"),
      bffJson<UnitSummary[]>("/units"),
      bffJson<PageResponse<StaffResponse>>("/staff?status=ACTIVE&page=0&size=100"),
    ]).then(([classPage, unitList, staffPage]) => {
      setClasses(classPage.content);
      setUnits(unitList);
      setStaff(staffPage.content);
    }).catch((err: Error) => setError(err.message)).finally(() => setLoading(false));
  }, []);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
    setSaving(true);
    const form = new FormData(event.currentTarget);
    const capacity = String(form.get("capacity") ?? "").trim();
    const lead = String(form.get("leadStaffId") ?? "").trim();
    const assistant = String(form.get("assistantStaffId") ?? "").trim();
    const staffAssignments = [
      ...(lead ? [{ staffId: lead, role: "LEAD" }] : []),
      ...(assistant && assistant !== lead ? [{ staffId: assistant, role: "ASSISTANT" }] : []),
    ];
    const body = {
      ...(initial ? {} : { dojoClassId: String(form.get("dojoClassId") ?? "") }),
      sessionDate: String(form.get("sessionDate") ?? ""),
      startTime: String(form.get("startTime") ?? ""),
      endTime: String(form.get("endTime") ?? ""),
      unitId: String(form.get("unitId") ?? ""),
      ...(capacity ? { capacity: Number(capacity) } : {}),
      notes: String(form.get("notes") ?? "").trim(),
      staff: staffAssignments,
    };

    try {
      const path = initial ? `/training-sessions/${initial.id}` : "/training-sessions";
      const method = initial ? "PATCH" : "POST";
      const saved = await bffJson<TrainingSession>(path, { method, body: JSON.stringify(body) });
      router.push(`/app/training/${saved.id}`);
      router.refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Erro ao salvar treino.");
    } finally {
      setSaving(false);
    }
  }

  if (loading) return <LoadingState label="Carregando formulario" />;

  return (
    <Card>
      <form onSubmit={submit} className="grid gap-5">
        {error ? <ErrorState message={error} /> : null}
        <div className="grid gap-4 md:grid-cols-2">
          <FormField label="Aula" required>
            <Select name="dojoClassId" required disabled={Boolean(initial)} defaultValue={initial?.dojoClass.id ?? ""}>
              <option value="">Selecione</option>
              {classes.map((dojoClass) => <option key={dojoClass.id} value={dojoClass.id}>{dojoClass.name}</option>)}
              {initial && !classes.some((item) => item.id === initial.dojoClass.id) ? <option value={initial.dojoClass.id}>{initial.dojoClass.name}</option> : null}
            </Select>
          </FormField>
          <FormField label="Unidade" required>
            <Select name="unitId" required defaultValue={initial?.unit.id ?? ""}>
              <option value="">Selecione</option>
              {units.map((unit) => <option key={unit.id} value={unit.id}>{unit.name}</option>)}
            </Select>
          </FormField>
        </div>
        <div className="grid gap-4 md:grid-cols-4">
          <FormField label="Data" required><Input name="sessionDate" type="date" required defaultValue={initial?.sessionDate ?? todayLocalDate()} /></FormField>
          <FormField label="Inicio" required><Input name="startTime" type="time" required defaultValue={time(initial?.startTime) ?? "20:30"} /></FormField>
          <FormField label="Fim" required><Input name="endTime" type="time" required defaultValue={time(initial?.endTime) ?? "22:00"} /></FormField>
          <FormField label="Capacidade"><Input name="capacity" type="number" min={1} defaultValue={initial?.capacity ?? ""} /></FormField>
        </div>
        <div className="grid gap-4 md:grid-cols-2">
          <FormField label="Professor principal">
            <Select name="leadStaffId" defaultValue={initial?.staff.find((item) => item.role === "LEAD")?.id ?? ""}>
              <option value="">A definir</option>
              {staff.map((person) => <option key={person.id} value={person.id}>{person.displayName} - {person.type}</option>)}
            </Select>
          </FormField>
          <FormField label="Assistente">
            <Select name="assistantStaffId" defaultValue={initial?.staff.find((item) => item.role === "ASSISTANT")?.id ?? ""}>
              <option value="">Sem assistente</option>
              {staff.map((person) => <option key={person.id} value={person.id}>{person.displayName} - {person.type}</option>)}
            </Select>
          </FormField>
        </div>
        <FormField label="Observacoes"><Textarea name="notes" defaultValue={initial?.notes ?? ""} placeholder="Treino pre-campeonato, professor substituto ou orientacao operacional." /></FormField>
        <Button disabled={saving} className="justify-self-start">{saving ? "Salvando..." : initial ? "Salvar treino" : "Criar treino"}</Button>
      </form>
    </Card>
  );
}

function todayLocalDate() {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}-${String(now.getDate()).padStart(2, "0")}`;
}

function time(value?: string) {
  return value?.slice(0, 5);
}

"use client";

import { Button } from "@/src/components/ui/Button";
import { Card } from "@/src/components/ui/Card";
import { FormField, Input, Select, Textarea } from "@/src/components/ui/Form";
import { ErrorState } from "@/src/components/ui/States";
import { bffJson } from "@/src/lib/people/client";
import { audienceLabels, classTypeLabels, levelLabels, modalityLabels, type DojoClass } from "@/src/lib/schedule/types";
import { useRouter } from "next/navigation";
import { type FormEvent, useState } from "react";

export function ClassForm({ initial }: { initial?: DojoClass }) {
  const router = useRouter();
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
    setSaving(true);
    const form = new FormData(event.currentTarget);
    const capacity = String(form.get("defaultCapacity") ?? "").trim();
    const body = {
      name: String(form.get("name") ?? "").trim(),
      description: String(form.get("description") ?? "").trim(),
      modality: String(form.get("modality")),
      audience: String(form.get("audience")),
      classType: String(form.get("classType")),
      level: String(form.get("level")),
      status: String(form.get("status")),
      ...(capacity ? { defaultCapacity: Number(capacity) } : {}),
    };
    try {
      const path = initial ? `/classes/${initial.id}` : "/classes";
      const method = initial ? "PATCH" : "POST";
      await bffJson(path, { method, body: JSON.stringify(body) });
      router.push("/app/classes");
      router.refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Erro ao salvar aula.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <Card>
      <form onSubmit={submit} className="grid gap-5">
        {error ? <ErrorState message={error} /> : null}
        <div className="grid gap-4 md:grid-cols-2">
          <FormField label="Nome" required><Input name="name" required defaultValue={initial?.name ?? ""} placeholder="Jiu-Jitsu Adulto" /></FormField>
          <FormField label="Status"><Select name="status" defaultValue={initial?.status ?? "ACTIVE"}><option value="ACTIVE">Ativa</option><option value="INACTIVE">Inativa</option></Select></FormField>
        </div>
        <FormField label="Descricao"><Textarea name="description" defaultValue={initial?.description ?? ""} placeholder="Treino tecnico, rolas e fundamentos da aula." /></FormField>
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          <FormField label="Modalidade" required><Select name="modality" defaultValue={initial?.modality ?? "GI"}>{Object.entries(modalityLabels).map(([value, label]) => <option key={value} value={value}>{label}</option>)}</Select></FormField>
          <FormField label="Publico" required><Select name="audience" defaultValue={initial?.audience ?? "ADULT"}>{Object.entries(audienceLabels).map(([value, label]) => <option key={value} value={value}>{label}</option>)}</Select></FormField>
          <FormField label="Tipo" required><Select name="classType" defaultValue={initial?.classType ?? "REGULAR"}>{Object.entries(classTypeLabels).map(([value, label]) => <option key={value} value={value}>{label}</option>)}</Select></FormField>
          <FormField label="Nivel" required><Select name="level" defaultValue={initial?.level ?? "ALL_LEVELS"}>{Object.entries(levelLabels).map(([value, label]) => <option key={value} value={value}>{label}</option>)}</Select></FormField>
        </div>
        <FormField label="Capacidade padrao" hint="Deixe vazio para aula sem limite configurado."><Input name="defaultCapacity" type="number" min={1} defaultValue={initial?.defaultCapacity ?? ""} placeholder="30" /></FormField>
        <Button disabled={saving} className="justify-self-start">{saving ? "Salvando..." : initial ? "Salvar aula" : "Criar aula"}</Button>
      </form>
    </Card>
  );
}

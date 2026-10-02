"use client";

import { Badge, StatusBadge } from "@/src/components/ui/Badge";
import { Button } from "@/src/components/ui/Button";
import { Card } from "@/src/components/ui/Card";
import { Input, Select } from "@/src/components/ui/Form";
import { EmptyState, ErrorState, PageHeader } from "@/src/components/ui/States";
import { bffJson, hasPermission } from "@/src/lib/people/client";
import { type PageResponse, type StaffResponse, type StaffStatus, type StaffType, type UnitSummary, type UserSummary } from "@/src/lib/people/types";
import { Search, UserPlus } from "lucide-react";
import { type FormEvent, useCallback, useEffect, useState } from "react";

type Me = { permissions: string[] };

export default function StaffPage() {
  const [me, setMe] = useState<Me | null>(null);
  const [staff, setStaff] = useState<PageResponse<StaffResponse> | null>(null);
  const [users, setUsers] = useState<UserSummary[]>([]);
  const [units, setUnits] = useState<UnitSummary[]>([]);
  const [type, setType] = useState("");
  const [status, setStatus] = useState("");
  const [search, setSearch] = useState("");
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const load = useCallback(() => {
    const params = new URLSearchParams({ page: "0", size: "20" });
    if (type) params.set("type", type);
    if (status) params.set("status", status);
    if (search) params.set("search", search);
    bffJson<PageResponse<StaffResponse>>(`/staff?${params.toString()}`).then(setStaff).catch((err: Error) => setError(err.message));
  }, [search, status, type]);

  useEffect(() => {
    fetch("/api/auth/me").then((r) => r.json()).then(setMe).catch(() => undefined);
    bffJson<UserSummary[]>("/users").then(setUsers).catch(() => undefined);
    bffJson<UnitSummary[]>("/units").then(setUnits).catch(() => undefined);
  }, []);

  useEffect(() => {
    void Promise.resolve().then(load);
  }, [load]);

  async function create(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
    setMessage("");
    const form = new FormData(event.currentTarget);
    try {
      await bffJson("/staff", {
        method: "POST",
        body: JSON.stringify({
          userId: String(form.get("userId") ?? ""),
          type: String(form.get("type") ?? "PROFESSOR") as StaffType,
          displayName: String(form.get("displayName") ?? "").trim(),
          primaryUnitId: blank(form.get("primaryUnitId")),
        }),
      });
      event.currentTarget.reset();
      setMessage("Membro da equipe criado.");
      load();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Erro ao criar membro da equipe.");
    }
  }

  const canCreate = me ? hasPermission(me.permissions, "STAFF_CREATE") : false;

  return (
    <div className="space-y-6">
      <PageHeader eyebrow="Operacao" title="Equipe" description="Professores e instrutores como perfis de negocio, separados das roles de autorizacao." />

      <Card className="p-4">
        <div className="grid gap-3 md:grid-cols-[1fr_180px_180px]">
          <label className="relative"><span className="sr-only">Buscar equipe</span><Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-mutedForeground" aria-hidden="true" /><Input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Buscar equipe..." className="pl-9" /></label>
          <Select value={type} onChange={(e) => setType(e.target.value)} aria-label="Tipo"><option value="">Tipo</option>{(["PROFESSOR", "INSTRUCTOR"] satisfies StaffType[]).map((value) => <option key={value}>{value}</option>)}</Select>
          <Select value={status} onChange={(e) => setStatus(e.target.value)} aria-label="Status"><option value="">Status</option>{(["ACTIVE", "INACTIVE"] satisfies StaffStatus[]).map((value) => <option key={value}>{value}</option>)}</Select>
        </div>
      </Card>

      {error ? <ErrorState message={error} /> : null}
      {message ? <div className="rounded-lg border border-success/20 bg-success/10 p-3 text-sm font-medium text-success">{message}</div> : null}

      {canCreate ? (
        <Card>
          <form onSubmit={create} className="grid gap-3 lg:grid-cols-4">
            <Select name="userId" required><option value="">Usuario existente</option>{users.map((user) => <option key={user.id} value={user.id}>{user.name} · {user.email}</option>)}</Select>
            <Select name="type"><option>PROFESSOR</option><option>INSTRUCTOR</option></Select>
            <Input name="displayName" required placeholder="Nome de exibicao" />
            <Select name="primaryUnitId"><option value="">Unidade principal</option>{units.map((unit) => <option key={unit.id} value={unit.id}>{unit.name}</option>)}</Select>
            <Button className="lg:col-span-4 lg:justify-self-start"><UserPlus className="h-4 w-4" aria-hidden="true" />Criar StaffProfile</Button>
          </form>
        </Card>
      ) : null}

      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        {staff?.content.map((person) => (
          <Card key={person.id}>
            <div className="flex items-start justify-between gap-3">
              <div>
                <p className="font-semibold text-foreground">{person.displayName}</p>
                <p className="mt-1 text-sm text-mutedForeground">{person.user.email}</p>
              </div>
              <StatusBadge status={person.status} />
            </div>
            <div className="mt-4 flex flex-wrap gap-2"><Badge tone="primary">{person.type}</Badge><Badge>{person.primaryUnit?.name ?? "Sem unidade"}</Badge></div>
          </Card>
        ))}
      </div>
      {staff?.content.length === 0 ? <EmptyState title="Nenhum membro encontrado" description="Ajuste os filtros ou crie um perfil de professor/instrutor usando um usuario existente." /> : null}
    </div>
  );
}

function blank(value: FormDataEntryValue | null) {
  const text = String(value ?? "").trim();
  return text ? text : null;
}

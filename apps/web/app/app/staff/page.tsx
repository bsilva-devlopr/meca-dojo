"use client";

import { bffJson, hasPermission } from "@/src/lib/people/client";
import { type PageResponse, type StaffResponse, type StaffStatus, type StaffType, type UnitSummary, type UserSummary } from "@/src/lib/people/types";
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
    load();
  }, [load]);

  async function create(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
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
      load();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Erro ao criar membro da equipe.");
    }
  }

  const canCreate = me ? hasPermission(me.permissions, "STAFF_CREATE") : false;

  return (
    <div className="space-y-5">
      <h1 className="text-2xl font-semibold text-stone-950">Equipe</h1>

      <div className="grid gap-3 rounded border border-stone-200 bg-white p-4 shadow-sm sm:grid-cols-3">
        <input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Buscar equipe..." className="h-11 rounded border border-stone-300 px-3" />
        <select value={type} onChange={(e) => setType(e.target.value)} className="h-11 rounded border border-stone-300 px-3">
          <option value="">Tipo</option>
          {(["PROFESSOR", "INSTRUCTOR"] satisfies StaffType[]).map((value) => <option key={value}>{value}</option>)}
        </select>
        <select value={status} onChange={(e) => setStatus(e.target.value)} className="h-11 rounded border border-stone-300 px-3">
          <option value="">Status</option>
          {(["ACTIVE", "INACTIVE"] satisfies StaffStatus[]).map((value) => <option key={value}>{value}</option>)}
        </select>
      </div>

      {error ? <p className="text-sm text-red-700">{error}</p> : null}

      {canCreate ? (
        <form onSubmit={create} className="grid gap-3 rounded border border-stone-200 bg-white p-4 shadow-sm sm:grid-cols-4">
          <select name="userId" required className="h-11 rounded border border-stone-300 px-3">
            <option value="">Usuario existente</option>
            {users.map((user) => <option key={user.id} value={user.id}>{user.name} · {user.email}</option>)}
          </select>
          <select name="type" className="h-11 rounded border border-stone-300 px-3">
            <option>PROFESSOR</option>
            <option>INSTRUCTOR</option>
          </select>
          <input name="displayName" required placeholder="Nome de exibicao" className="h-11 rounded border border-stone-300 px-3" />
          <select name="primaryUnitId" className="h-11 rounded border border-stone-300 px-3">
            <option value="">Unidade principal</option>
            {units.map((unit) => <option key={unit.id} value={unit.id}>{unit.name}</option>)}
          </select>
          <button className="rounded bg-stone-950 px-4 py-2 text-sm font-semibold text-white sm:col-span-4">Criar StaffProfile</button>
        </form>
      ) : null}

      <div className="grid gap-3 sm:grid-cols-2">
        {staff?.content.map((person) => (
          <div key={person.id} className="rounded border border-stone-200 bg-white p-4 shadow-sm">
            <p className="font-semibold text-stone-950">{person.displayName}</p>
            <p className="text-sm text-stone-700">{person.type} · {person.status}</p>
            <p className="mt-2 text-sm text-stone-600">{person.user.email}</p>
            <p className="text-sm text-stone-600">{person.primaryUnit?.name ?? "Sem unidade principal"}</p>
          </div>
        ))}
      </div>
    </div>
  );
}

function blank(value: FormDataEntryValue | null) {
  const text = String(value ?? "").trim();
  return text ? text : null;
}

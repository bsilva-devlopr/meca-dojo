"use client";

import { bffJson, hasPermission } from "@/src/lib/people/client";
import { type PageResponse, type StudentListItem, type StudentStatus, type UnitSummary } from "@/src/lib/people/types";
import Link from "next/link";
import { useEffect, useState } from "react";

type Me = { permissions: string[] };

export default function StudentsPage() {
  const [me, setMe] = useState<Me | null>(null);
  const [students, setStudents] = useState<PageResponse<StudentListItem> | null>(null);
  const [units, setUnits] = useState<UnitSummary[]>([]);
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState("");
  const [unitId, setUnitId] = useState("");
  const [page, setPage] = useState(0);
  const [error, setError] = useState("");

  useEffect(() => {
    fetch("/api/auth/me").then((r) => r.json()).then(setMe).catch(() => setError("Sessao invalida."));
    bffJson<UnitSummary[]>("/units").then(setUnits).catch(() => undefined);
  }, []);

  useEffect(() => {
    const params = new URLSearchParams({ page: String(page), size: "10" });
    if (search) params.set("search", search);
    if (status) params.set("status", status);
    if (unitId) params.set("unitId", unitId);
    bffJson<PageResponse<StudentListItem>>(`/students?${params.toString()}`)
      .then(setStudents)
      .catch((err: Error) => setError(err.message));
  }, [search, status, unitId, page]);

  const canCreate = me ? hasPermission(me.permissions, "STUDENT_CREATE") : false;

  return (
    <div className="space-y-5">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <h1 className="text-2xl font-semibold text-stone-950">Alunos</h1>
        {canCreate ? (
          <Link href="/app/students/new" className="rounded bg-stone-950 px-4 py-2 text-sm font-semibold text-white">
            + Novo aluno
          </Link>
        ) : null}
      </div>

      <div className="grid gap-3 rounded border border-stone-200 bg-white p-4 shadow-sm sm:grid-cols-3">
        <input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Buscar aluno..." className="h-11 rounded border border-stone-300 px-3" />
        <select value={status} onChange={(e) => setStatus(e.target.value)} className="h-11 rounded border border-stone-300 px-3">
          <option value="">Status</option>
          {(["ACTIVE", "TRIAL", "INACTIVE", "SUSPENDED"] satisfies StudentStatus[]).map((value) => <option key={value}>{value}</option>)}
        </select>
        <select value={unitId} onChange={(e) => setUnitId(e.target.value)} className="h-11 rounded border border-stone-300 px-3">
          <option value="">Unidade</option>
          {units.map((unit) => <option key={unit.id} value={unit.id}>{unit.name}</option>)}
        </select>
      </div>

      {error ? <p className="text-sm text-red-700">{error}</p> : null}

      <div className="overflow-hidden rounded border border-stone-200 bg-white shadow-sm">
        <div className="hidden grid-cols-4 border-b border-stone-200 px-4 py-3 text-sm font-semibold text-stone-700 sm:grid">
          <span>Nome</span><span>Unidade</span><span>Status</span><span>Desde</span>
        </div>
        {students?.content.map((student) => (
          <Link key={student.id} href={`/app/students/${student.id}`} className="grid gap-1 border-b border-stone-100 px-4 py-3 text-sm sm:grid-cols-4">
            <span className="font-medium text-stone-950">{student.fullName}</span>
            <span>{student.primaryUnit.name}</span>
            <span>{student.status}</span>
            <span>{student.joinDate}</span>
          </Link>
        ))}
        {students?.content.length === 0 ? <p className="p-4 text-sm text-stone-600">Nenhum aluno encontrado.</p> : null}
      </div>

      <div className="flex items-center justify-between">
        <button disabled={page === 0} onClick={() => setPage((value) => Math.max(0, value - 1))} className="rounded border border-stone-300 px-3 py-2 text-sm disabled:text-stone-400">Anterior</button>
        <span className="text-sm text-stone-600">Pagina {(students?.page ?? 0) + 1} de {students?.totalPages || 1}</span>
        <button disabled={!students || page + 1 >= students.totalPages} onClick={() => setPage((value) => value + 1)} className="rounded border border-stone-300 px-3 py-2 text-sm disabled:text-stone-400">Proxima</button>
      </div>
    </div>
  );
}

"use client";

import { Badge, StatusBadge } from "@/src/components/ui/Badge";
import { ButtonLink, Button } from "@/src/components/ui/Button";
import { Card } from "@/src/components/ui/Card";
import { Input, Select } from "@/src/components/ui/Form";
import { EmptyState, ErrorState, LoadingState, PageHeader } from "@/src/components/ui/States";
import { bffJson, hasPermission } from "@/src/lib/people/client";
import { type PageResponse, type StudentListItem, type StudentStatus, type UnitSummary } from "@/src/lib/people/types";
import { Search, UserPlus } from "lucide-react";
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
    <div className="space-y-6">
      <PageHeader
        eyebrow="Pessoas"
        title="Alunos"
        description="Encontre alunos, filtre por status ou unidade e acesse rapidamente o perfil administrativo."
        action={canCreate ? <ButtonLink href="/app/students/new"><UserPlus className="h-4 w-4" aria-hidden="true" />Novo aluno</ButtonLink> : null}
      />

      <Card className="p-4">
        <div className="grid gap-3 md:grid-cols-[1fr_180px_220px]">
          <label className="relative">
            <span className="sr-only">Buscar aluno</span>
            <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-mutedForeground" aria-hidden="true" />
            <Input value={search} onChange={(event) => { setPage(0); setSearch(event.target.value); }} placeholder="Buscar aluno..." className="pl-9" />
          </label>
          <Select value={status} onChange={(event) => { setPage(0); setStatus(event.target.value); }} aria-label="Filtrar por status">
            <option value="">Status</option>
            {(["ACTIVE", "TRIAL", "INACTIVE", "SUSPENDED"] satisfies StudentStatus[]).map((value) => <option key={value}>{value}</option>)}
          </Select>
          <Select value={unitId} onChange={(event) => { setPage(0); setUnitId(event.target.value); }} aria-label="Filtrar por unidade">
            <option value="">Unidade</option>
            {units.map((unit) => <option key={unit.id} value={unit.id}>{unit.name}</option>)}
          </Select>
        </div>
      </Card>

      {error ? <ErrorState message={error} /> : null}

      {!students ? <LoadingState label="Carregando alunos" /> : null}

      {students && students.content.length > 0 ? (
        <Card className="overflow-hidden p-0">
          <div className="hidden grid-cols-[1.4fr_1fr_140px_140px] border-b border-border bg-surfaceElevated px-5 py-3 text-xs font-semibold uppercase tracking-[0.14em] text-mutedForeground md:grid">
            <span>Nome</span><span>Unidade</span><span>Status</span><span>Desde</span>
          </div>
          <div className="divide-y divide-border">
            {students.content.map((student) => (
              <Link key={student.id} href={`/app/students/${student.id}`} className="block transition hover:bg-surfaceElevated md:grid md:grid-cols-[1.4fr_1fr_140px_140px] md:items-center md:px-5 md:py-4">
                <div className="p-4 md:p-0">
                  <p className="font-semibold text-foreground">{student.fullName}</p>
                  <p className="mt-1 text-sm text-mutedForeground md:hidden">{student.primaryUnit.name} · desde {formatDate(student.joinDate)}</p>
                </div>
                <span className="hidden text-sm text-mutedForeground md:block">{student.primaryUnit.name}</span>
                <span className="hidden md:block"><StatusBadge status={student.status} /></span>
                <span className="hidden text-sm text-mutedForeground md:block">{formatDate(student.joinDate)}</span>
                <div className="px-4 pb-4 md:hidden"><StatusBadge status={student.status} /></div>
              </Link>
            ))}
          </div>
        </Card>
      ) : null}

      {students && students.content.length === 0 ? (
        <EmptyState title="Nenhum aluno encontrado" description="Ajuste a busca ou cadastre um novo aluno para iniciar a gestao da equipe." action={canCreate ? <ButtonLink href="/app/students/new">Novo aluno</ButtonLink> : null} />
      ) : null}

      {students ? (
        <div className="flex items-center justify-between">
          <Button variant="secondary" disabled={page === 0} onClick={() => setPage((value) => Math.max(0, value - 1))}>Anterior</Button>
          <Badge>Pagina {students.page + 1} de {students.totalPages || 1}</Badge>
          <Button variant="secondary" disabled={page + 1 >= students.totalPages} onClick={() => setPage((value) => value + 1)}>Proxima</Button>
        </div>
      ) : null}
    </div>
  );
}

function formatDate(value: string) {
  const [year, month, day] = value.split("-");
  return `${day}/${month}/${year}`;
}

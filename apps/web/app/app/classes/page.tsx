"use client";

import { Badge, StatusBadge } from "@/src/components/ui/Badge";
import { Button, ButtonLink } from "@/src/components/ui/Button";
import { Card } from "@/src/components/ui/Card";
import { Input, Select } from "@/src/components/ui/Form";
import { EmptyState, ErrorState, LoadingState, PageHeader } from "@/src/components/ui/States";
import { bffJson, hasPermission } from "@/src/lib/people/client";
import { type PageResponse } from "@/src/lib/people/types";
import { audienceLabels, classTypeLabels, levelLabels, modalityLabels, type ClassModality, type ClassStatus, type DojoClass } from "@/src/lib/schedule/types";
import { BookOpen, CalendarPlus, Search } from "lucide-react";
import { useEffect, useState } from "react";

type Me = { permissions: string[] };

export default function ClassesPage() {
  const [me, setMe] = useState<Me | null>(null);
  const [classes, setClasses] = useState<PageResponse<DojoClass> | null>(null);
  const [search, setSearch] = useState("");
  const [modality, setModality] = useState("");
  const [status, setStatus] = useState("");
  const [page, setPage] = useState(0);
  const [error, setError] = useState("");

  useEffect(() => { fetch("/api/auth/me").then((r) => r.json()).then(setMe).catch(() => undefined); }, []);
  useEffect(() => {
    const params = new URLSearchParams({ page: String(page), size: "12" });
    if (search) params.set("search", search);
    if (modality) params.set("modality", modality);
    if (status) params.set("status", status);
    bffJson<PageResponse<DojoClass>>(`/classes?${params.toString()}`).then(setClasses).catch((err: Error) => setError(err.message));
  }, [search, modality, status, page]);

  const canCreate = me ? hasPermission(me.permissions, "CLASS_CREATE") : false;
  const canUpdate = me ? hasPermission(me.permissions, "CLASS_UPDATE") : false;

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Operacao"
        title="Aulas"
        description="Defina os programas oferecidos pela academia. Os horarios ficam na grade semanal."
        action={canCreate ? <ButtonLink href="/app/classes/new"><BookOpen className="h-4 w-4" aria-hidden="true" />Nova aula</ButtonLink> : null}
      />

      <Card className="p-4">
        <div className="grid gap-3 md:grid-cols-[1fr_180px_180px]">
          <label className="relative"><span className="sr-only">Buscar aula</span><Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-mutedForeground" aria-hidden="true" /><Input value={search} onChange={(e) => { setPage(0); setSearch(e.target.value); }} placeholder="Buscar aula..." className="pl-9" /></label>
          <Select value={modality} onChange={(e) => { setPage(0); setModality(e.target.value); }} aria-label="Modalidade"><option value="">Modalidade</option>{(["GI", "NO_GI", "MIXED", "OTHER"] satisfies ClassModality[]).map((item) => <option key={item} value={item}>{modalityLabels[item]}</option>)}</Select>
          <Select value={status} onChange={(e) => { setPage(0); setStatus(e.target.value); }} aria-label="Status"><option value="">Status</option>{(["ACTIVE", "INACTIVE"] satisfies ClassStatus[]).map((item) => <option key={item}>{item}</option>)}</Select>
        </div>
      </Card>

      {error ? <ErrorState message={error} /> : null}
      {!classes ? <LoadingState label="Carregando aulas" /> : null}

      {classes && classes.content.length > 0 ? (
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          {classes.content.map((dojoClass) => (
            <Card key={dojoClass.id} className="group p-5">
              <div className="flex items-start justify-between gap-3">
                <div>
                  <p className="caption-text">{classTypeLabels[dojoClass.classType]}</p>
                  <h2 className="mt-2 text-lg font-semibold text-foreground">{dojoClass.name}</h2>
                  {dojoClass.description ? <p className="mt-2 line-clamp-2 text-sm leading-6 text-mutedForeground">{dojoClass.description}</p> : null}
                </div>
                <StatusBadge status={dojoClass.status} />
              </div>
              <div className="mt-5 flex flex-wrap gap-2">
                <ModalityBadge modality={dojoClass.modality} />
                <Badge>{audienceLabels[dojoClass.audience]}</Badge>
                <Badge>{levelLabels[dojoClass.level]}</Badge>
                <Badge>{dojoClass.defaultCapacity ? `${dojoClass.defaultCapacity} vagas` : "Sem limite"}</Badge>
              </div>
              <div className="mt-5 flex gap-2">
                <ButtonLink href="/app/schedule" variant="secondary" size="sm"><CalendarPlus className="h-4 w-4" aria-hidden="true" />Grade</ButtonLink>
                {canUpdate ? <ButtonLink href={`/app/classes/${dojoClass.id}/edit`} variant="ghost" size="sm">Editar</ButtonLink> : null}
              </div>
            </Card>
          ))}
        </div>
      ) : null}

      {classes?.content.length === 0 ? <EmptyState title="Nenhuma aula cadastrada" description="Crie o primeiro programa da academia e depois organize seus horarios na agenda semanal." action={canCreate ? <ButtonLink href="/app/classes/new">Nova aula</ButtonLink> : null} /> : null}

      {classes ? <div className="flex items-center justify-between"><Button variant="secondary" disabled={page === 0} onClick={() => setPage((value) => Math.max(0, value - 1))}>Anterior</Button><Badge>Pagina {classes.page + 1} de {classes.totalPages || 1}</Badge><Button variant="secondary" disabled={page + 1 >= classes.totalPages} onClick={() => setPage((value) => value + 1)}>Proxima</Button></div> : null}
    </div>
  );
}

function ModalityBadge({ modality }: { modality: ClassModality }) {
  const tone = modality === "NO_GI" ? "primary" : modality === "MIXED" ? "info" : undefined;
  return <Badge tone={tone}>{modalityLabels[modality]}</Badge>;
}

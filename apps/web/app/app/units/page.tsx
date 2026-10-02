import { Card } from "@/src/components/ui/Card";
import { PageHeader } from "@/src/components/ui/States";
import { MapPin } from "lucide-react";

export default function UnitsPage() {
  return (
    <div className="space-y-6">
      <PageHeader eyebrow="Organizacao" title="Unidades" description="As unidades ja existem na API do Cycle 01. A experiencia completa de gestao visual sera expandida em ciclo proprio." />
      <Card className="flex items-start gap-4">
        <div className="grid h-11 w-11 place-items-center rounded-2xl bg-primary/10 text-primary"><MapPin className="h-5 w-5" aria-hidden="true" /></div>
        <div>
          <h2 className="card-title">Gestao de unidades</h2>
          <p className="mt-1 text-sm leading-6 text-mutedForeground">Esta tela mantem a navegacao consistente sem criar funcionalidades fake.</p>
        </div>
      </Card>
    </div>
  );
}

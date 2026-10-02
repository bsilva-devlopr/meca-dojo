import { Card } from "@/src/components/ui/Card";
import { PageHeader } from "@/src/components/ui/States";
import { Badge } from "@/src/components/ui/Badge";
import { getCurrentUser } from "@/src/lib/auth/server";
import { redirect } from "next/navigation";

export default async function AppPage() {
  const user = await getCurrentUser();
  if (!user) {
    redirect("/login");
  }

  return (
    <div className="space-y-6">
      <PageHeader title="Dashboard" eyebrow="Visao geral" description={`Sessao ativa em ${user.tenant.name}. Este espaco fica reservado para dados reais dos proximos ciclos.`} />
      <Card className="overflow-hidden">
        <div className="flex flex-col gap-5 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <p className="caption-text">Conta autenticada</p>
            <h2 className="mt-2 text-2xl font-semibold tracking-tight text-foreground">Ola, {user.name}</h2>
            <p className="mt-1 text-sm text-mutedForeground">{user.email}</p>
          </div>
          <div className="flex flex-wrap gap-2">
            {user.roles.map((role) => <Badge key={role} tone="primary">{role}</Badge>)}
          </div>
        </div>
      </Card>
    </div>
  );
}

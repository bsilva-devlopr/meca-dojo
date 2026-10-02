import { LoginForm } from "./LoginForm";

export default function LoginPage() {
  return (
    <main className="grid min-h-screen bg-foreground text-white lg:grid-cols-[1.05fr_0.95fr]">
      <section className="relative hidden overflow-hidden p-10 lg:flex lg:flex-col lg:justify-between">
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_20%_20%,hsl(var(--primary)/0.45),transparent_28rem)]" />
        <div className="relative">
          <p className="text-sm font-semibold uppercase tracking-[0.24em] text-white/65">MECA DOJO</p>
          <h1 className="mt-8 max-w-xl text-6xl font-semibold leading-[0.95] tracking-tight">Gestao moderna para o tatame.</h1>
          <p className="mt-6 max-w-md text-base leading-7 text-white/62">Uma operacao premium, rapida e mobile-first para equipes e academias de Jiu-Jitsu.</p>
        </div>
        <div className="relative rounded-3xl border border-white/10 bg-white/6 p-5 text-sm text-white/70">
          <p className="font-medium text-white">Identity, alunos e graduacao</p>
          <p className="mt-2">Controle seguro por tenant com experiencia feita para uso diario.</p>
        </div>
      </section>

      <section className="flex min-h-screen items-center justify-center bg-background px-5 py-10 text-foreground">
        <div className="w-full max-w-md">
          <div className="mb-8 lg:hidden">
            <p className="caption-text">MECA DOJO</p>
            <h1 className="mt-3 text-4xl font-semibold tracking-tight text-foreground">Acesse sua equipe</h1>
          </div>
          <div className="rounded-3xl border border-border bg-surface p-6 shadow-soft sm:p-8">
            <p className="caption-text hidden lg:block">MECA DOJO</p>
            <h2 className="mt-2 hidden text-3xl font-semibold tracking-tight lg:block">Acesse sua equipe</h2>
            <p className="mb-6 mt-2 text-sm leading-6 text-mutedForeground">Entre com o tenant, e-mail e senha para continuar.</p>
            <LoginForm />
          </div>
        </div>
      </section>
    </main>
  );
}

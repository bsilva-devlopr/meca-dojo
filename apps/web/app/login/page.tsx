import { LoginForm } from "./LoginForm";

export default function LoginPage() {
  return (
    <main className="flex min-h-screen items-center justify-center px-5 py-10">
      <section className="w-full max-w-sm">
        <p className="text-sm font-semibold uppercase tracking-[0.18em] text-red-800">MECA DOJO</p>
        <h1 className="mt-3 text-3xl font-semibold leading-tight text-stone-950">Acesse sua equipe</h1>
        <div className="mt-8 rounded border border-stone-200 bg-white p-5 shadow-sm">
          <LoginForm />
        </div>
      </section>
    </main>
  );
}

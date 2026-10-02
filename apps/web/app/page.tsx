import { FoundationStatus } from "@/src/lib/api/FoundationStatus";

export default function Home() {
  return (
    <main className="flex min-h-screen items-center justify-center px-6 py-12">
      <section className="w-full max-w-3xl">
        <p className="mb-4 text-sm font-semibold uppercase tracking-[0.18em] text-ember">
          MECA DOJO
        </p>
        <h1 className="max-w-2xl text-4xl font-semibold leading-tight text-graphite sm:text-6xl">
          Gestao inteligente para equipes de Jiu-Jitsu.
        </h1>
        <p className="mt-6 text-lg text-neutral-700">
          Foundation running.
        </p>
        <div className="mt-10">
          <FoundationStatus />
        </div>
      </section>
    </main>
  );
}

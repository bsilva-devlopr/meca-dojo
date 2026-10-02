"use client";

import { useRouter } from "next/navigation";
import { FormEvent, useState } from "react";

type FormState = "idle" | "loading" | "error";

export function LoginForm() {
  const router = useRouter();
  const [state, setState] = useState<FormState>("idle");
  const [message, setMessage] = useState("");

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const formData = new FormData(event.currentTarget);
    const tenant = String(formData.get("tenant") ?? "").trim();
    const email = String(formData.get("email") ?? "").trim();
    const password = String(formData.get("password") ?? "");

    if (!tenant || !email || !password) {
      setState("error");
      setMessage("Preencha equipe, e-mail e senha.");
      return;
    }

    setState("loading");
    setMessage("");

    const response = await fetch("/api/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ tenant, email, password }),
    });

    if (!response.ok) {
      setState("error");
      setMessage("Equipe, e-mail ou senha invalidos.");
      return;
    }

    router.replace("/app");
    router.refresh();
  }

  return (
    <form onSubmit={submit} className="flex w-full flex-col gap-4">
      <label className="flex flex-col gap-2 text-sm font-medium text-stone-800">
        Equipe / Tenant
        <input
          name="tenant"
          autoComplete="organization"
          className="h-12 rounded border border-stone-300 bg-white px-3 text-base outline-none transition focus:border-stone-900"
          defaultValue="meca-dojo"
        />
      </label>

      <label className="flex flex-col gap-2 text-sm font-medium text-stone-800">
        E-mail
        <input
          name="email"
          type="email"
          autoComplete="email"
          className="h-12 rounded border border-stone-300 bg-white px-3 text-base outline-none transition focus:border-stone-900"
          defaultValue="admin@mecadojo.local"
        />
      </label>

      <label className="flex flex-col gap-2 text-sm font-medium text-stone-800">
        Senha
        <input
          name="password"
          type="password"
          autoComplete="current-password"
          className="h-12 rounded border border-stone-300 bg-white px-3 text-base outline-none transition focus:border-stone-900"
        />
      </label>

      {message ? <p className="text-sm text-red-700">{message}</p> : null}

      <button
        type="submit"
        disabled={state === "loading"}
        className="mt-2 h-12 rounded bg-stone-950 px-4 text-sm font-semibold uppercase tracking-wide text-white transition hover:bg-stone-800 disabled:cursor-not-allowed disabled:bg-stone-500"
      >
        {state === "loading" ? "Entrando..." : "ENTRAR"}
      </button>

      <p className="text-center text-sm text-stone-600">Esqueci minha senha</p>
    </form>
  );
}

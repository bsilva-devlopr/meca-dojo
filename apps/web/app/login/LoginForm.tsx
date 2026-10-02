"use client";

import { Button } from "@/src/components/ui/Button";
import { ErrorState } from "@/src/components/ui/States";
import { FormField, Input } from "@/src/components/ui/Form";
import { ArrowRight } from "lucide-react";
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
    <form onSubmit={submit} className="grid w-full gap-4">
      <FormField label="Equipe / Tenant" required>
        <Input name="tenant" autoComplete="organization" defaultValue="meca-dojo" />
      </FormField>

      <FormField label="E-mail" required>
        <Input name="email" type="email" autoComplete="email" defaultValue="admin@mecadojo.local" />
      </FormField>

      <FormField label="Senha" required>
        <Input name="password" type="password" autoComplete="current-password" />
      </FormField>

      {message ? <ErrorState message={message} /> : null}

      <Button type="submit" disabled={state === "loading"} size="lg" className="mt-1 w-full uppercase tracking-[0.16em]">
        {state === "loading" ? "Entrando..." : "Entrar"}
        <ArrowRight className="h-4 w-4" aria-hidden="true" />
      </Button>

      <p className="text-center text-sm text-mutedForeground">Esqueci minha senha</p>
    </form>
  );
}

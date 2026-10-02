# ADR-006 - Frontend Session Strategy

Status: Accepted.

Decisao: usar um BFF leve no Next.js para login, sessao e logout.

Fluxo:

```text
Browser
  -> Next.js /api/auth/*
  -> Spring Boot /api/v1/auth/*
```

O browser nao armazena access token nem refresh token em `localStorage`. O Next.js grava ambos em cookies HttpOnly, `SameSite=Lax`, `Secure` em producao. O refresh token nao fica exposto ao JavaScript do browser.

Motivos:

- protege refresh token contra acesso direto por JavaScript;
- reduz exposicao de CORS para fluxos de autenticacao do browser;
- mantem simplicidade para o MVP;
- preserva uma API Spring direta que podera ser usada por app mobile futuro com estrategia propria de armazenamento seguro.

No Cycle 01, o shell autenticado e minimo e prova login, `/me`, area autenticada e logout. Renovacao proativa no frontend pode evoluir em ciclo posterior.

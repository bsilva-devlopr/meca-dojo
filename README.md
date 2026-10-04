# Meca Dojo

Meca Dojo e uma plataforma web/mobile-first para gestao de equipes e academias de Jiu-Jitsu.

O Cycle 05 implementa treinos datados, transformando a grade semanal recorrente em TrainingSessions operacionais com snapshot, professores da sessao, estados explicitos e telas mobile-first para professores.

## Requisitos

- Java 21 com `JAVA_HOME` apontando para o JDK 21, ou `java.exe` disponivel no `PATH`
- Node.js 22 ou superior
- npm
- Docker
- Docker Compose

O backend usa Maven Wrapper em `apps/api`; nao e necessario instalar Maven globalmente.

## Estrutura

```text
meca-dojo/
├── apps/
│   ├── api/
│   └── web/
├── docs/
├── infra/
├── scripts/
└── .github/
```

## Configuracao local

Copie o arquivo de exemplo antes de subir o ambiente:

```bash
cp .env.example .env
```

No Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

O arquivo `.env.example` documenta as variaveis usadas por PostgreSQL, Redis, MinIO, API e frontend. Nao versione `.env` ou arquivos com secrets reais.

## Subir infraestrutura

```bash
docker compose --project-name meca-dojo up -d postgres redis minio
```

Atalho em PowerShell:

```powershell
.\scripts\dev-up.ps1
```

Atalho em shell Unix:

```bash
./scripts/dev-up.sh
```

## Executar backend

Windows PowerShell:

```powershell
cd apps/api
.\mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
cd apps/api
./mvnw spring-boot:run
```

A API usa PostgreSQL via variaveis `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER` e `DB_PASSWORD`. Em desenvolvimento local, os defaults apontam para o PostgreSQL do Docker Compose.

Para login local inicial, configure tambem:

```powershell
$env:JWT_SECRET="<at-least-32-characters-local-secret>"
$env:BOOTSTRAP_ENABLED="true"
$env:BOOTSTRAP_TENANT_NAME="Meca Dojo"
$env:BOOTSTRAP_TENANT_SLUG="meca-dojo"
$env:BOOTSTRAP_OWNER_NAME="Admin Local"
$env:BOOTSTRAP_OWNER_EMAIL="admin@mecadojo.local"
$env:BOOTSTRAP_OWNER_PASSWORD="<local-owner-password>"
```

O bootstrap e idempotente e deve ser usado apenas em desenvolvimento/local. Em producao, `JWT_SECRET` deve vir do ambiente e ter pelo menos 32 caracteres.

## Executar frontend

```bash
cd apps/web
npm install
npm run dev
```

A URL da API usada pelo BFF vem de `API_BASE_URL`, com fallback local documentado em `.env.example`. `NEXT_PUBLIC_APP_URL` representa a URL publica do frontend quando alguma experiencia precisa conhecer a origem publica da aplicacao.

## Testar backend

Windows PowerShell:

```powershell
cd apps/api
.\mvnw.cmd clean test
```

Linux/macOS:

```bash
cd apps/api
./mvnw clean test
```

Os testes usam Testcontainers para subir PostgreSQL e validar conexao e Flyway.

## Validar frontend

```bash
cd apps/web
npm install
npm run lint
npm run build
```

## URLs locais

- Frontend: http://localhost:3000
- Login: http://localhost:3000/login
- Area autenticada: http://localhost:3000/app
- API: http://localhost:8080
- Actuator health: http://localhost:8080/actuator/health
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- PostgreSQL: localhost:5432
- Redis: localhost:6379
- MinIO API: http://localhost:9000
- MinIO Console: http://localhost:9001

## Endpoints principais

- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `GET /api/v1/auth/me`
- `GET /api/v1/tenant`
- `PATCH /api/v1/tenant`
- `GET /api/v1/units`
- `POST /api/v1/units`
- `GET /api/v1/units/{id}`
- `PATCH /api/v1/units/{id}`
- `GET /api/v1/users`
- `POST /api/v1/users`
- `GET /api/v1/users/{id}`
- `PATCH /api/v1/users/{id}`
- `PUT /api/v1/users/{id}/roles`
- `GET /api/v1/roles`
- `GET /api/v1/students`
- `POST /api/v1/students`
- `GET /api/v1/students/{id}`
- `PATCH /api/v1/students/{id}`
- `PUT /api/v1/students/{id}/user`
- `PUT /api/v1/students/{id}/emergency-contact`
- `DELETE /api/v1/students/{id}/emergency-contact`
- `POST /api/v1/students/{id}/guardians`
- `POST /api/v1/students/{id}/guardians/new`
- `DELETE /api/v1/students/{studentId}/guardians/{guardianId}`
- `GET /api/v1/guardians`
- `POST /api/v1/guardians`
- `GET /api/v1/guardians/{id}`
- `PATCH /api/v1/guardians/{id}`
- `PUT /api/v1/guardians/{id}/user`
- `GET /api/v1/staff`
- `POST /api/v1/staff`
- `GET /api/v1/staff/{id}`
- `PATCH /api/v1/staff/{id}`
- `GET /api/v1/belts`
- `GET /api/v1/students/{studentId}/graduation`
- `GET /api/v1/students/{studentId}/graduations`
- `POST /api/v1/students/{studentId}/graduations/initial`
- `POST /api/v1/students/{studentId}/graduations/degree`
- `POST /api/v1/students/{studentId}/graduations/belt`
- `POST /api/v1/students/{studentId}/graduations/correction`
- `GET /api/v1/graduation-policies`
- `PUT /api/v1/graduation-policies/{beltCode}`
- `GET /api/v1/classes`
- `POST /api/v1/classes`
- `GET /api/v1/classes/{id}`
- `PATCH /api/v1/classes/{id}`
- `GET /api/v1/schedules`
- `POST /api/v1/schedules`
- `GET /api/v1/schedules/weekly`
- `GET /api/v1/schedules/{id}`
- `PATCH /api/v1/schedules/{id}`
- `GET /api/v1/training-sessions`
- `POST /api/v1/training-sessions`
- `POST /api/v1/training-sessions/generate`
- `GET /api/v1/training-sessions/{id}`
- `PATCH /api/v1/training-sessions/{id}`
- `POST /api/v1/training-sessions/{id}/open`
- `POST /api/v1/training-sessions/{id}/complete`
- `POST /api/v1/training-sessions/{id}/cancel`

## RBAC inicial

Roles e permissions sao globais e definidas pelo sistema. Nao existem custom roles no Cycle 01.

- OWNER: todas as permissions atuais.
- ADMIN: permissions administrativas de Tenant, Unit, User, Role, Student, Guardian, Staff, Graduation, Graduation Policy, Classes e Schedules.
- PROFESSOR: leitura de alunos/responsaveis, leitura/criacao de graduacoes, leitura de aulas/agenda e administracao operacional de treinos.
- INSTRUCTOR: leitura/criacao de graduacoes, leitura de aulas/agenda, leitura de treinos, abertura e conclusao.
- RECEPTION: leitura/criacao/edicao de alunos e responsaveis.
- RECEPTION tambem possui leitura de graduacao, aulas, agenda e treinos, alem de criar/editar/cancelar treinos.
- STUDENT e GUARDIAN: sem permissions administrativas neste ciclo.

## Encerrar infraestrutura

```bash
docker compose --project-name meca-dojo down
```

Atalho em PowerShell:

```powershell
.\scripts\dev-down.ps1
```

## Cycle 06 - Attendance & Check-in

Cycle 06 adiciona presenca oficial em TrainingSession e check-in por QR temporario. `Attendance` e a fonte de verdade de frequencia; `CheckIn` registra tentativas operacionais, inclusive duplicadas, expiradas ou rejeitadas.

Endpoints adicionados:

- `GET /api/v1/training-sessions/{sessionId}/attendance`
- `POST /api/v1/training-sessions/{sessionId}/attendance`
- `POST /api/v1/training-sessions/{sessionId}/check-in-token`
- `POST /api/v1/check-ins`
- `POST /api/v1/attendance/{id}/void`
- `GET /api/v1/students/{studentId}/attendance`
- `GET /api/v1/students/{studentId}/attendance/summary`

Permissions adicionadas:

- `ATTENDANCE_READ`
- `ATTENDANCE_CREATE`
- `ATTENDANCE_VOID`
- `CHECKIN_TOKEN_CREATE`
- `CHECKIN_AUDIT_READ`

Self check-in nao usa permission administrativa; exige usuario autenticado, Student vinculado, token valido, TrainingSession OPEN e Student ACTIVE/TRIAL.

## Deployment

Cycle 06.1 define a fundacao de staging:

```text
Vercel (Next.js + BFF)
  -> HTTPS
Spring Boot API em runtime Java/container
  -> PostgreSQL gerenciado
```

O backend nao roda em Vercel Functions. Ele e empacotado como imagem Docker em `apps/api/Dockerfile` e deve rodar em runtime apropriado para Java 21/Spring Boot.

Documentacao operacional:

- `docs/deployment/architecture.md`
- `docs/deployment/environments.md`
- `docs/deployment/vercel.md`
- `docs/deployment/backend-host.md`
- `docs/deployment/staging-runbook.md`
- `docs/adr/ADR-012-production-deployment.md`

Variaveis principais:

- API: `SPRING_PROFILES_ACTIVE`, `PORT`, `JDBC_DATABASE_URL` ou `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `JWT_SECRET`, `APP_CORS_ALLOWED_ORIGINS`, `APP_PUBLIC_URL`, `BOOTSTRAP_ENABLED`.
- Web: `API_BASE_URL`, `NEXT_PUBLIC_APP_URL`.

Use `apps/api/.env.example` e `apps/web/.env.example` como templates. Nao versionar `.env` real, tokens de provider, senhas, JWT secrets ou URLs com credenciais.

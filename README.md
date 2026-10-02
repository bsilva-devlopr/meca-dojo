# Meca Dojo

Meca Dojo e uma plataforma web/mobile-first para gestao de equipes e academias de Jiu-Jitsu.

O Cycle 00 prepara apenas a fundacao tecnica do monorepo. Funcionalidades de negocio como identidade, alunos, turmas, graduacoes, check-in e pagamentos serao implementadas em ciclos futuros.

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

## Executar frontend

```bash
cd apps/web
npm install
npm run dev
```

A URL da API vem de `NEXT_PUBLIC_API_URL`, com default documentado em `.env.example`.

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
- API: http://localhost:8080
- Actuator health: http://localhost:8080/actuator/health
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- PostgreSQL: localhost:5432
- Redis: localhost:6379
- MinIO API: http://localhost:9000
- MinIO Console: http://localhost:9001

## Encerrar infraestrutura

```bash
docker compose --project-name meca-dojo down
```

Atalho em PowerShell:

```powershell
.\scripts\dev-down.ps1
```

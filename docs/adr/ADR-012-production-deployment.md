# ADR-012 - Production Deployment Foundation

## Status

Accepted

## Context

Meca Dojo needs a staging deployment architecture without distorting the application. The frontend is Next.js with BFF routes and HttpOnly cookies. The backend is Spring Boot Java 21 with Flyway and PostgreSQL.

## Decision

Use Vercel for the Next.js frontend under `apps/web`. Keep Browser -> Next.js BFF -> Spring Boot API as the request model. The BFF uses private `API_BASE_URL`; the browser does not handle access or refresh tokens directly.

Use a container-capable Java runtime for the API. Railway is the selected first staging backend host because it supports Dockerfile deployments, Spring Boot, managed PostgreSQL, environment variables, logs and health checks with low operational overhead for MVP/pilot.

PostgreSQL is required and managed by the backend host. Redis remains optional/future because no current runtime feature depends on it. Object storage remains optional/future; when needed, use S3-compatible storage rather than requiring MinIO in production.

Flyway remains schema authority. Hibernate `ddl-auto` stays `validate`. Flyway `clean` is disabled. The API supports provider database URLs through `JDBC_DATABASE_URL` or `DATABASE_URL` and accepts cloud `PORT`.

Bootstrap is disabled by default outside explicit local/one-time setup. Staging owner creation may use one-time bootstrap with secrets in the provider environment, followed by redeploy with `BOOTSTRAP_ENABLED=false`.

## Consequences

The architecture can start with provider domains and later move to `app.<domain>` and `api.<domain>` without code changes. Deploy requires provider authentication and environment variables, but no secrets are committed.

## Cycle 06.1 staging deployment

Cycle 06.1 deployed the first staging environment:

- Vercel project `meca-dojo`, root directory `apps/web`.
- Railway project `meca-dojo`.
- Railway API service `meca-dojo-api`.
- Railway PostgreSQL service `Postgres`.
- Web URL `https://meca-dojo.vercel.app`.
- API URL `https://meca-dojo-api-production.up.railway.app`.

The Vercel production alias is used as the staging URL for the MVP phase. The backend remains `SPRING_PROFILES_ACTIVE=staging`, and `BOOTSTRAP_ENABLED=false` is the required final state after any one-time owner bootstrap.

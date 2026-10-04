# Environments

## local

Local development uses Docker Compose for PostgreSQL, Redis and MinIO. Redis and MinIO are available for future features, but the application currently depends only on PostgreSQL.

Use `.env.example` as a template and provide local-only secrets.

## staging

Staging is the primary Cycle 06.1 target.

- Web: Vercel project with root directory `apps/web`.
- API: Railway service with root directory `apps/api` and Dockerfile build.
- Database: managed PostgreSQL attached to the backend service.
- `SPRING_PROFILES_ACTIVE=staging`.
- `BOOTSTRAP_ENABLED=false` by default.

Current staging URLs:

```text
Web: https://meca-dojo.vercel.app
API: https://meca-dojo-api-production.up.railway.app
```

The Vercel production alias is being used as the staging URL during the MVP phase.

## production

Production uses the same topology as staging, with production secrets, production URLs and `SPRING_PROFILES_ACTIVE=prod`. Production is not automatically deployed in this cycle.

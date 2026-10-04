# Deployment architecture

Cycle 06.1 defines the first staging-ready deployment architecture for Meca Dojo.

```text
Internet
  -> Vercel
     Next.js + BFF
  -> HTTPS
  -> Railway
     Spring Boot Java 21 API
  -> Railway PostgreSQL
```

The Spring Boot API is not adapted to Vercel Functions. It remains a long-running Java 21 service, packaged as a Docker image.

## Responsibilities

- Vercel serves the Next.js app and BFF routes under `apps/web`.
- The BFF calls the API with the private `API_BASE_URL` variable.
- Browser tokens remain in HttpOnly cookies owned by the Vercel app domain.
- Spring Boot owns domain rules, RBAC, tenant isolation and Flyway migrations.
- PostgreSQL is managed by the backend host/provider.

## Runtime dependencies

PostgreSQL is required. Redis and object storage are not required by runtime features through Cycle 06 and remain optional/future dependencies.

## Current staging endpoints

- Web: `https://meca-dojo.vercel.app`
- API: `https://meca-dojo-api-production.up.railway.app`
- Health: `https://meca-dojo-api-production.up.railway.app/actuator/health`

The current Vercel production alias is being used as the staging URL for the MVP phase. It is not the final production environment.

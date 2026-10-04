# Backend host

Cycle 06.1 evaluates Railway, Render and Fly.io for the Spring Boot API.

## Evaluation

Railway supports Dockerfile-based services, Spring Boot deployment, managed PostgreSQL, environment variables, public HTTPS domains, logs and health checks. It also has monorepo and config-as-code support.

Render supports Docker services, managed PostgreSQL, environment variables and health checks. It is a viable alternative, but initial monorepo/container setup is slightly less direct for this project than Railway.

Fly.io supports Dockerized apps and Java/Spring Boot, with strong regional/runtime control. It is powerful, but asks for more operational decisions earlier, including app machines, regions and database attachment.

## Decision

Use Railway for the first staging backend because it keeps the MVP operational model simple: one Dockerized Spring Boot service, one managed PostgreSQL database, environment variables, logs and `/actuator/health`.

Do not introduce Kubernetes, ECS/EKS, Terraform or a custom autoscaling stack in this cycle.

## Service settings

- Service root: `apps/api`
- Builder: Dockerfile
- Health check path: `/actuator/health`
- Profile: `SPRING_PROFILES_ACTIVE=staging`
- Required database: managed PostgreSQL

`apps/api/railway.json` stores the health check and restart policy.

## Current staging resources

- Railway project: `meca-dojo`
- API service: `meca-dojo-api`
- PostgreSQL service: `Postgres`
- API URL: `https://meca-dojo-api-production.up.railway.app`
- Health URL: `https://meca-dojo-api-production.up.railway.app/actuator/health`

The API uses Railway environment variables and Postgres variable references. Secrets such as JWT and database credentials stay in Railway and are not committed.

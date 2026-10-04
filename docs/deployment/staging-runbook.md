# Staging runbook

## 1. Provision backend

1. Create or connect a Railway project.
2. Add a PostgreSQL database.
3. Add a service from the Git repository with root directory `apps/api`.
4. Confirm Dockerfile build is selected.
5. Configure environment variables from `apps/api/.env.example`.
6. Set `BOOTSTRAP_ENABLED=false` after any one-time owner bootstrap is complete.

## 2. Provision web

1. Create a Vercel project from the same repository.
2. Set Root Directory to `apps/web`.
3. Configure `API_BASE_URL` with the Railway API URL.
4. Configure `NEXT_PUBLIC_APP_URL` with the Vercel URL.
5. Deploy Preview/Staging.

## 3. Initial owner strategy

Do not use default credentials in staging. If there is no owner creation flow yet, use one-time bootstrap with secrets provided through the backend host environment:

```text
BOOTSTRAP_ENABLED=true
BOOTSTRAP_TENANT_NAME=<real tenant>
BOOTSTRAP_TENANT_SLUG=<slug>
BOOTSTRAP_OWNER_NAME=<owner>
BOOTSTRAP_OWNER_EMAIL=<owner email>
BOOTSTRAP_OWNER_PASSWORD=<random temporary password>
```

After the first successful login is verified, redeploy with `BOOTSTRAP_ENABLED=false`.

Current staging has completed this procedure and must remain with:

```text
BOOTSTRAP_ENABLED=false
```

## 4. Smoke checklist

- API `/actuator/health` returns UP.
- Flyway applies V1 through V7 in an empty database.
- Web opens over HTTPS.
- Login works.
- `/app`, students, classes, schedule and training pages load.
- Training detail shows Attendance.
- Student profile shows real frequency.
- QR URL uses public HTTPS, not localhost.
- Student login after QR preserves `returnTo` and creates Attendance ACTIVE.
- Logout clears the session.

## 5. Restart/redeploy

Restart or redeploy the backend once. Verify data persists, Flyway validates without reapplying destructive operations and login/refresh still work.

## 6. Current staging URLs

```text
Web:    https://meca-dojo.vercel.app
API:    https://meca-dojo-api-production.up.railway.app
Health: https://meca-dojo-api-production.up.railway.app/actuator/health
```

The current Vercel production alias is the staging URL for this MVP phase.

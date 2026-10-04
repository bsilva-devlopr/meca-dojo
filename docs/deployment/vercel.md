# Vercel deployment

The frontend remains in the monorepo under `apps/web`.

## Project settings

- Root Directory: `apps/web`
- Install Command: `npm ci`
- Build Command: `npm run build`
- Framework: Next.js

`apps/web/vercel.json` records the install and build commands for this project root.

The Root Directory setting is the Vercel monorepo mechanism used for this project. The frontend stays under `apps/web`; no files are moved to the repository root for deployment.

## Environment variables

Required for Preview/Staging:

```text
API_BASE_URL=https://meca-dojo-api-production.up.railway.app
NEXT_PUBLIC_APP_URL=https://meca-dojo.vercel.app
```

`API_BASE_URL` is server-side/private and is used by the BFF. Do not expose secrets through `NEXT_PUBLIC_*`.

## Cookies and BFF

The browser talks to Vercel routes. Access and refresh tokens stay in HttpOnly cookies. In production builds, cookies are marked `Secure`.

## Current staging deployment

- Project: `meca-dojo`
- Owner scope: `silvadevevolper1-2215s-projects`
- Staging URL: `https://meca-dojo.vercel.app`
- Deployment model: Vercel production alias used as staging during MVP.

The project has persistent Production and Preview environment variables for `API_BASE_URL` and `NEXT_PUBLIC_APP_URL`. No server-only secret is intentionally exposed through `NEXT_PUBLIC_*`.

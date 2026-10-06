# Student Experience — Cycle 07

The student's home prioritizes a real available training session, QR guidance, current belt/degree and completed frequency. `/app/history` presents paginated official training history, `/app/progress` presents graduation history and configured academy criteria, and `/app/profile` presents personal information for consultation.

An available session is not an enrollment or reservation. Sessions belong to the student's primary unit. A valid QR and an authenticated user linked to an ACTIVE/TRIAL Student are still required for self check-in. The home cannot register attendance without a token.

Attendance during OPEN is shown separately as registered presence awaiting completion. Official history, frequency and graduation criteria count only ACTIVE attendance in COMPLETED sessions. CANCELLED and VOIDED remain auditable and contribute zero.

Academy criteria are informational: the professor decides graduation. There are no predicted promotion dates or automatic promotion. Unconfigured policy, missing graduation, empty history and unavailable sessions have explicit empty states.

INACTIVE/SUSPENDED retain consultation access. The student profile is read-only; corrections to personal data go through the academy. Guardian portal, enrollment, streaks, badges, financial information and announcements remain outside this cycle.

## Final review — 2026-10-05

`npm.cmd audit --json` reports 7 high and 2 moderate affected packages. Both moderate entries are transitive development/build dependencies already present in the Cycle 06.1 lockfile: `tailwindcss@3.4.19 -> postcss-selector-parser@6.1.4` and `tailwindcss@3.4.19 -> postcss-nested@6.2.0 -> postcss-selector-parser@6.1.4`. The nested package inherits the parser advisory; these are two package entries for one vulnerability, not two independent advisories.

[GHSA-rj75-hqrm-r3gf / CVE-2026-104844](https://github.com/advisories/GHSA-rj75-hqrm-r3gf) concerns quadratic selector parsing and CPU exhaustion. The parser fix is version 7.1.6. The advisory was added to the GitHub Advisory Database on 2026-10-05, after the Cycle 06.1 baseline commit (2026-10-04). `package.json` and `package-lock.json` have identical Git blob hashes in that baseline (`97a6d75`) and current HEAD (`2614cfd`); Cycle 07 introduced no dependency change. The additional moderate findings reflect advisory database coverage, not a new Cycle 07 dependency.

These packages process repository CSS at build time, rather than user-supplied selectors in a request handler. The advisory excludes ordinary build-time use on trusted sources. npm proposes Tailwind 4.3.3 for the parser finding, marked as a SemVer-major upgrade; the current consumers require parser 6.x, so installing parser 7.1.6 is not a compatible lockfile-only update. The inherited `postcss-nested` finding is marked fixable but has no separate patched version in this audit. Defer the coordinated build-tool upgrade to a dedicated dependency review; no forced audit fix or override was applied. The existing 7 high findings remain technical debt.

`smoke.cjs`, `visual-final.cjs` and `production-visual.cjs` were temporary Cycle 07 validation scripts under the Windows temporary directory, never tracked or untracked repository files. Their temporary copies were removed after review. Browser screenshots and validation evidence remain outside the repository; no credentials or local environment files are included in Git.

Attendance integration tests were rerun: 7 tests, 0 failures, 0 errors, 0 skipped, BUILD SUCCESS. They confirm OPEN contributes zero official trainings, completion makes ACTIVE attendance count, cancellation contributes zero, voiding removes eligibility contribution, and a second QR check-in while OPEN returns DUPLICATE with exactly one ACTIVE attendance. No product code changed in this review.

# ADR-013 — Student self-service and completed training counts

Status: Accepted for Cycle 07 review.

## Context

Administrative student APIs accept student IDs and require management permissions. Students need an experience based on their own identity without receiving those permissions. Attendance is recorded during OPEN sessions, which can subsequently be cancelled.

## Decision

Expose read-only `/api/v1/student/me` APIs. Resolve the authenticated tenant and user through `Student.userId` on every request; neither query parameters nor request bodies select another student. A linked user can access self-service regardless of administrative roles. An unlinked account receives 404. No administrative permissions are granted to STUDENT.

The dashboard aggregates identity, current graduation, completed frequency, up to three available sessions in the student's primary unit, five recent completed trainings and four recent graduation events. Separate profile, paginated training history and graduation endpoints support focused views. History size is clamped to 1–50; graduation history is limited to the latest 50 events. Public self-service DTOs exclude administrative notes, eligibility reasons, audit users and tenant/user/student IDs. Session and attendance identifiers remain opaque references, never displayed in the UI.

Official totals, frequency history and `GraduationEligibility.minimumTrainingCount` require both `Attendance.status = ACTIVE` and `TrainingSession.status = COMPLETED`. ACTIVE presence during OPEN is operational only. Cancellation preserves attendance for audit but contributes zero official trainings. VOIDED never counts. Completing a session makes its valid attendances count naturally, without copying counters or rewriting attendance.

Frequency windows use `TrainingSession.sessionDate`, not the timestamp when a staff member recorded attendance. The last 30/90 days include today and the preceding 29/89 local dates. Current month uses a half-open local-date interval. The shared operational clock uses `APP_OPERATIONAL_ZONE`, default `America/Sao_Paulo`. Unit timezone is not yet modeled: all units currently share that configured zone. Session date/time values are never silently converted to UTC; browser labels use browser-local today/tomorrow. Users in a different zone can see a different relative-day label until Unit timezone is modeled.

Upcoming sessions are actual SCHEDULED/OPEN TrainingSessions of the primary unit whose operational end time has not passed. No enrollment is inferred: the UI says available sessions in the student's unit. No schedule occurrences are invented. ACTIVE/TRIAL receive normal read access and available sessions. INACTIVE/SUSPENDED retain read-only profile/history/graduation access, but receive no upcoming operation suggestions; existing check-in validation continues to reject them.

STUDENT users see Início, Treinos (history), Evolução and Perfil. Users with both STUDENT and a management role can use Gestão/Área do aluno links to move between the experiences. Administrative routes and their backend permissions remain intact. Non-STUDENT dashboards remain unchanged.

The dashboard check-in action explains how to scan the professor's QR. It never bypasses CheckInToken. Duplicate detection checks ACTIVE attendance for the session independently of the completed history, so OPEN QR check-in continues to return DUPLICATE correctly.

## Consequences

This supersedes the immediate-frequency rule in ADR-011. No schema change or V8 migration is needed. History pagination runs in the database; session staff are loaded in a separate bulk query to avoid collection-fetch pagination and per-row staff loading. Self-service remains read-only, and future Guardian scope can be implemented separately without changing student identity resolution.

No streak, enrollment, finance, announcements, gamification or automatic promotion is introduced. Staging deployment follows human review and is not part of this round.

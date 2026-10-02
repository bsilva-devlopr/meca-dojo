# ADR-005 - RBAC

Status: Accepted.

Decisao: implementar RBAC com roles globais fixas e permissions globais fixas no Cycle 01.

Roles iniciais:

- OWNER
- ADMIN
- PROFESSOR
- INSTRUCTOR
- RECEPTION
- STUDENT
- GUARDIAN

Permissions iniciais:

- TENANT_READ
- TENANT_UPDATE
- UNIT_READ
- UNIT_CREATE
- UNIT_UPDATE
- USER_READ
- USER_CREATE
- USER_UPDATE
- ROLE_READ
- ROLE_ASSIGN

Mapping atual:

- OWNER possui todas as permissions atuais.
- ADMIN possui permissions administrativas de Tenant, Unit, User e Role.
- PROFESSOR, INSTRUCTOR, RECEPTION, STUDENT e GUARDIAN comecam sem permissions administrativas neste ciclo.

Um usuario pode ter mais de uma role via `user_roles`. Autorizacao em endpoints usa permissions, nao apenas `hasRole`.

Custom roles ficam fora do Cycle 01.

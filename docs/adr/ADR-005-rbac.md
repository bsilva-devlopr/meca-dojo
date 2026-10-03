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
- STUDENT_READ
- STUDENT_CREATE
- STUDENT_UPDATE
- GUARDIAN_READ
- GUARDIAN_CREATE
- GUARDIAN_UPDATE
- STAFF_READ
- STAFF_CREATE
- STAFF_UPDATE
- GRADUATION_READ
- GRADUATION_CREATE
- GRADUATION_CORRECT
- GRADUATION_POLICY_READ
- GRADUATION_POLICY_UPDATE
- CLASS_READ
- CLASS_CREATE
- CLASS_UPDATE
- SCHEDULE_READ
- SCHEDULE_CREATE
- SCHEDULE_UPDATE

Mapping atual:

- OWNER possui todas as permissions atuais.
- ADMIN possui permissions administrativas de Tenant, Unit, User, Role, Student, Guardian, Staff, Graduation, Graduation Policy, Classes e Schedules.
- PROFESSOR possui STUDENT_READ, GUARDIAN_READ, GRADUATION_READ, GRADUATION_CREATE, CLASS_READ e SCHEDULE_READ.
- INSTRUCTOR possui GRADUATION_READ, GRADUATION_CREATE, CLASS_READ e SCHEDULE_READ.
- RECEPTION possui leitura, criacao e edicao de Student e Guardian, alem de GRADUATION_READ, CLASS_READ e SCHEDULE_READ.
- STUDENT e GUARDIAN continuam sem permissions administrativas neste ciclo.

Um usuario pode ter mais de uma role via `user_roles`. Autorizacao em endpoints usa permissions, nao apenas `hasRole`.

Custom roles ficam fora do Cycle 01.

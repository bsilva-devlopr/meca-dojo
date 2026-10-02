# Domain Model

Tenant representa a organizacao/equipe/academia. Unit representa uma unidade fisica pertencente a um Tenant.

User representa identidade e autenticacao. Neste ciclo, User pertence a exatamente um Tenant e pode possuir multiplas Roles. Roles concedem Permissions.

Student, Professor e Guardian representam perfis de dominio ligados a usuarios quando aplicavel, mas ainda nao foram implementados como entidades de negocio. No Cycle 01, STUDENT, PROFESSOR e GUARDIAN existem apenas como roles de autorizacao.

Student possui BeltHistory.
Class possui schedules e professores.
TrainingSession representa uma ocorrencia concreta da aula.
Attendance liga Student a TrainingSession e registra origem/status do check-in.
AuditLog registra mudancas criticas.

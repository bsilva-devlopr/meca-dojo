# Domain Model
Tenant → Units.
Users recebem Roles/Permissions.
Student, Professor e Guardian representam perfis de domínio ligados a usuários quando aplicável.
Student possui BeltHistory.
Class possui schedules e professores.
TrainingSession representa uma ocorrência concreta da aula.
Attendance liga Student a TrainingSession e registra origem/status do check-in.
AuditLog registra mudanças críticas.

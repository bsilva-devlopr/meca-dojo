# Domain Model

Tenant representa a organizacao/equipe/academia. Unit representa uma unidade fisica pertencente a um Tenant.

User representa identidade e autenticacao. Neste ciclo, User pertence a exatamente um Tenant e pode possuir multiplas Roles. Roles concedem Permissions.

Student, StaffProfile e Guardian representam perfis de dominio ligados a usuarios quando aplicavel.

User continua sendo identidade, login e seguranca. Student representa a ficha real do aluno. StaffProfile representa a pessoa exercendo funcao de PROFESSOR ou INSTRUCTOR na academia. Guardian representa responsavel por alunos.

User pode existir sem Student. Student e Guardian podem existir sem User. StaffProfile exige User neste MVP. O mesmo User pode ter Student e StaffProfile ao mesmo tempo.

Roles como PROFESSOR e STUDENT continuam sendo autorizacao. StaffProfile type PROFESSOR e Student continuam sendo dominio.

Student possui BeltHistory.
Class possui schedules e professores.
TrainingSession representa uma ocorrencia concreta da aula.
Attendance liga Student a TrainingSession e registra origem/status do check-in.
AuditLog registra mudancas criticas.

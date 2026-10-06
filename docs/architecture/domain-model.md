# Domain Model

Tenant representa a organizacao/equipe/academia. Unit representa uma unidade fisica pertencente a um Tenant.

User representa identidade e autenticacao. Neste ciclo, User pertence a exatamente um Tenant e pode possuir multiplas Roles. Roles concedem Permissions.

Student, StaffProfile e Guardian representam perfis de dominio ligados a usuarios quando aplicavel.

User continua sendo identidade, login e seguranca. Student representa a ficha real do aluno. StaffProfile representa a pessoa exercendo funcao de PROFESSOR ou INSTRUCTOR na academia. Guardian representa responsavel por alunos.

User pode existir sem Student. Student e Guardian podem existir sem User. StaffProfile exige User neste MVP. O mesmo User pode ter Student e StaffProfile ao mesmo tempo.

Roles como PROFESSOR e STUDENT continuam sendo autorizacao. StaffProfile type PROFESSOR e Student continuam sendo dominio.

Graduation representa eventos de graduacao do Student. O historico de graduacao e a fonte de verdade: uma graduacao atual e obtida do evento mais recente criado para o aluno dentro do tenant. O sistema suporta graduacao inicial, promocao de grau, promocao de faixa e correcao administrativa.

Belt e catalogo global do sistema, com faixas GENERAL e KIDS. GraduationPolicy e configuracao por tenant para indicar elegibilidade de avaliacao, inicialmente com dias minimos na graduacao e campo preparado para frequencia futura. Elegibilidade nunca promove aluno automaticamente; professor continua tomando a decisao final.

Student sem historico de graduacao e considerado sem graduacao cadastrada, nao faixa branca presumida.
DojoClass representa a definicao tenant-scoped de uma aula/programa, como Jiu-Jitsu Adulto, No-Gi Avancado ou Open Mat. O nome evita conflito com `java.lang.Class`.

ClassSchedule representa a recorrencia semanal planejada de uma DojoClass em uma Unit, com dia, horario, capacidade opcional e professores/assistentes. DojoClass nao pertence a Unit; a unidade fica no schedule para permitir a mesma aula em varias unidades.

TrainingSession representa uma ocorrencia concreta da aula em uma data especifica. Ela guarda snapshot operacional proprio de aula, unidade, data, horario e capacidade para preservar o historico quando a grade recorrente muda depois. `classSchedule` e opcional para permitir treinos extras ou eventos especiais sem recorrencia semanal.

TrainingSessionStaff representa professores/assistentes daquela sessao especifica. A criacao por schedule copia os responsaveis da grade, mas substituicoes posteriores alteram apenas a sessao.
Attendance liga Student a TrainingSession e registra origem/status do check-in.
AuditLog registra mudancas criticas.

## Attendance & Check-in

`Attendance` representa a presenca oficial e e a fonte de verdade para frequencia. Conta oficialmente apenas quando `status = ACTIVE` e TrainingSession esta COMPLETED. `VOIDED` preserva auditoria de correcoes operacionais e permite registrar uma nova presenca ACTIVE para o mesmo aluno e sessao.

`CheckIn` representa a tentativa operacional, nao a frequencia. Um QR expirado, uma tentativa duplicada ou uma conta sem Student vinculado gera CheckIn auditavel sem criar Attendance.

Self check-in resolve `User -> Student` no backend. O navegador envia apenas o token opaco do QR; nunca escolhe `studentId` nesse fluxo. Student ACTIVE e TRIAL podem treinar; INACTIVE e SUSPENDED sao rejeitados.

Attendance pode ser registrada enquanto a TrainingSession esta OPEN. A presenca e operacional enquanto OPEN; aparece no historico oficial e resumo apenas depois de COMPLETED. CANCELLED nunca conta. Sessoes SCHEDULED, COMPLETED e CANCELLED nao aceitam novas presencas.
## Student self-service (Cycle 07)

Student identity is resolved by authenticated tenant and User through Student.userId. The self-service view derives graduation from StudentGraduation history and frequency from ACTIVE Attendance in COMPLETED TrainingSessions. OPEN attendance is operational only; cancellation retains audit records without increasing official totals. See ADR-013.

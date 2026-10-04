# Data Model

Cycle 00 criou `foundation_schema_marker`, uma tabela tecnica minima para validar conexao com PostgreSQL e Flyway.

Cycle 01 cria a fundacao de Identity e Multi-tenancy:

- `tenants`: organizacao/equipe/academia. `slug` e unico globalmente.
- `units`: unidade fisica de um tenant. `tenant_id` e obrigatorio e `UNIQUE(tenant_id, slug)` impede duplicidade dentro do tenant.
- `users`: identidade de autenticacao. `tenant_id` e obrigatorio e `UNIQUE(tenant_id, email)` permite o mesmo e-mail em tenants diferentes.
- `roles`: roles globais fixas do sistema.
- `permissions`: permissions globais fixas do sistema.
- `user_roles`: relacionamento muitos-para-muitos entre users e roles.
- `role_permissions`: relacionamento muitos-para-muitos entre roles e permissions.
- `refresh_tokens`: refresh tokens opacos, com apenas `token_hash` persistido, expiracao e revogacao.

Cycle 02 cria People & Student Management:

- `students`: ficha administrativa do aluno. Possui `tenant_id`, `user_id` opcional, `primary_unit_id`, dados pessoais basicos, status, data de inicio e observacoes administrativas.
- `student_emergency_contacts`: um contato de emergencia principal por aluno.
- `guardians`: responsaveis, com `user_id` opcional.
- `guardian_students`: relacionamento N:N entre responsaveis e alunos, com relacionamento e `is_primary`.
- `staff_profiles`: perfil de negocio de professor/instrutor. `user_id` e obrigatorio e nao substitui roles de autorizacao.

Cycle 03 cria Belts & Graduation:

- `belts`: catalogo global de faixas, com `code`, `display_name`, `system`, `sequence`, `max_degree` e `active`. `UNIQUE(system, code)` permite `WHITE` em GENERAL e KIDS; `UNIQUE(system, sequence)` garante ordenacao por sistema.
- `student_graduations`: historico tenant-scoped de eventos de graduacao. Guarda `student_id`, `belt_id`, `degree`, `graduated_at`, `awarded_by_staff_id`, `type`, `created_by_user_id`, `corrects_graduation_id`, `correction_reason` e `created_at`.
- `graduation_policies`: politica tenant-scoped por faixa, com `minimum_days_in_grade`, `minimum_training_count` preparado para ciclo futuro de frequencia, `active` e `UNIQUE(tenant_id, belt_id)`.

Cycle 04 cria Classes & Schedule:

- `dojo_classes`: catalogo tenant-scoped de aulas/programas oferecidos pela academia. Guarda modalidade, publico, tipo, nivel, status e capacidade padrao opcional.
- `class_schedules`: grade semanal recorrente tenant-scoped. Liga uma aula a uma unidade, dia da semana, horario de inicio/fim, capacidade especifica opcional e status. `CHECK(end_time > start_time)` impede aulas atravessando meia-noite neste ciclo.
- `class_schedule_staff`: relacionamento N:N entre horarios e StaffProfile, com papel `LEAD` ou `ASSISTANT`, sem permitir staff duplicado no mesmo horario e com no maximo um `LEAD`.

Cycle 05 cria Training Sessions:

- `training_sessions`: ocorrencia real tenant-scoped de treino, com `dojo_class_id`, `class_schedule_id` opcional, `unit_id`, `session_date`, `start_time`, `end_time`, `status`, `capacity`, `notes`, `created_by_user_id` e metadados de cancelamento.
- `training_session_staff`: relacionamento N:N entre sessao e StaffProfile, com papel `LEAD` ou `ASSISTANT`, staff duplicado bloqueado e no maximo um `LEAD`.

`training_sessions` usa `UNIQUE(class_schedule_id, session_date) WHERE class_schedule_id IS NOT NULL` para tornar a geracao idempotente sem bloquear sessoes manuais.

Entidades futuras como check-ins, payments, competitions, CRM e store ainda nao possuem tabelas fisicas.

Cycle 06 cria Attendance & Check-in:

- `attendances`: presenca oficial tenant-scoped de um Student em uma TrainingSession. Guarda source, status `ACTIVE`/`VOIDED`, `checked_in_at`, `recorded_by_user_id`, `check_in_id` opcional e metadados de correcao (`voided_at`, `voided_by_user_id`, `void_reason`). Um indice unico parcial garante apenas uma presenca `ACTIVE` por `(training_session_id, student_id)`.
- `check_in_tokens`: tokens opacos temporarios para QR. O banco persiste apenas `token_hash`, com expiracao, sessao, tenant e criador.
- `check_ins`: tentativas auditaveis de check-in por QR, com status aceito, duplicado, expirado, invalido, sessao fechada, aluno invalido ou usuario sem aluno vinculado.

Attendance `VOIDED` permanece auditavel, mas nao conta em frequencia, resumo do aluno ou elegibilidade de graduacao.
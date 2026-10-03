# Security Baseline

Cycle 01 implementa:

- Spring Security stateless.
- JWT de access token com expiracao curta configuravel por `JWT_ACCESS_TOKEN_TTL`.
- Refresh token opaco, aleatorio, persistido apenas como SHA-256 hash e rotacionado a cada uso.
- Logout com revogacao do refresh token.
- BCrypt para senha.
- RBAC por permissions server-side.
- Tenant context derivado da autenticacao, nunca de `tenantId` vindo do request body.
- Queries de Tenant, Unit e User filtradas pelo tenant autenticado.
- Protecao para nao remover ou desativar o ultimo OWNER ativo do tenant.
- Logs tecnicos de login success, login failure, logout e refresh rotation sem senha, hash ou tokens.
- CORS restrito por `APP_CORS_ALLOWED_ORIGINS`.
- Frontend com BFF leve no Next.js e tokens em cookies HttpOnly.
- Cycle 02 mantem Student, Guardian e Staff tenant-scoped.
- Referencias para Unit e User em Student/Guardian/Staff sao validadas pelo tenant autenticado.
- A UI administrativa esconde acoes sem permission, sem substituir a autorizacao server-side.
- `Student.notes` e administrativo e nao deve ser usado como prontuario medico.
- Cycle 03 mantem graduacoes tenant-scoped. Consultas e comandos carregam Student e StaffProfile pelo tenant autenticado.
- Correcoes de graduacao exigem `GRADUATION_CORRECT`, motivo obrigatorio e preservam o evento original.
- `createdByUser` registra quem lancou a graduacao no sistema; `awardedByStaff` registra professor/instrutor responsavel quando informado.
- Historico de graduacao nao possui endpoint de delecao.
- Elegibilidade e apenas indicacao para avaliacao; nao existe promocao automatica.

- Cycle 04 mantem DojoClass e ClassSchedule tenant-scoped. Schedules validam Unit, DojoClass e StaffProfile pelo tenant autenticado. Staff associado a grade precisa estar ativo e ser professor ou instrutor. A autorizacao usa `CLASS_*` e `SCHEDULE_*`; a UI esconde acoes sem permission, mas o backend continua sendo autoridade. Nao ha DELETE fisico de aulas ou horarios.
- Cycle 05 mantem TrainingSession tenant-scoped. A criacao manual valida DojoClass, Unit e StaffProfile pelo tenant autenticado. A geracao le apenas ClassSchedule ACTIVE do tenant autenticado. `createdByUser` e `cancelledByUser` vem da autenticacao, nunca do request. A state machine e imposta no backend: `SCHEDULED -> OPEN -> COMPLETED`, com cancelamento permitido somente a partir de `SCHEDULED` ou `OPEN`. Nao ha DELETE fisico de sessoes.

Ainda ficam para ciclos futuros:

- rate limiting em endpoints sensiveis;
- audit log persistente;
- prontuario medico;
- protecao de QR/check-in;
- device management detalhado para refresh tokens.

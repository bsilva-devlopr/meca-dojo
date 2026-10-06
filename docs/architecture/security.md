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

## Cycle 06 - Attendance & Check-in Security

- Tenant continua vindo exclusivamente da autenticacao.
- Registro manual valida TrainingSession e Student no tenant autenticado.
- Self check-in aceita apenas token opaco e resolve o Student pelo User autenticado.
- O token plaintext e entregue uma unica vez ao frontend; o banco armazena SHA-256.
- O QR expira por `app.check-in.qr-ttl-seconds` com default de 60 segundos.
- Token valido ainda exige TrainingSession OPEN.
- Duplicidade de Attendance ACTIVE e protegida por indice unico parcial no banco.
- VOID nao apaga presenca; registra motivo, usuario e timestamp.
- `recordedByUserId`, `createdByUserId` e `voidedByUserId` vêm da autenticacao.
- Nao ha DELETE fisico de Attendance ou CheckIn exposto pela API.
## Deployment security

Cycle 06.1 mantem o browser falando com o BFF do Next.js. `API_BASE_URL` e privado do servidor Next.js; secrets nao devem usar prefixo `NEXT_PUBLIC_`.

Cookies de access/refresh token continuam `HttpOnly`, `SameSite=Lax` e `Secure` em build de producao. O backend aceita CORS apenas para origens explicitas em `APP_CORS_ALLOWED_ORIGINS` e falha a inicializacao se `*` for configurado junto com credenciais.

Bootstrap fica desabilitado por padrao (`BOOTSTRAP_ENABLED=false`) fora de execucao local/one-time. Flyway continua habilitado para validate/migrate, com clean desabilitado. O backend aceita `PORT` de cloud runtime e URLs de PostgreSQL gerenciado sem commitar credenciais.

## Student self-service

`/api/v1/student/me/*` requires authentication and resolves tenant/user to the linked Student server-side. No studentId is accepted as the subject. STUDENT receives no administrative STUDENT_READ or ATTENDANCE_READ grants. Self DTOs exclude internal eligibility reasons and administrative graduation notes. INACTIVE/SUSPENDED can read their own data but cannot check in; ACTIVE/TRIAL retain existing QR rules. Multi-role navigation does not change backend permissions. Tokens remain in the existing HttpOnly BFF cookies, never localStorage.

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

Ainda ficam para ciclos futuros:

- rate limiting em endpoints sensiveis;
- audit log persistente;
- prontuario medico;
- protecao de QR/check-in;
- device management detalhado para refresh tokens.

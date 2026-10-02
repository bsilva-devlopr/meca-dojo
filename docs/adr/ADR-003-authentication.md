# ADR-003 - Authentication and Tokens

Status: Accepted.

Decisao: usar Spring Security stateless com access token JWT e refresh token opaco.

O access token JWT usa HS256, expiracao curta configuravel por `JWT_ACCESS_TOKEN_TTL` e contem apenas claims necessarias: `sub`, `tenant`, `roles`, `iat` e `exp`. O backend carrega usuario, tenant, roles e permissions a partir do banco em cada request autenticado.

O refresh token e um valor aleatorio opaco. O cliente recebe o valor plaintext uma unica vez, e o banco persiste apenas `token_hash`, `expires_at`, `revoked_at` e `created_at`. Ao chamar refresh, o token antigo e revogado e um novo refresh token e emitido.

`JWT_SECRET` deve vir do ambiente e ter pelo menos 32 caracteres. Nao ha secret real versionado.

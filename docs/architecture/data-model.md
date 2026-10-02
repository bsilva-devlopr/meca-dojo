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

Entidades futuras como students, professors, guardians, belts, classes, training sessions, check-ins, payments, competitions, CRM e store ainda nao possuem tabelas fisicas.

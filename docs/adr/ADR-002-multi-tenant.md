# ADR-002 - Multi-tenant desde o dominio

Status: Accepted.

Decisao: Tenant representa a organizacao/equipe/academia e Unit representa unidade fisica pertencente a um Tenant. No Cycle 01, User pertence a exatamente um Tenant. Nao ha compartilhamento de usuarios entre tenants.

Todas as entidades de negocio aplicaveis devem carregar `tenant_id` e todas as queries funcionais devem ser filtradas pelo tenant autenticado. O tenant confiavel vem da autenticacao, nao de campos enviados pelo cliente.

No Cycle 01, `roles` e `permissions` sao globais/system-defined para manter simplicidade e consistencia no MVP. `users` e `units` sao tenant-scoped.

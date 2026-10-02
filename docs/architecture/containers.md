# C4 - Containers

- Web/PWA: Next.js, React, TypeScript, App Router e Tailwind CSS.
- API: Spring Boot 3, Java 21 target, Maven, Spring Data JPA, Flyway, Bean Validation, Spring Security, Actuator e OpenAPI.
- Database: PostgreSQL.
- Cache local: Redis disponivel na infraestrutura para ciclos futuros.
- Object storage local: MinIO disponivel na infraestrutura para ciclos futuros.

No Cycle 01, o browser fala com rotas BFF leves do Next.js para autenticacao. O BFF chama a API Spring e guarda access/refresh tokens em cookies HttpOnly. Redis e MinIO sao provisionados pelo Docker Compose, mas ainda nao sao usados pela aplicacao.

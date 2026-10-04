# C4 - Containers

- Web/PWA: Next.js, React, TypeScript, App Router e Tailwind CSS.
- API: Spring Boot 3, Java 21 target, Maven, Spring Data JPA, Flyway, Bean Validation, Spring Security, Actuator e OpenAPI.
- Database: PostgreSQL.
- Cache local: Redis disponivel na infraestrutura para ciclos futuros.
- Object storage local: MinIO disponivel na infraestrutura para ciclos futuros.

No Cycle 01, o browser fala com rotas BFF leves do Next.js para autenticacao. O BFF chama a API Spring e guarda access/refresh tokens em cookies HttpOnly. Redis e MinIO sao provisionados pelo Docker Compose, mas ainda nao sao usados pela aplicacao.

Cycle 06.1 define staging como Vercel para o Web/PWA e um runtime Java/container para a API Spring Boot. O BFF continua sendo o ponto de contato do browser com autenticacao por cookies HttpOnly. A API usa PostgreSQL gerenciado, Flyway e `/actuator/health`; Redis e object storage permanecem opcionais/futuros enquanto nenhuma funcionalidade de runtime depender deles.

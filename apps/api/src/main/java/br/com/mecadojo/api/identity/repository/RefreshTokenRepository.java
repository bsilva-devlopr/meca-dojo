package br.com.mecadojo.api.identity.repository;

import br.com.mecadojo.api.identity.domain.RefreshToken;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    @EntityGraph(attributePaths = {"user", "user.tenant", "user.roles", "user.roles.permissions"})
    Optional<RefreshToken> findByTokenHash(String tokenHash);
}

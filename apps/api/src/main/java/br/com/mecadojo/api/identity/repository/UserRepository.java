package br.com.mecadojo.api.identity.repository;

import br.com.mecadojo.api.identity.domain.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {
    @EntityGraph(attributePaths = {"tenant", "roles", "roles.permissions"})
    Optional<User> findByTenantIdAndEmail(UUID tenantId, String email);

    @EntityGraph(attributePaths = {"tenant", "roles", "roles.permissions"})
    Optional<User> findByIdAndTenantId(UUID id, UUID tenantId);

    @EntityGraph(attributePaths = {"tenant", "roles", "roles.permissions"})
    Optional<User> findWithTenantAndRolesById(UUID id);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    List<User> findAllByTenantIdOrderByNameAsc(UUID tenantId);

    boolean existsByTenantIdAndEmail(UUID tenantId, String email);
}

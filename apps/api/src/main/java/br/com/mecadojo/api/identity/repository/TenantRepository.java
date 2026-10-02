package br.com.mecadojo.api.identity.repository;

import br.com.mecadojo.api.identity.domain.Tenant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    Optional<Tenant> findBySlug(String slug);

    boolean existsBySlug(String slug);
}

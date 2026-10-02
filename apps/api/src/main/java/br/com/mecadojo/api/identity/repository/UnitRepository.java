package br.com.mecadojo.api.identity.repository;

import br.com.mecadojo.api.identity.domain.Unit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnitRepository extends JpaRepository<Unit, UUID> {
    List<Unit> findAllByTenantIdOrderByNameAsc(UUID tenantId);

    Optional<Unit> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByTenantIdAndSlug(UUID tenantId, String slug);
}

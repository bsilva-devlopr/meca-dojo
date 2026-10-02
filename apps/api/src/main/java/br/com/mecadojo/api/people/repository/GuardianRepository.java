package br.com.mecadojo.api.people.repository;

import br.com.mecadojo.api.people.domain.Guardian;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GuardianRepository extends JpaRepository<Guardian, UUID>, JpaSpecificationExecutor<Guardian> {
    @EntityGraph(attributePaths = "user")
    Optional<Guardian> findByIdAndTenantId(UUID id, UUID tenantId);
}

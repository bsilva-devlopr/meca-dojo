package br.com.mecadojo.api.people.repository;

import br.com.mecadojo.api.people.domain.StaffProfile;
import br.com.mecadojo.api.people.domain.StaffType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StaffProfileRepository extends JpaRepository<StaffProfile, UUID>, JpaSpecificationExecutor<StaffProfile> {
    @EntityGraph(attributePaths = {"user", "primaryUnit"})
    Optional<StaffProfile> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByTenantIdAndUserIdAndType(UUID tenantId, UUID userId, StaffType type);
}

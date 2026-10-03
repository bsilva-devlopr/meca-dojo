package br.com.mecadojo.api.schedule.repository;

import br.com.mecadojo.api.schedule.domain.DojoClass;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DojoClassRepository extends JpaRepository<DojoClass, UUID>, JpaSpecificationExecutor<DojoClass> {
    Optional<DojoClass> findByIdAndTenantId(UUID id, UUID tenantId);
}

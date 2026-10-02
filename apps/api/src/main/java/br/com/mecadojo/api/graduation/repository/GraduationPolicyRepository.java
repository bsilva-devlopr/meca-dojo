package br.com.mecadojo.api.graduation.repository;

import br.com.mecadojo.api.graduation.domain.GraduationPolicy;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GraduationPolicyRepository extends JpaRepository<GraduationPolicy, UUID> {
    @EntityGraph(attributePaths = {"belt"})
    List<GraduationPolicy> findAllByTenantIdOrderByBeltSystemAscBeltSequenceAsc(UUID tenantId);

    @EntityGraph(attributePaths = {"belt"})
    Optional<GraduationPolicy> findByTenantIdAndBeltId(UUID tenantId, UUID beltId);
}

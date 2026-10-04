package br.com.mecadojo.api.people.repository;

import br.com.mecadojo.api.people.domain.Student;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StudentRepository extends JpaRepository<Student, UUID>, JpaSpecificationExecutor<Student> {
    @EntityGraph(attributePaths = {"primaryUnit", "user"})
    Optional<Student> findByIdAndTenantId(UUID id, UUID tenantId);

    @EntityGraph(attributePaths = {"primaryUnit", "user"})
    Optional<Student> findByTenantIdAndUserId(UUID tenantId, UUID userId);
}

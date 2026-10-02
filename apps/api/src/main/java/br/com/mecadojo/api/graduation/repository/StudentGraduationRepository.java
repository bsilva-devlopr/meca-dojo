package br.com.mecadojo.api.graduation.repository;

import br.com.mecadojo.api.graduation.domain.StudentGraduation;
import br.com.mecadojo.api.graduation.domain.GraduationType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

public interface StudentGraduationRepository extends JpaRepository<StudentGraduation, UUID> {
    boolean existsByTenantIdAndStudentIdAndType(UUID tenantId, UUID studentId, GraduationType type);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"belt", "awardedByStaff", "createdByUser", "correctsGraduation"})
    List<StudentGraduation> findByTenantIdAndStudentIdOrderByCreatedAtDesc(UUID tenantId, UUID studentId);

    @EntityGraph(attributePaths = {"belt", "awardedByStaff", "createdByUser", "correctsGraduation"})
    Page<StudentGraduation> findByTenantIdAndStudentId(UUID tenantId, UUID studentId, Pageable pageable);

    @EntityGraph(attributePaths = {"belt", "awardedByStaff", "createdByUser", "correctsGraduation"})
    Optional<StudentGraduation> findFirstByTenantIdAndStudentIdOrderByCreatedAtDesc(UUID tenantId, UUID studentId);

    @EntityGraph(attributePaths = {"belt", "awardedByStaff", "createdByUser", "correctsGraduation"})
    Optional<StudentGraduation> findByIdAndTenantIdAndStudentId(UUID id, UUID tenantId, UUID studentId);
}

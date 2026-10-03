package br.com.mecadojo.api.training.repository;

import br.com.mecadojo.api.training.domain.TrainingSession;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TrainingSessionRepository extends JpaRepository<TrainingSession, UUID>, JpaSpecificationExecutor<TrainingSession> {
    @EntityGraph(attributePaths = {"dojoClass", "classSchedule", "unit", "createdByUser", "cancelledByUser", "staff", "staff.staffProfile"})
    Optional<TrainingSession> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByTenantIdAndClassScheduleIdAndSessionDate(UUID tenantId, UUID classScheduleId, LocalDate sessionDate);
}

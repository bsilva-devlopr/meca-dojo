package br.com.mecadojo.api.training.repository;

import br.com.mecadojo.api.training.domain.TrainingSession;
import br.com.mecadojo.api.training.domain.TrainingSessionStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrainingSessionRepository extends JpaRepository<TrainingSession, UUID>, JpaSpecificationExecutor<TrainingSession> {
    @EntityGraph(attributePaths = {"dojoClass", "classSchedule", "unit", "createdByUser", "cancelledByUser", "staff", "staff.staffProfile"})
    Optional<TrainingSession> findByIdAndTenantId(UUID id, UUID tenantId);

    @EntityGraph(attributePaths = {"staff", "staff.staffProfile"})
    List<TrainingSession> findAllByTenantIdAndIdIn(UUID tenantId, Collection<UUID> ids);

    boolean existsByTenantIdAndClassScheduleIdAndSessionDate(UUID tenantId, UUID classScheduleId, LocalDate sessionDate);

    @EntityGraph(attributePaths = {"dojoClass", "unit"})
    @Query("""
        select s from TrainingSession s
        where s.tenant.id = :tenantId
          and s.unit.id = :unitId
          and s.status in :statuses
          and (s.sessionDate > :today or (s.sessionDate = :today and s.endTime >= :nowTime))
        order by s.sessionDate asc, s.startTime asc
        """)
    List<TrainingSession> findUpcomingForUnit(
        @Param("tenantId") UUID tenantId,
        @Param("unitId") UUID unitId,
        @Param("today") LocalDate today,
        @Param("nowTime") LocalTime nowTime,
        @Param("statuses") Collection<TrainingSessionStatus> statuses,
        Pageable pageable
    );
}

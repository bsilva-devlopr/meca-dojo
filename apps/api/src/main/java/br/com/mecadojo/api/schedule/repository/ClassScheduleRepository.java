package br.com.mecadojo.api.schedule.repository;

import br.com.mecadojo.api.schedule.domain.ClassSchedule;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ClassScheduleRepository extends JpaRepository<ClassSchedule, UUID>, JpaSpecificationExecutor<ClassSchedule> {
    @EntityGraph(attributePaths = {"dojoClass", "unit", "staff", "staff.staffProfile"})
    Optional<ClassSchedule> findByIdAndTenantId(UUID id, UUID tenantId);

    @EntityGraph(attributePaths = {"dojoClass", "unit", "staff", "staff.staffProfile"})
    List<ClassSchedule> findByTenantIdAndStatusOrderByDayOfWeekAscStartTimeAsc(UUID tenantId, br.com.mecadojo.api.schedule.domain.ClassScheduleStatus status);
}

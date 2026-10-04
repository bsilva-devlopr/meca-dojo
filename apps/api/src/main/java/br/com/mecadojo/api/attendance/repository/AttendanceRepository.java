package br.com.mecadojo.api.attendance.repository;

import br.com.mecadojo.api.attendance.domain.Attendance;
import br.com.mecadojo.api.attendance.domain.AttendanceStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {
    @EntityGraph(attributePaths = {"trainingSession", "trainingSession.dojoClass", "trainingSession.unit", "student", "recordedByUser", "voidedByUser", "checkIn"})
    Optional<Attendance> findByIdAndTenantId(UUID id, UUID tenantId);

    @EntityGraph(attributePaths = {"student", "recordedByUser", "voidedByUser", "checkIn"})
    List<Attendance> findAllByTenantIdAndTrainingSessionIdAndStatusOrderByCheckedInAtAsc(UUID tenantId, UUID trainingSessionId, AttendanceStatus status);

    @EntityGraph(attributePaths = {"student", "recordedByUser", "voidedByUser", "checkIn"})
    List<Attendance> findAllByTenantIdAndTrainingSessionIdOrderByCheckedInAtAsc(UUID tenantId, UUID trainingSessionId);

    @EntityGraph(attributePaths = {"trainingSession", "trainingSession.dojoClass", "trainingSession.unit", "recordedByUser", "checkIn"})
    List<Attendance> findAllByTenantIdAndStudentIdAndStatusOrderByCheckedInAtDesc(UUID tenantId, UUID studentId, AttendanceStatus status);

    Optional<Attendance> findByTenantIdAndTrainingSessionIdAndStudentIdAndStatus(UUID tenantId, UUID trainingSessionId, UUID studentId, AttendanceStatus status);

    long countByTenantIdAndStudentIdAndStatus(UUID tenantId, UUID studentId, AttendanceStatus status);

    long countByTenantIdAndStudentIdAndStatusAndCheckedInAtGreaterThanEqual(UUID tenantId, UUID studentId, AttendanceStatus status, Instant from);

    @Query("""
        select count(a) from Attendance a
        where a.tenant.id = :tenantId
          and a.student.id = :studentId
          and a.status = br.com.mecadojo.api.attendance.domain.AttendanceStatus.ACTIVE
          and a.checkedInAt >= :from
          and a.checkedInAt < :to
        """)
    long countActiveBetween(@Param("tenantId") UUID tenantId, @Param("studentId") UUID studentId, @Param("from") Instant from, @Param("to") Instant to);

    @Query("""
        select count(a) from Attendance a
        where a.tenant.id = :tenantId
          and a.student.id = :studentId
          and a.status = br.com.mecadojo.api.attendance.domain.AttendanceStatus.ACTIVE
          and a.trainingSession.dojoClass.modality = :modality
        """)
    long countActiveByModality(@Param("tenantId") UUID tenantId, @Param("studentId") UUID studentId, @Param("modality") br.com.mecadojo.api.schedule.domain.ClassModality modality);
}

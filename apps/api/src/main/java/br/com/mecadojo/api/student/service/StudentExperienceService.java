package br.com.mecadojo.api.student.service;

import br.com.mecadojo.api.attendance.domain.Attendance;
import br.com.mecadojo.api.attendance.repository.AttendanceRepository;
import br.com.mecadojo.api.graduation.domain.Belt;
import br.com.mecadojo.api.graduation.domain.StudentGraduation;
import br.com.mecadojo.api.graduation.repository.StudentGraduationRepository;
import br.com.mecadojo.api.people.api.dto.PageResponse;
import br.com.mecadojo.api.people.domain.Student;
import br.com.mecadojo.api.people.domain.StudentEmergencyContact;
import br.com.mecadojo.api.people.repository.StudentEmergencyContactRepository;
import br.com.mecadojo.api.people.repository.StudentRepository;
import br.com.mecadojo.api.schedule.domain.ClassModality;
import br.com.mecadojo.api.shared.exception.NotFoundException;
import br.com.mecadojo.api.student.api.dto.StudentExperienceDtos.*;
import br.com.mecadojo.api.training.domain.TrainingSession;
import br.com.mecadojo.api.training.domain.TrainingSessionStaffRole;
import br.com.mecadojo.api.training.domain.TrainingSessionStatus;
import br.com.mecadojo.api.training.repository.TrainingSessionRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentExperienceService {
    private final StudentRepository studentRepository;
    private final StudentEmergencyContactRepository emergencyContactRepository;
    private final StudentGraduationRepository graduationRepository;
    private final br.com.mecadojo.api.graduation.repository.GraduationPolicyRepository policyRepository;
    private final ZoneId operationalZone;
    private final AttendanceRepository attendanceRepository;
    private final TrainingSessionRepository trainingSessionRepository;

    public StudentExperienceService(
        StudentRepository studentRepository,
        StudentEmergencyContactRepository emergencyContactRepository,
        StudentGraduationRepository graduationRepository,
        br.com.mecadojo.api.graduation.repository.GraduationPolicyRepository policyRepository,
        @org.springframework.beans.factory.annotation.Value("${app.operational-zone:America/Sao_Paulo}") String operationalZone,
        AttendanceRepository attendanceRepository,
        TrainingSessionRepository trainingSessionRepository
    ) {
        this.studentRepository = studentRepository;
        this.emergencyContactRepository = emergencyContactRepository;
        this.graduationRepository = graduationRepository;
        this.policyRepository = policyRepository;
        this.operationalZone = ZoneId.of(operationalZone);
        this.attendanceRepository = attendanceRepository;
        this.trainingSessionRepository = trainingSessionRepository;
    }

    @Transactional(readOnly = true)
    public StudentDashboardResponse dashboard() {
        Student student = currentStudent();
        List<SessionCard> upcoming = (student.getStatus() == br.com.mecadojo.api.people.domain.StudentStatus.ACTIVE || student.getStatus() == br.com.mecadojo.api.people.domain.StudentStatus.TRIAL) ? upcomingSessions(student, 3) : List.of();
        return new StudentDashboardResponse(
            profile(student),
            currentBelt(student).orElse(null),
            frequency(student),
            upcoming.stream().filter(session -> session.status() == TrainingSessionStatus.OPEN).findFirst().orElse(null),
            upcoming,
            trainingHistoryItems(student, 0, 5),
            graduationItems(student, 0, 4)
        );
    }

    @Transactional(readOnly = true)
    public StudentProfileResponse profile() {
        return profile(currentStudent());
    }

    @Transactional(readOnly = true)
    public PageResponse<TrainingHistoryItem> trainingHistory(int page, int size) {
        Student student = currentStudent();
        int safeSize = Math.min(Math.max(size, 1), 50);
        List<TrainingHistoryItem> items = trainingHistoryItems(student, Math.max(page, 0), safeSize);
        long total = attendanceRepository.countActiveBySessionStatus(student.getTenant().getId(), student.getId(), TrainingSessionStatus.COMPLETED);
        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / safeSize);
        return new PageResponse<>(items, Math.max(page, 0), safeSize, total, totalPages);
    }

    @Transactional(readOnly = true)
    public GraduationResponse graduation() {
        Student student = currentStudent();
        return new GraduationResponse(currentBelt(student).orElse(null), graduationItems(student, 0, 50));
    }

    private Student currentStudent() {
        var current = br.com.mecadojo.api.identity.security.CurrentUser.get();
        return studentRepository.findByTenantIdAndUserId(current.tenantId(), current.userId())
            .orElseThrow(() -> new NotFoundException("Authenticated user is not linked to a student"));
    }

    private StudentProfileResponse profile(Student student) {
        EmergencyContactSummary emergency = emergencyContactRepository.findByStudentId(student.getId())
            .map(this::emergency)
            .orElse(null);
        String displayName = student.getPreferredName() == null || student.getPreferredName().isBlank() ? student.getFullName() : student.getPreferredName();
        return new StudentProfileResponse(
            displayName,
            student.getFullName(),
            student.getPreferredName(),
            student.getEmail(),
            student.getPhone(),
            student.getStatus(),
            student.getJoinDate(),
            student.getPrimaryUnit() == null ? null : new UnitSummary(student.getPrimaryUnit().getName()),
            emergency
        );
    }

    private EmergencyContactSummary emergency(StudentEmergencyContact contact) {
        return new EmergencyContactSummary(contact.getName(), contact.getRelationship(), contact.getPhone());
    }

    private Optional<CurrentBeltResponse> currentBelt(Student student) {
        return graduationRepository.findFirstByTenantIdAndStudentIdOrderByCreatedAtDesc(student.getTenant().getId(), student.getId())
            .map(graduation -> new CurrentBeltResponse(
                belt(graduation.getBelt()),
                graduation.getDegree(),
                graduation.getGraduatedAt(),
                staff(graduation.getAwardedByStaff() == null ? null : graduation.getAwardedByStaff().getDisplayName()),
                graduation.getType().name(),
                criteria(student, graduation)
            ));
    }

    private List<GraduationHistoryItem> graduationItems(Student student, int page, int size) {
        return graduationRepository.findByTenantIdAndStudentId(
            student.getTenant().getId(),
            student.getId(),
            PageRequest.of(page, size, Sort.by("createdAt").descending())
        ).stream().map(this::graduationItem).toList();
    }

    private GraduationHistoryItem graduationItem(StudentGraduation graduation) {
        return new GraduationHistoryItem(
            belt(graduation.getBelt()),
            graduation.getDegree(),
            graduation.getGraduatedAt(),
            staff(graduation.getAwardedByStaff() == null ? null : graduation.getAwardedByStaff().getDisplayName()),
            graduation.getType().name()
        );
    }

    private CriteriaResponse criteria(Student student, StudentGraduation graduation) {
        return policyRepository.findByTenantIdAndBeltId(student.getTenant().getId(), graduation.getBelt().getId())
            .filter(br.com.mecadojo.api.graduation.domain.GraduationPolicy::isActive)
            .map(policy -> new CriteriaResponse(
                attendanceRepository.countActiveBySessionStatus(student.getTenant().getId(), student.getId(), TrainingSessionStatus.COMPLETED),
                policy.getMinimumTrainingCount(),
                graduation.getGraduatedAt() == null ? null : java.time.temporal.ChronoUnit.DAYS.between(graduation.getGraduatedAt(), LocalDate.now(operationalZone)),
                policy.getMinimumDaysInGrade()))
            .orElse(null);
    }

    private FrequencySummary frequency(Student student) {
        UUID tenantId = student.getTenant().getId();
        UUID studentId = student.getId();
        LocalDate today = LocalDate.now(operationalZone);
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate nextMonth = monthStart.plusMonths(1);
        return new FrequencySummary(
            attendanceRepository.countActiveBySessionStatus(tenantId, studentId, TrainingSessionStatus.COMPLETED),
            attendanceRepository.countOfficialBetween(tenantId, studentId, today.minusDays(29), today.plusDays(1)),
            attendanceRepository.countOfficialBetween(tenantId, studentId, today.minusDays(89), today.plusDays(1)),
            attendanceRepository.countOfficialBetween(tenantId, studentId, monthStart, nextMonth),
            attendanceRepository.countOfficialByModality(tenantId, studentId, ClassModality.GI),
            attendanceRepository.countOfficialByModality(tenantId, studentId, ClassModality.NO_GI)
        );
    }

    private List<SessionCard> upcomingSessions(Student student, int limit) {
        if (student.getPrimaryUnit() == null) return List.of();
        List<TrainingSession> sessions = trainingSessionRepository.findUpcomingForUnit(
            student.getTenant().getId(),
            student.getPrimaryUnit().getId(),
            LocalDate.now(operationalZone),
            LocalTime.now(operationalZone),
            List.of(TrainingSessionStatus.OPEN, TrainingSessionStatus.SCHEDULED),
            PageRequest.of(0, limit)
        );
        if (!sessions.isEmpty()) trainingSessionRepository.findAllByTenantIdAndIdIn(student.getTenant().getId(), sessions.stream().map(TrainingSession::getId).toList());
        return sessions.stream().map(session -> sessionCard(session, hasActiveAttendance(session.getTenant().getId(), session.getId(), student.getId()))).toList();
    }

    private List<TrainingHistoryItem> trainingHistoryItems(Student student, int page, int size) {
        List<Attendance> attendances = attendanceRepository.findOfficialHistoryList(
            student.getTenant().getId(),
            student.getId(),
            PageRequest.of(page, size)
        );
        if (!attendances.isEmpty()) trainingSessionRepository.findAllByTenantIdAndIdIn(student.getTenant().getId(), attendances.stream().map(a -> a.getTrainingSession().getId()).toList());
        return attendances.stream().map(attendance -> new TrainingHistoryItem(
            attendance.getId(),
            sessionCard(attendance.getTrainingSession(), true),
            attendance.getSource().name(),
            attendance.getCheckedInAt()
        )).toList();
    }

    private boolean hasActiveAttendance(UUID tenantId, UUID sessionId, UUID studentId) {
        return attendanceRepository.findByTenantIdAndTrainingSessionIdAndStudentIdAndStatus(
            tenantId,
            sessionId,
            studentId,
            br.com.mecadojo.api.attendance.domain.AttendanceStatus.ACTIVE
        ).isPresent();
    }

    private SessionCard sessionCard(TrainingSession session, boolean attendanceRegistered) {
        List<StaffSummary> staff = session.getStaff().stream()
            .sorted((a, b) -> {
                if (a.getRole() == b.getRole()) return a.getStaffProfile().getDisplayName().compareToIgnoreCase(b.getStaffProfile().getDisplayName());
                return a.getRole() == TrainingSessionStaffRole.LEAD ? -1 : 1;
            })
            .map(assignment -> staff(assignment.getStaffProfile().getDisplayName()))
            .toList();
        return new SessionCard(
            session.getId(),
            session.getDojoClass().getName(),
            session.getDojoClass().getModality(),
            session.getUnit().getName(),
            session.getSessionDate(),
            session.getStartTime(),
            session.getEndTime(),
            session.getStatus(),
            staff,
            attendanceRegistered
        );
    }

    private BeltSummary belt(Belt belt) {
        return new BeltSummary(belt.getCode(), belt.getDisplayName(), belt.getSystem(), belt.getSequence(), belt.getMaxDegree());
    }

    private StaffSummary staff(String name) {
        return name == null ? null : new StaffSummary(name);
    }
}

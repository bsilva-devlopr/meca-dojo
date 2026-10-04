package br.com.mecadojo.api.attendance.service;

import br.com.mecadojo.api.attendance.api.dto.AttendanceDtos.*;
import br.com.mecadojo.api.attendance.domain.*;
import br.com.mecadojo.api.attendance.repository.AttendanceRepository;
import br.com.mecadojo.api.attendance.repository.CheckInRepository;
import br.com.mecadojo.api.attendance.repository.CheckInTokenRepository;
import br.com.mecadojo.api.identity.domain.Tenant;
import br.com.mecadojo.api.identity.domain.User;
import br.com.mecadojo.api.identity.security.CurrentUser;
import br.com.mecadojo.api.people.domain.Student;
import br.com.mecadojo.api.people.domain.StudentStatus;
import br.com.mecadojo.api.people.repository.StudentRepository;
import br.com.mecadojo.api.people.service.PeopleMapper;
import br.com.mecadojo.api.people.service.PeopleReferenceService;
import br.com.mecadojo.api.schedule.domain.ClassModality;
import br.com.mecadojo.api.shared.exception.BadRequestException;
import br.com.mecadojo.api.shared.exception.NotFoundException;
import br.com.mecadojo.api.training.domain.TrainingSession;
import br.com.mecadojo.api.training.domain.TrainingSessionStatus;
import br.com.mecadojo.api.training.repository.TrainingSessionRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final CheckInRepository checkInRepository;
    private final TrainingSessionRepository trainingSessionRepository;
    private final StudentRepository studentRepository;
    private final PeopleReferenceService references;

    public AttendanceService(
        AttendanceRepository attendanceRepository,
        CheckInRepository checkInRepository,
        TrainingSessionRepository trainingSessionRepository,
        StudentRepository studentRepository,
        PeopleReferenceService references
    ) {
        this.attendanceRepository = attendanceRepository;
        this.checkInRepository = checkInRepository;
        this.trainingSessionRepository = trainingSessionRepository;
        this.studentRepository = studentRepository;
        this.references = references;
    }

    @Transactional
    public AttendanceResponse registerManual(UUID sessionId, ManualAttendanceRequest request) {
        TrainingSession session = loadSession(sessionId);
        ensureOpen(session);
        Student student = loadValidStudent(request.studentId());
        User user = references.user(CurrentUser.get().userId());
        Attendance attendance = createAttendance(session, student, AttendanceSource.MANUAL, user, null);
        return toResponse(attendance);
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponse> listForSession(UUID sessionId, boolean includeVoided) {
        loadSession(sessionId);
        List<Attendance> attendances = includeVoided
            ? attendanceRepository.findAllByTenantIdAndTrainingSessionIdOrderByCheckedInAtAsc(CurrentUser.get().tenantId(), sessionId)
            : attendanceRepository.findAllByTenantIdAndTrainingSessionIdAndStatusOrderByCheckedInAtAsc(CurrentUser.get().tenantId(), sessionId, AttendanceStatus.ACTIVE);
        return attendances.stream().map(this::toResponse).toList();
    }

    @Transactional
    public AttendanceResponse voidAttendance(UUID attendanceId, VoidAttendanceRequest request) {
        if (request.reason() == null || request.reason().isBlank()) {
            throw new BadRequestException("Void reason is required");
        }
        Attendance attendance = attendanceRepository.findByIdAndTenantId(attendanceId, CurrentUser.get().tenantId())
            .orElseThrow(() -> new NotFoundException("Attendance not found"));
        if (attendance.getStatus() != AttendanceStatus.ACTIVE) {
            throw new BadRequestException("Only active attendance can be corrected");
        }
        attendance.voidAttendance(references.user(CurrentUser.get().userId()), request.reason().trim());
        return toResponse(attendanceRepository.save(attendance));
    }

    @Transactional(readOnly = true)
    public List<StudentAttendanceHistoryItem> studentHistory(UUID studentId) {
        loadStudentSameTenant(studentId);
        return attendanceRepository.findAllByTenantIdAndStudentIdAndStatusOrderByCheckedInAtDesc(CurrentUser.get().tenantId(), studentId, AttendanceStatus.ACTIVE)
            .stream()
            .map(attendance -> new StudentAttendanceHistoryItem(attendance.getId(), sessionSummary(attendance.getTrainingSession()), attendance.getSource(), attendance.getCheckedInAt()))
            .toList();
    }

    @Transactional(readOnly = true)
    public StudentAttendanceSummary studentSummary(UUID studentId) {
        loadStudentSameTenant(studentId);
        UUID tenantId = CurrentUser.get().tenantId();
        Instant now = Instant.now();
        LocalDate firstOfMonth = LocalDate.now().withDayOfMonth(1);
        Instant monthStart = firstOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant nextMonth = firstOfMonth.plusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        return new StudentAttendanceSummary(
            attendanceRepository.countByTenantIdAndStudentIdAndStatus(tenantId, studentId, AttendanceStatus.ACTIVE),
            attendanceRepository.countByTenantIdAndStudentIdAndStatusAndCheckedInAtGreaterThanEqual(tenantId, studentId, AttendanceStatus.ACTIVE, now.minus(java.time.Duration.ofDays(30))),
            attendanceRepository.countByTenantIdAndStudentIdAndStatusAndCheckedInAtGreaterThanEqual(tenantId, studentId, AttendanceStatus.ACTIVE, now.minus(java.time.Duration.ofDays(90))),
            attendanceRepository.countActiveByModality(tenantId, studentId, ClassModality.GI),
            attendanceRepository.countActiveByModality(tenantId, studentId, ClassModality.NO_GI),
            attendanceRepository.countActiveBetween(tenantId, studentId, monthStart, nextMonth)
        );
    }

    Attendance createAttendance(TrainingSession session, Student student, AttendanceSource source, User recordedBy, CheckIn checkIn) {
        return attendanceRepository.findByTenantIdAndTrainingSessionIdAndStudentIdAndStatus(CurrentUser.get().tenantId(), session.getId(), student.getId(), AttendanceStatus.ACTIVE)
            .orElseGet(() -> saveNewAttendance(session, student, source, recordedBy, checkIn));
    }

    private Attendance saveNewAttendance(TrainingSession session, Student student, AttendanceSource source, User recordedBy, CheckIn checkIn) {
        try {
            return attendanceRepository.saveAndFlush(new Attendance(references.currentTenant(), session, student, source, Instant.now(), recordedBy, checkIn));
        } catch (DataIntegrityViolationException exception) {
            return attendanceRepository.findByTenantIdAndTrainingSessionIdAndStudentIdAndStatus(CurrentUser.get().tenantId(), session.getId(), student.getId(), AttendanceStatus.ACTIVE)
                .orElseThrow(() -> exception);
        }
    }

    TrainingSession loadSession(UUID sessionId) {
        return trainingSessionRepository.findByIdAndTenantId(sessionId, CurrentUser.get().tenantId())
            .orElseThrow(() -> new NotFoundException("Training session not found"));
    }

    void ensureOpen(TrainingSession session) {
        if (session.getStatus() != TrainingSessionStatus.OPEN) {
            throw new BadRequestException("Training session must be open to register attendance");
        }
    }

    Student loadValidStudent(UUID studentId) {
        Student student = loadStudentSameTenant(studentId);
        if (student.getStatus() != StudentStatus.ACTIVE && student.getStatus() != StudentStatus.TRIAL) {
            throw new BadRequestException("Student must be active or trial to register attendance");
        }
        return student;
    }

    Student loadStudentSameTenant(UUID studentId) {
        return studentRepository.findByIdAndTenantId(studentId, CurrentUser.get().tenantId())
            .orElseThrow(() -> new NotFoundException("Student not found"));
    }

    AttendanceResponse toResponse(Attendance attendance) {
        return new AttendanceResponse(
            attendance.getId(),
            attendance.getTrainingSession().getId(),
            studentSummary(attendance.getStudent()),
            attendance.getSource(),
            attendance.getStatus(),
            attendance.getCheckedInAt(),
            user(attendance.getRecordedByUser()),
            attendance.getCheckIn() == null ? null : attendance.getCheckIn().getId(),
            attendance.getVoidedAt(),
            user(attendance.getVoidedByUser()),
            attendance.getVoidReason()
        );
    }

    TrainingSessionSummary sessionSummary(TrainingSession session) {
        return new TrainingSessionSummary(
            session.getId(),
            new ClassSummary(session.getDojoClass().getId(), session.getDojoClass().getName(), session.getDojoClass().getModality()),
            PeopleMapper.unit(session.getUnit()),
            session.getSessionDate(),
            session.getStartTime(),
            session.getEndTime(),
            session.getStatus().name()
        );
    }

    StudentSummary studentSummary(Student student) {
        return new StudentSummary(student.getId(), student.getFullName(), student.getPreferredName(), student.getStatus().name());
    }

    UserSummary user(User user) {
        return user == null ? null : new UserSummary(user.getId(), user.getName(), user.getEmail());
    }
}

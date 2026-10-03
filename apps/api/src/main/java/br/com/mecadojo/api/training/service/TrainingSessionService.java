package br.com.mecadojo.api.training.service;

import br.com.mecadojo.api.identity.domain.Unit;
import br.com.mecadojo.api.identity.domain.User;
import br.com.mecadojo.api.identity.security.CurrentUser;
import br.com.mecadojo.api.people.api.dto.PageResponse;
import br.com.mecadojo.api.people.domain.StaffProfile;
import br.com.mecadojo.api.people.domain.StaffStatus;
import br.com.mecadojo.api.people.domain.StaffType;
import br.com.mecadojo.api.people.repository.StaffProfileRepository;
import br.com.mecadojo.api.people.service.PeopleMapper;
import br.com.mecadojo.api.people.service.PeopleReferenceService;
import br.com.mecadojo.api.schedule.domain.ClassSchedule;
import br.com.mecadojo.api.schedule.domain.ClassScheduleStatus;
import br.com.mecadojo.api.schedule.domain.DojoClass;
import br.com.mecadojo.api.schedule.repository.ClassScheduleRepository;
import br.com.mecadojo.api.schedule.service.DojoClassService;
import br.com.mecadojo.api.schedule.api.dto.ScheduleDtos.ClassResponse;
import br.com.mecadojo.api.shared.exception.BadRequestException;
import br.com.mecadojo.api.shared.exception.NotFoundException;
import br.com.mecadojo.api.training.api.dto.TrainingSessionDtos.*;
import br.com.mecadojo.api.training.domain.*;
import br.com.mecadojo.api.training.repository.TrainingSessionRepository;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrainingSessionService {
    private static final long MAX_GENERATION_DAYS = 31;

    private final TrainingSessionRepository repository;
    private final ClassScheduleRepository scheduleRepository;
    private final StaffProfileRepository staffRepository;
    private final DojoClassService dojoClassService;
    private final PeopleReferenceService references;

    public TrainingSessionService(TrainingSessionRepository repository, ClassScheduleRepository scheduleRepository, StaffProfileRepository staffRepository, DojoClassService dojoClassService, PeopleReferenceService references) {
        this.repository = repository;
        this.scheduleRepository = scheduleRepository;
        this.staffRepository = staffRepository;
        this.dojoClassService = dojoClassService;
        this.references = references;
    }

    @Transactional(readOnly = true)
    public PageResponse<TrainingSessionResponse> list(LocalDate from, LocalDate to, UUID unitId, UUID classId, UUID staffId, TrainingSessionStatus status, int page, int size) {
        if (unitId != null) references.unit(unitId);
        if (classId != null) dojoClassService.load(classId);
        if (staffId != null) loadValidStaff(staffId);
        return PeopleMapper.page(repository.findAll(spec(from, to, unitId, classId, staffId, status), PageRequest.of(page, Math.min(size, 100), Sort.by("sessionDate").ascending().and(Sort.by("startTime").ascending()))).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public TrainingSessionResponse get(UUID id) { return toResponse(load(id)); }

    @Transactional
    public TrainingSessionResponse create(CreateTrainingSessionRequest request) {
        validateTimes(request.startTime(), request.endTime());
        DojoClass dojoClass = dojoClassService.load(request.dojoClassId());
        Unit unit = references.unit(request.unitId());
        TrainingSession session = new TrainingSession(references.currentTenant(), dojoClass, null, unit, request.sessionDate(), request.startTime(), request.endTime(), references.user(CurrentUser.get().userId()));
        session.setCapacity(request.capacity());
        session.setNotes(blankToNull(request.notes()));
        session.replaceStaff(assignments(session, request.staff()));
        return toResponse(repository.save(session));
    }

    @Transactional
    public GenerateTrainingSessionsResponse generate(GenerateTrainingSessionsRequest request) {
        if (request.to().isBefore(request.from())) throw new BadRequestException("Generation range is invalid");
        long days = ChronoUnit.DAYS.between(request.from(), request.to()) + 1;
        if (days > MAX_GENERATION_DAYS) throw new BadRequestException("Generation range cannot exceed 31 days");
        if (request.unitId() != null) references.unit(request.unitId());

        List<ClassSchedule> schedules = scheduleRepository.findAll(activeScheduleSpec(request.unitId()), Sort.by("dayOfWeek").ascending().and(Sort.by("startTime").ascending()));
        int created = 0;
        int existing = 0;
        User createdBy = references.user(CurrentUser.get().userId());
        for (ClassSchedule schedule : schedules) {
            for (LocalDate date = request.from(); !date.isAfter(request.to()); date = date.plusDays(1)) {
                if (date.getDayOfWeek() != schedule.getDayOfWeek()) continue;
                if (repository.existsByTenantIdAndClassScheduleIdAndSessionDate(CurrentUser.get().tenantId(), schedule.getId(), date)) {
                    existing++;
                    continue;
                }
                TrainingSession session = new TrainingSession(references.currentTenant(), schedule.getDojoClass(), schedule, schedule.getUnit(), date, schedule.getStartTime(), schedule.getEndTime(), createdBy);
                session.setCapacity(schedule.effectiveCapacity());
                session.replaceStaff(schedule.getStaff().stream()
                    .map(item -> new TrainingSessionStaff(session, item.getStaffProfile(), TrainingSessionStaffRole.valueOf(item.getRole().name())))
                    .collect(Collectors.toSet()));
                repository.save(session);
                created++;
            }
        }
        return new GenerateTrainingSessionsResponse(created, existing);
    }

    @Transactional
    public TrainingSessionResponse update(UUID id, UpdateTrainingSessionRequest request) {
        TrainingSession session = load(id);
        if (session.getStatus() != TrainingSessionStatus.SCHEDULED) {
            throw new BadRequestException("Only scheduled sessions can be edited");
        }
        if (request.unitId() != null) session.setUnit(references.unit(request.unitId()));
        LocalTime start = request.startTime() != null ? request.startTime() : session.getStartTime();
        LocalTime end = request.endTime() != null ? request.endTime() : session.getEndTime();
        validateTimes(start, end);
        session.setStartTime(start);
        session.setEndTime(end);
        if (request.sessionDate() != null) session.setSessionDate(request.sessionDate());
        if (request.capacity() != null) session.setCapacity(request.capacity());
        if (request.notes() != null) session.setNotes(blankToNull(request.notes()));
        if (request.staff() != null) replaceStaff(session, assignments(session, request.staff()));
        return toResponse(repository.save(session));
    }

    private void replaceStaff(TrainingSession session, Set<TrainingSessionStaff> assignments) {
        session.replaceStaff(Set.of());
        repository.flush();
        session.replaceStaff(assignments);
    }

    @Transactional
    public TrainingSessionResponse open(UUID id) {
        TrainingSession session = load(id);
        if (session.getStatus() != TrainingSessionStatus.SCHEDULED) throw new BadRequestException("Only scheduled sessions can be opened");
        session.setStatus(TrainingSessionStatus.OPEN);
        return toResponse(repository.save(session));
    }

    @Transactional
    public TrainingSessionResponse complete(UUID id) {
        TrainingSession session = load(id);
        if (session.getStatus() != TrainingSessionStatus.OPEN) throw new BadRequestException("Only open sessions can be completed");
        session.setStatus(TrainingSessionStatus.COMPLETED);
        return toResponse(repository.save(session));
    }

    @Transactional
    public TrainingSessionResponse cancel(UUID id, CancelTrainingSessionRequest request) {
        TrainingSession session = load(id);
        if (session.getStatus() != TrainingSessionStatus.SCHEDULED && session.getStatus() != TrainingSessionStatus.OPEN) {
            throw new BadRequestException("Only scheduled or open sessions can be cancelled");
        }
        if (request.reason() == null || request.reason().isBlank()) throw new BadRequestException("Cancellation reason is required");
        session.setStatus(TrainingSessionStatus.CANCELLED);
        session.setCancelledAt(Instant.now());
        session.setCancelledByUser(references.user(CurrentUser.get().userId()));
        session.setCancellationReason(request.reason().trim());
        return toResponse(repository.save(session));
    }

    private TrainingSession load(UUID id) {
        return repository.findByIdAndTenantId(id, CurrentUser.get().tenantId()).orElseThrow(() -> new NotFoundException("Training session not found"));
    }

    private Set<TrainingSessionStaff> assignments(TrainingSession session, List<TrainingSessionStaffRequest> requests) {
        if (requests == null || requests.isEmpty()) return Set.of();
        Set<UUID> seen = new HashSet<>();
        long leads = requests.stream().filter(item -> item.role() == TrainingSessionStaffRole.LEAD).count();
        if (leads > 1) throw new BadRequestException("Only one lead staff is allowed per session");
        Set<TrainingSessionStaff> result = new HashSet<>();
        for (TrainingSessionStaffRequest request : requests) {
            if (!seen.add(request.staffId())) throw new BadRequestException("Staff cannot be duplicated in the same session");
            result.add(new TrainingSessionStaff(session, loadValidStaff(request.staffId()), request.role()));
        }
        return result;
    }

    private StaffProfile loadValidStaff(UUID staffId) {
        StaffProfile staff = staffRepository.findByIdAndTenantId(staffId, CurrentUser.get().tenantId()).orElseThrow(() -> new NotFoundException("Staff profile not found"));
        if (staff.getStatus() != StaffStatus.ACTIVE || (staff.getType() != StaffType.PROFESSOR && staff.getType() != StaffType.INSTRUCTOR)) {
            throw new BadRequestException("Staff must be an active professor or instructor");
        }
        return staff;
    }

    private void validateTimes(LocalTime start, LocalTime end) {
        if (start == null || end == null || !end.isAfter(start)) throw new BadRequestException("Session endTime must be after startTime");
    }

    private Specification<ClassSchedule> activeScheduleSpec(UUID unitId) {
        return (root, query, cb) -> {
            if (query != null && !Long.class.equals(query.getResultType())) {
                root.fetch("dojoClass", JoinType.LEFT);
                root.fetch("unit", JoinType.LEFT);
                root.fetch("staff", JoinType.LEFT).fetch("staffProfile", JoinType.LEFT);
                query.distinct(true);
            }
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("tenant").get("id"), CurrentUser.get().tenantId()));
            predicates.add(cb.equal(root.get("status"), ClassScheduleStatus.ACTIVE));
            if (unitId != null) predicates.add(cb.equal(root.get("unit").get("id"), unitId));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<TrainingSession> spec(LocalDate from, LocalDate to, UUID unitId, UUID classId, UUID staffId, TrainingSessionStatus status) {
        return (root, query, cb) -> {
            if (query != null && !Long.class.equals(query.getResultType())) {
                root.fetch("dojoClass", JoinType.LEFT);
                root.fetch("classSchedule", JoinType.LEFT);
                root.fetch("unit", JoinType.LEFT);
                root.fetch("createdByUser", JoinType.LEFT);
                root.fetch("cancelledByUser", JoinType.LEFT);
                query.distinct(true);
            }
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("tenant").get("id"), CurrentUser.get().tenantId()));
            if (from != null) predicates.add(cb.greaterThanOrEqualTo(root.get("sessionDate"), from));
            if (to != null) predicates.add(cb.lessThanOrEqualTo(root.get("sessionDate"), to));
            if (unitId != null) predicates.add(cb.equal(root.get("unit").get("id"), unitId));
            if (classId != null) predicates.add(cb.equal(root.get("dojoClass").get("id"), classId));
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            if (staffId != null) {
                var staffJoin = root.join("staff");
                predicates.add(cb.equal(staffJoin.get("staffProfile").get("id"), staffId));
                if (query != null) query.distinct(true);
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private TrainingSessionResponse toResponse(TrainingSession session) {
        List<StaffSummary> staff = session.getStaff().stream()
            .sorted(Comparator.comparing((TrainingSessionStaff item) -> item.getRole().name()).reversed().thenComparing(item -> item.getStaffProfile().getDisplayName()))
            .map(item -> new StaffSummary(item.getStaffProfile().getId(), item.getStaffProfile().getDisplayName(), item.getStaffProfile().getType().name(), item.getRole()))
            .toList();
        return new TrainingSessionResponse(
            session.getId(),
            classResponse(session.getDojoClass()),
            session.getClassSchedule() == null ? null : new ScheduleSummary(session.getClassSchedule().getId()),
            PeopleMapper.unit(session.getUnit()),
            session.getSessionDate(),
            session.getStartTime(),
            session.getEndTime(),
            session.getStatus(),
            session.getCapacity(),
            session.getNotes(),
            staff,
            user(session.getCreatedByUser()),
            session.getCancelledAt(),
            user(session.getCancelledByUser()),
            session.getCancellationReason()
        );
    }

    private ClassResponse classResponse(DojoClass dojoClass) {
        return new ClassResponse(dojoClass.getId(), dojoClass.getName(), dojoClass.getDescription(), dojoClass.getModality(), dojoClass.getAudience(), dojoClass.getClassType(), dojoClass.getLevel(), dojoClass.getStatus(), dojoClass.getDefaultCapacity());
    }

    private UserSummary user(User user) {
        return user == null ? null : new UserSummary(user.getId(), user.getName(), user.getEmail());
    }

    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}

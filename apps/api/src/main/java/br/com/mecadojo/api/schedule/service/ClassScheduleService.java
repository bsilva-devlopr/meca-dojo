package br.com.mecadojo.api.schedule.service;

import br.com.mecadojo.api.identity.domain.Unit;
import br.com.mecadojo.api.identity.security.CurrentUser;
import br.com.mecadojo.api.people.api.dto.PageResponse;
import br.com.mecadojo.api.people.domain.StaffProfile;
import br.com.mecadojo.api.people.domain.StaffStatus;
import br.com.mecadojo.api.people.domain.StaffType;
import br.com.mecadojo.api.people.repository.StaffProfileRepository;
import br.com.mecadojo.api.people.service.PeopleMapper;
import br.com.mecadojo.api.people.service.PeopleReferenceService;
import br.com.mecadojo.api.schedule.api.dto.ScheduleDtos.CreateScheduleRequest;
import br.com.mecadojo.api.schedule.api.dto.ScheduleDtos.DayScheduleResponse;
import br.com.mecadojo.api.schedule.api.dto.ScheduleDtos.ScheduleResponse;
import br.com.mecadojo.api.schedule.api.dto.ScheduleDtos.ScheduleStaffRequest;
import br.com.mecadojo.api.schedule.api.dto.ScheduleDtos.StaffSummary;
import br.com.mecadojo.api.schedule.api.dto.ScheduleDtos.UpdateScheduleRequest;
import br.com.mecadojo.api.schedule.api.dto.ScheduleDtos.WeeklyScheduleResponse;
import br.com.mecadojo.api.schedule.domain.ClassAudience;
import br.com.mecadojo.api.schedule.domain.ClassModality;
import br.com.mecadojo.api.schedule.domain.ClassSchedule;
import br.com.mecadojo.api.schedule.domain.ClassScheduleStaff;
import br.com.mecadojo.api.schedule.domain.ClassScheduleStatus;
import br.com.mecadojo.api.schedule.domain.DojoClass;
import br.com.mecadojo.api.schedule.domain.ScheduleStaffRole;
import br.com.mecadojo.api.schedule.repository.ClassScheduleRepository;
import br.com.mecadojo.api.shared.exception.BadRequestException;
import br.com.mecadojo.api.shared.exception.NotFoundException;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClassScheduleService {
    private final ClassScheduleRepository scheduleRepository;
    private final StaffProfileRepository staffRepository;
    private final DojoClassService dojoClassService;
    private final PeopleReferenceService references;

    public ClassScheduleService(ClassScheduleRepository scheduleRepository, StaffProfileRepository staffRepository, DojoClassService dojoClassService, PeopleReferenceService references) {
        this.scheduleRepository = scheduleRepository;
        this.staffRepository = staffRepository;
        this.dojoClassService = dojoClassService;
        this.references = references;
    }

    @Transactional(readOnly = true)
    public PageResponse<ScheduleResponse> list(UUID unitId, UUID classId, DayOfWeek dayOfWeek, ClassScheduleStatus status, UUID staffId, int page, int size) {
        if (unitId != null) references.unit(unitId);
        if (classId != null) dojoClassService.load(classId);
        if (staffId != null) loadValidStaff(staffId);
        return PeopleMapper.page(scheduleRepository.findAll(spec(unitId, classId, dayOfWeek, status, staffId), PageRequest.of(page, Math.min(size, 100), Sort.by("dayOfWeek").ascending().and(Sort.by("startTime").ascending()))).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public WeeklyScheduleResponse weekly(UUID unitId, ClassModality modality, ClassAudience audience) {
        if (unitId != null) references.unit(unitId);
        List<ClassSchedule> schedules = scheduleRepository.findByTenantIdAndStatusOrderByDayOfWeekAscStartTimeAsc(CurrentUser.get().tenantId(), ClassScheduleStatus.ACTIVE)
            .stream()
            .filter(schedule -> unitId == null || schedule.getUnit().getId().equals(unitId))
            .filter(schedule -> modality == null || schedule.getDojoClass().getModality() == modality)
            .filter(schedule -> audience == null || schedule.getDojoClass().getAudience() == audience)
            .sorted(Comparator.comparing(ClassSchedule::getDayOfWeek).thenComparing(ClassSchedule::getStartTime))
            .toList();
        Map<DayOfWeek, List<ScheduleResponse>> grouped = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek day : DayOfWeek.values()) grouped.put(day, new ArrayList<>());
        schedules.forEach(schedule -> grouped.get(schedule.getDayOfWeek()).add(toResponse(schedule)));
        return new WeeklyScheduleResponse(grouped.entrySet().stream().map(entry -> new DayScheduleResponse(entry.getKey(), entry.getValue())).toList());
    }

    @Transactional
    public ScheduleResponse create(CreateScheduleRequest request) {
        validateTimes(request.startTime(), request.endTime());
        DojoClass dojoClass = dojoClassService.load(request.classId());
        Unit unit = references.unit(request.unitId());
        ClassSchedule schedule = new ClassSchedule(references.currentTenant(), dojoClass, unit, request.dayOfWeek(), request.startTime(), request.endTime());
        schedule.setCapacityOverride(request.capacityOverride());
        schedule.setStatus(request.status() == null ? ClassScheduleStatus.ACTIVE : request.status());
        schedule.replaceStaff(assignments(schedule, request.staff()));
        return toResponse(scheduleRepository.save(schedule));
    }

    @Transactional(readOnly = true)
    public ScheduleResponse get(UUID id) {
        return toResponse(load(id));
    }

    @Transactional
    public ScheduleResponse update(UUID id, UpdateScheduleRequest request) {
        ClassSchedule schedule = load(id);
        if (request.classId() != null) schedule.setDojoClass(dojoClassService.load(request.classId()));
        if (request.unitId() != null) schedule.setUnit(references.unit(request.unitId()));
        if (request.dayOfWeek() != null) schedule.setDayOfWeek(request.dayOfWeek());
        LocalTime start = request.startTime() != null ? request.startTime() : schedule.getStartTime();
        LocalTime end = request.endTime() != null ? request.endTime() : schedule.getEndTime();
        validateTimes(start, end);
        schedule.setStartTime(start);
        schedule.setEndTime(end);
        if (request.capacityOverride() != null) schedule.setCapacityOverride(request.capacityOverride());
        if (request.status() != null) schedule.setStatus(request.status());
        if (request.staff() != null) schedule.replaceStaff(assignments(schedule, request.staff()));
        return toResponse(scheduleRepository.save(schedule));
    }

    private ClassSchedule load(UUID id) {
        return scheduleRepository.findByIdAndTenantId(id, CurrentUser.get().tenantId()).orElseThrow(() -> new NotFoundException("Schedule not found"));
    }

    private Set<ClassScheduleStaff> assignments(ClassSchedule schedule, List<ScheduleStaffRequest> requests) {
        if (requests == null || requests.isEmpty()) return Set.of();
        Set<UUID> seen = new HashSet<>();
        long leads = requests.stream().filter(item -> item.role() == ScheduleStaffRole.LEAD).count();
        if (leads > 1) throw new BadRequestException("Only one lead staff is allowed per schedule");
        Set<ClassScheduleStaff> result = new HashSet<>();
        for (ScheduleStaffRequest request : requests) {
            if (!seen.add(request.staffId())) throw new BadRequestException("Staff cannot be duplicated in the same schedule");
            StaffProfile staff = loadValidStaff(request.staffId());
            result.add(new ClassScheduleStaff(schedule, staff, request.role()));
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
        if (start == null || end == null || !end.isAfter(start)) throw new BadRequestException("Schedule endTime must be after startTime");
    }

    private Specification<ClassSchedule> spec(UUID unitId, UUID classId, DayOfWeek dayOfWeek, ClassScheduleStatus status, UUID staffId) {
        return (root, query, cb) -> {
            if (query != null && !Long.class.equals(query.getResultType())) {
                root.fetch("dojoClass", JoinType.LEFT);
                root.fetch("unit", JoinType.LEFT);
            }
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("tenant").get("id"), CurrentUser.get().tenantId()));
            if (unitId != null) predicates.add(cb.equal(root.get("unit").get("id"), unitId));
            if (classId != null) predicates.add(cb.equal(root.get("dojoClass").get("id"), classId));
            if (dayOfWeek != null) predicates.add(cb.equal(root.get("dayOfWeek"), dayOfWeek));
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            if (staffId != null) {
                var staffJoin = root.join("staff");
                predicates.add(cb.equal(staffJoin.get("staffProfile").get("id"), staffId));
                if (query != null) query.distinct(true);
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private ScheduleResponse toResponse(ClassSchedule schedule) {
        List<StaffSummary> staff = schedule.getStaff().stream()
            .sorted(Comparator.comparing((ClassScheduleStaff item) -> item.getRole().name()).reversed().thenComparing(item -> item.getStaffProfile().getDisplayName()))
            .map(item -> new StaffSummary(item.getStaffProfile().getId(), item.getStaffProfile().getDisplayName(), item.getStaffProfile().getType().name(), item.getRole()))
            .collect(Collectors.toList());
        return new ScheduleResponse(
            schedule.getId(),
            dojoClassService.toResponse(schedule.getDojoClass()),
            PeopleMapper.unit(schedule.getUnit()),
            schedule.getDayOfWeek(),
            schedule.getStartTime(),
            schedule.getEndTime(),
            schedule.getCapacityOverride(),
            schedule.effectiveCapacity(),
            schedule.getStatus(),
            staff
        );
    }
}

package br.com.mecadojo.api.schedule.api.dto;

import br.com.mecadojo.api.people.api.dto.PageResponse;
import br.com.mecadojo.api.people.api.dto.PeopleCommonDtos.UnitSummary;
import br.com.mecadojo.api.schedule.domain.ClassAudience;
import br.com.mecadojo.api.schedule.domain.ClassLevel;
import br.com.mecadojo.api.schedule.domain.ClassModality;
import br.com.mecadojo.api.schedule.domain.ClassScheduleStatus;
import br.com.mecadojo.api.schedule.domain.ClassStatus;
import br.com.mecadojo.api.schedule.domain.ClassType;
import br.com.mecadojo.api.schedule.domain.ScheduleStaffRole;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public final class ScheduleDtos {
    private ScheduleDtos() {}

    public record CreateClassRequest(
        @NotBlank String name,
        String description,
        @NotNull ClassModality modality,
        @NotNull ClassAudience audience,
        @NotNull ClassType classType,
        @NotNull ClassLevel level,
        ClassStatus status,
        @Min(1) Integer defaultCapacity
    ) {}

    public record UpdateClassRequest(
        String name,
        String description,
        ClassModality modality,
        ClassAudience audience,
        ClassType classType,
        ClassLevel level,
        ClassStatus status,
        @Min(1) Integer defaultCapacity
    ) {}

    public record ClassResponse(
        UUID id,
        String name,
        String description,
        ClassModality modality,
        ClassAudience audience,
        ClassType classType,
        ClassLevel level,
        ClassStatus status,
        Integer defaultCapacity
    ) {}

    public record ScheduleStaffRequest(
        @NotNull UUID staffId,
        @NotNull ScheduleStaffRole role
    ) {}

    public record CreateScheduleRequest(
        @NotNull UUID classId,
        @NotNull UUID unitId,
        @NotNull DayOfWeek dayOfWeek,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        @Min(1) Integer capacityOverride,
        ClassScheduleStatus status,
        @Valid List<ScheduleStaffRequest> staff
    ) {}

    public record UpdateScheduleRequest(
        UUID classId,
        UUID unitId,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        @Min(1) Integer capacityOverride,
        ClassScheduleStatus status,
        @Valid List<ScheduleStaffRequest> staff
    ) {}

    public record StaffSummary(
        UUID id,
        String displayName,
        String type,
        ScheduleStaffRole role
    ) {}

    public record ScheduleResponse(
        UUID id,
        ClassResponse dojoClass,
        UnitSummary unit,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        Integer capacityOverride,
        Integer effectiveCapacity,
        ClassScheduleStatus status,
        List<StaffSummary> staff
    ) {}

    public record WeeklyScheduleResponse(
        List<DayScheduleResponse> days
    ) {}

    public record DayScheduleResponse(
        DayOfWeek dayOfWeek,
        List<ScheduleResponse> schedules
    ) {}

    public record ClassPage(PageResponse<ClassResponse> page) {}
}

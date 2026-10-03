package br.com.mecadojo.api.training.api.dto;

import br.com.mecadojo.api.people.api.dto.PageResponse;
import br.com.mecadojo.api.people.api.dto.PeopleCommonDtos.UnitSummary;
import br.com.mecadojo.api.schedule.api.dto.ScheduleDtos.ClassResponse;
import br.com.mecadojo.api.training.domain.TrainingSessionStaffRole;
import br.com.mecadojo.api.training.domain.TrainingSessionStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public final class TrainingSessionDtos {
    private TrainingSessionDtos() {}

    public record TrainingSessionStaffRequest(@NotNull UUID staffId, @NotNull TrainingSessionStaffRole role) {}

    public record CreateTrainingSessionRequest(
        @NotNull UUID dojoClassId,
        @NotNull UUID unitId,
        @NotNull LocalDate sessionDate,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        @Min(1) Integer capacity,
        String notes,
        @Valid List<TrainingSessionStaffRequest> staff
    ) {}

    public record UpdateTrainingSessionRequest(
        LocalDate sessionDate,
        LocalTime startTime,
        LocalTime endTime,
        UUID unitId,
        @Min(1) Integer capacity,
        String notes,
        @Valid List<TrainingSessionStaffRequest> staff
    ) {}

    public record GenerateTrainingSessionsRequest(@NotNull LocalDate from, @NotNull LocalDate to, UUID unitId) {}
    public record GenerateTrainingSessionsResponse(int created, int existing) {}
    public record CancelTrainingSessionRequest(@NotBlank String reason) {}

    public record StaffSummary(UUID id, String displayName, String type, TrainingSessionStaffRole role) {}
    public record ScheduleSummary(UUID id) {}
    public record UserSummary(UUID id, String name, String email) {}

    public record TrainingSessionResponse(
        UUID id,
        ClassResponse dojoClass,
        ScheduleSummary classSchedule,
        UnitSummary unit,
        LocalDate sessionDate,
        LocalTime startTime,
        LocalTime endTime,
        TrainingSessionStatus status,
        Integer capacity,
        String notes,
        List<StaffSummary> staff,
        UserSummary createdBy,
        Instant cancelledAt,
        UserSummary cancelledBy,
        String cancellationReason
    ) {}

    public record TrainingSessionPage(PageResponse<TrainingSessionResponse> page) {}
}

package br.com.mecadojo.api.attendance.api.dto;

import br.com.mecadojo.api.attendance.domain.AttendanceSource;
import br.com.mecadojo.api.attendance.domain.AttendanceStatus;
import br.com.mecadojo.api.attendance.domain.CheckInStatus;
import br.com.mecadojo.api.people.api.dto.PeopleCommonDtos.UnitSummary;
import br.com.mecadojo.api.schedule.domain.ClassModality;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public final class AttendanceDtos {
    private AttendanceDtos() {}

    public record ManualAttendanceRequest(@NotNull UUID studentId) {}
    public record VoidAttendanceRequest(@NotBlank String reason) {}
    public record CreateCheckInTokenResponse(String token, Instant expiresAt, String checkInUrl) {}
    public record SelfCheckInRequest(@NotBlank String token) {}

    public record UserSummary(UUID id, String name, String email) {}
    public record StudentSummary(UUID id, String fullName, String preferredName, String status) {}
    public record ClassSummary(UUID id, String name, ClassModality modality) {}
    public record TrainingSessionSummary(UUID id, ClassSummary dojoClass, UnitSummary unit, LocalDate sessionDate, LocalTime startTime, LocalTime endTime, String status) {}

    public record AttendanceResponse(
        UUID id,
        UUID trainingSessionId,
        StudentSummary student,
        AttendanceSource source,
        AttendanceStatus status,
        Instant checkedInAt,
        UserSummary recordedBy,
        UUID checkInId,
        Instant voidedAt,
        UserSummary voidedBy,
        String voidReason
    ) {}

    public record CheckInResponse(
        CheckInStatus status,
        String message,
        AttendanceResponse attendance,
        TrainingSessionSummary trainingSession,
        Instant occurredAt
    ) {}

    public record StudentAttendanceHistoryItem(
        UUID attendanceId,
        TrainingSessionSummary trainingSession,
        AttendanceSource source,
        Instant checkedInAt
    ) {}

    public record StudentAttendanceSummary(
        long totalTrainings,
        long last30Days,
        long last90Days,
        long giCount,
        long noGiCount,
        long currentMonth
    ) {}

    public record AttendanceListResponse(List<AttendanceResponse> attendances) {}
}

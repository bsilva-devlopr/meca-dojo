package br.com.mecadojo.api.student.api.dto;

import br.com.mecadojo.api.graduation.domain.BeltSystem;
import br.com.mecadojo.api.people.domain.StudentStatus;
import br.com.mecadojo.api.schedule.domain.ClassModality;
import br.com.mecadojo.api.training.domain.TrainingSessionStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public final class StudentExperienceDtos {
    private StudentExperienceDtos() {}

    public record StudentProfileResponse(
        String displayName,
        String fullName,
        String preferredName,
        String email,
        String phone,
        StudentStatus status,
        LocalDate joinDate,
        UnitSummary primaryUnit,
        EmergencyContactSummary emergencyContact
    ) {}

    public record UnitSummary(String name) {}
    public record EmergencyContactSummary(String name, String relationship, String phone) {}

    public record BeltSummary(String code, String name, BeltSystem system, int sequence, int maxDegree) {}

    public record CurrentBeltResponse(
        BeltSummary belt,
        int degree,
        LocalDate graduatedAt,
        StaffSummary awardedBy,
        String type,
        CriteriaResponse criteria
    ) {}

    public record CriteriaResponse(long completedTrainings, Integer minimumTrainingCount, Long daysInGrade, Integer minimumDaysInGrade) {}

    public record StaffSummary(String name) {}

    public record GraduationHistoryItem(
        BeltSummary belt,
        int degree,
        LocalDate graduatedAt,
        StaffSummary awardedBy,
        String type
    ) {}

    public record FrequencySummary(long totalTrainings, long last30Days, long last90Days, long currentMonth, long giCount, long noGiCount) {}

    public record SessionCard(
        UUID id,
        String className,
        ClassModality modality,
        String unitName,
        LocalDate sessionDate,
        LocalTime startTime,
        LocalTime endTime,
        TrainingSessionStatus status,
        List<StaffSummary> staff,
        boolean attendanceRegistered
    ) {}

    public record TrainingHistoryItem(
        UUID attendanceId,
        SessionCard session,
        String source,
        Instant checkedInAt
    ) {}

    public record StudentDashboardResponse(
        StudentProfileResponse student,
        CurrentBeltResponse currentBelt,
        FrequencySummary frequency,
        SessionCard currentSession,
        List<SessionCard> upcomingSessions,
        List<TrainingHistoryItem> recentTrainings,
        List<GraduationHistoryItem> graduationHistory
    ) {}

    public record GraduationResponse(
        CurrentBeltResponse current,
        List<GraduationHistoryItem> history
    ) {}
}

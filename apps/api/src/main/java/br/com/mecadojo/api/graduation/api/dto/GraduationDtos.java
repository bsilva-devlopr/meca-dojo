package br.com.mecadojo.api.graduation.api.dto;

import br.com.mecadojo.api.graduation.domain.BeltSystem;
import br.com.mecadojo.api.graduation.domain.GraduationType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class GraduationDtos {
    private GraduationDtos() {
    }

    public record BeltResponse(UUID id, String code, String name, BeltSystem system, int sequence, int maxDegree) {
    }

    public record StaffSummary(UUID id, String name) {
    }

    public record UserSummary(UUID id, String name, String email) {
    }

    public record GraduationEventResponse(
        UUID id,
        BeltResponse belt,
        int degree,
        LocalDate graduatedAt,
        StaffSummary awardedBy,
        GraduationType type,
        String notes,
        UserSummary createdBy,
        UUID correctsGraduationId,
        String correctionReason,
        Instant createdAt
    ) {
    }

    public record EligibilityResponse(boolean eligible, List<String> reasons, List<String> warnings) {
    }

    public record CurrentGraduationResponse(GraduationEventResponse current, EligibilityResponse eligibility) {
    }

    public record InitialGraduationRequest(
        @NotBlank String beltCode,
        BeltSystem system,
        @Min(0) int degree,
        LocalDate graduatedAt,
        UUID awardedByStaffId,
        String notes
    ) {
    }

    public record DegreePromotionRequest(
        @NotNull LocalDate graduatedAt,
        UUID awardedByStaffId,
        String notes
    ) {
    }

    public record BeltPromotionRequest(
        @NotBlank String beltCode,
        BeltSystem system,
        @NotNull LocalDate graduatedAt,
        UUID awardedByStaffId,
        String notes
    ) {
    }

    public record CorrectionRequest(
        @NotBlank String beltCode,
        BeltSystem system,
        @Min(0) int degree,
        LocalDate graduatedAt,
        UUID awardedByStaffId,
        UUID correctsGraduationId,
        String notes,
        @NotBlank String reason
    ) {
    }

    public record GraduationPolicyResponse(
        UUID id,
        BeltResponse belt,
        Integer minimumTrainingCount,
        Integer minimumDaysInGrade,
        boolean active
    ) {
    }

    public record UpdateGraduationPolicyRequest(
        @Min(0) Integer minimumTrainingCount,
        @Min(0) Integer minimumDaysInGrade,
        Boolean active
    ) {
    }
}

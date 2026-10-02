package br.com.mecadojo.api.people.api.dto;

import br.com.mecadojo.api.people.api.dto.PeopleCommonDtos.UnitSummary;
import br.com.mecadojo.api.people.api.dto.PeopleCommonDtos.UserSummary;
import br.com.mecadojo.api.people.domain.GuardianRelationship;
import br.com.mecadojo.api.people.domain.StudentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class StudentDtos {
    private StudentDtos() {
    }

    public record StudentListItemResponse(
        UUID id,
        String fullName,
        String preferredName,
        StudentStatus status,
        LocalDate joinDate,
        UnitSummary primaryUnit
    ) {
    }

    public record StudentResponse(
        UUID id,
        String fullName,
        String preferredName,
        LocalDate birthDate,
        String email,
        String phone,
        StudentStatus status,
        LocalDate joinDate,
        UnitSummary primaryUnit,
        UserSummary user,
        EmergencyContactResponse emergencyContact,
        List<StudentGuardianResponse> guardians,
        String notes
    ) {
    }

    public record CreateStudentRequest(
        UUID userId,
        @NotNull UUID primaryUnitId,
        @NotBlank String fullName,
        String preferredName,
        LocalDate birthDate,
        String email,
        String phone,
        @NotNull StudentStatus status,
        @NotNull LocalDate joinDate,
        String notes
    ) {
    }

    public record UpdateStudentRequest(
        UUID userId,
        UUID primaryUnitId,
        String fullName,
        String preferredName,
        LocalDate birthDate,
        String email,
        String phone,
        StudentStatus status,
        LocalDate joinDate,
        String notes
    ) {
    }

    public record LinkUserRequest(UUID userId) {
    }

    public record EmergencyContactRequest(@NotBlank String name, @NotBlank String relationship, @NotBlank String phone) {
    }

    public record EmergencyContactResponse(UUID id, String name, String relationship, String phone) {
    }

    public record LinkGuardianRequest(@NotNull UUID guardianId, @NotNull GuardianRelationship relationship, boolean primary) {
    }

    public record StudentGuardianResponse(
        UUID id,
        String fullName,
        String email,
        String phone,
        GuardianRelationship relationship,
        boolean primary
    ) {
    }

    public record CreateGuardianForStudentRequest(
        @NotBlank String fullName,
        String email,
        @NotBlank String phone,
        @NotNull GuardianRelationship relationship,
        boolean primary
    ) {
    }
}

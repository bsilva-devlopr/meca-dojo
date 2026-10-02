package br.com.mecadojo.api.people.api.dto;

import br.com.mecadojo.api.people.api.dto.PeopleCommonDtos.UnitSummary;
import br.com.mecadojo.api.people.api.dto.PeopleCommonDtos.UserSummary;
import br.com.mecadojo.api.people.domain.StaffStatus;
import br.com.mecadojo.api.people.domain.StaffType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public final class StaffDtos {
    private StaffDtos() {
    }

    public record StaffResponse(
        UUID id,
        StaffType type,
        StaffStatus status,
        String displayName,
        UserSummary user,
        UnitSummary primaryUnit
    ) {
    }

    public record CreateStaffRequest(
        @NotNull UUID userId,
        @NotNull StaffType type,
        @NotBlank String displayName,
        UUID primaryUnitId
    ) {
    }

    public record UpdateStaffRequest(StaffType type, StaffStatus status, String displayName, UUID primaryUnitId) {
    }
}

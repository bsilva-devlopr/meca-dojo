package br.com.mecadojo.api.people.api.dto;

import br.com.mecadojo.api.people.api.dto.PeopleCommonDtos.UserSummary;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public final class GuardianDtos {
    private GuardianDtos() {
    }

    public record GuardianResponse(UUID id, String fullName, String email, String phone, UserSummary user) {
    }

    public record CreateGuardianRequest(UUID userId, @NotBlank String fullName, String email, @NotBlank String phone) {
    }

    public record UpdateGuardianRequest(UUID userId, String fullName, String email, String phone) {
    }

    public record LinkUserRequest(UUID userId) {
    }
}

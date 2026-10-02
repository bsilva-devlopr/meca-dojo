package br.com.mecadojo.api.identity.api.dto;

import br.com.mecadojo.api.identity.domain.RoleName;
import br.com.mecadojo.api.identity.domain.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public final class UserDtos {
    private UserDtos() {
    }

    public record UserResponse(
        UUID id,
        String name,
        String email,
        UserStatus status,
        Set<RoleName> roles,
        Instant createdAt,
        Instant updatedAt,
        Instant lastLoginAt
    ) {
    }

    public record CreateUserRequest(
        @NotBlank String name,
        @Email @NotBlank String email,
        @NotBlank String password,
        @NotEmpty Set<RoleName> roles
    ) {
    }

    public record UpdateUserRequest(String name, @Email String email, UserStatus status) {
    }

    public record UpdateUserRolesRequest(@NotEmpty Set<RoleName> roles) {
    }
}

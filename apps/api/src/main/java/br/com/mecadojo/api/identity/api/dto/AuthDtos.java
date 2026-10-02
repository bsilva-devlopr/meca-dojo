package br.com.mecadojo.api.identity.api.dto;

import br.com.mecadojo.api.identity.domain.PermissionName;
import br.com.mecadojo.api.identity.domain.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;
import java.util.UUID;

public final class AuthDtos {
    private AuthDtos() {
    }

    public record LoginRequest(
        @NotBlank String tenant,
        @Email @NotBlank String email,
        @NotBlank String password
    ) {
    }

    public record RefreshRequest(@NotBlank String refreshToken) {
    }

    public record LogoutRequest(@NotBlank String refreshToken) {
    }

    public record TokenResponse(String accessToken, String refreshToken, long expiresInSeconds) {
    }

    public record MeResponse(
        UUID id,
        TenantSummary tenant,
        String name,
        String email,
        Set<RoleName> roles,
        Set<PermissionName> permissions
    ) {
    }

    public record TenantSummary(UUID id, String name, String slug) {
    }
}

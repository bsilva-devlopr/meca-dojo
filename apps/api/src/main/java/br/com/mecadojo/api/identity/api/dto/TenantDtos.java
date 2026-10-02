package br.com.mecadojo.api.identity.api.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public final class TenantDtos {
    private TenantDtos() {
    }

    public record TenantResponse(UUID id, String name, String slug, String status) {
    }

    public record UpdateTenantRequest(@NotBlank String name) {
    }
}

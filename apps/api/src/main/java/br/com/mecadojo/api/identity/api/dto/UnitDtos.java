package br.com.mecadojo.api.identity.api.dto;

import br.com.mecadojo.api.identity.domain.UnitStatus;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public final class UnitDtos {
    private UnitDtos() {
    }

    public record UnitResponse(UUID id, String name, String slug, UnitStatus status) {
    }

    public record CreateUnitRequest(@NotBlank String name, @NotBlank String slug) {
    }

    public record UpdateUnitRequest(String name, UnitStatus status) {
    }
}

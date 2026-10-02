package br.com.mecadojo.api.people.api.dto;

import java.util.UUID;

public final class PeopleCommonDtos {
    private PeopleCommonDtos() {
    }

    public record UnitSummary(UUID id, String name) {
    }

    public record UserSummary(UUID id, String name, String email) {
    }
}

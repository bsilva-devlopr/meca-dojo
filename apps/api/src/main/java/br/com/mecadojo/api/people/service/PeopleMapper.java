package br.com.mecadojo.api.people.service;

import br.com.mecadojo.api.identity.domain.Unit;
import br.com.mecadojo.api.identity.domain.User;
import br.com.mecadojo.api.people.api.dto.PageResponse;
import br.com.mecadojo.api.people.api.dto.PeopleCommonDtos.UnitSummary;
import br.com.mecadojo.api.people.api.dto.PeopleCommonDtos.UserSummary;
import org.springframework.data.domain.Page;

public final class PeopleMapper {
    private PeopleMapper() {
    }

    public static UnitSummary unit(Unit unit) {
        return unit == null ? null : new UnitSummary(unit.getId(), unit.getName());
    }

    public static UserSummary user(User user) {
        return user == null ? null : new UserSummary(user.getId(), user.getName(), user.getEmail());
    }

    public static <T> PageResponse<T> page(Page<T> page) {
        return new PageResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages()
        );
    }
}

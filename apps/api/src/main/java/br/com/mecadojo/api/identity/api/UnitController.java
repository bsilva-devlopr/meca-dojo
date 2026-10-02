package br.com.mecadojo.api.identity.api;

import br.com.mecadojo.api.identity.api.dto.UnitDtos.CreateUnitRequest;
import br.com.mecadojo.api.identity.api.dto.UnitDtos.UnitResponse;
import br.com.mecadojo.api.identity.api.dto.UnitDtos.UpdateUnitRequest;
import br.com.mecadojo.api.identity.service.UnitService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/units")
@Tag(name = "Units")
public class UnitController {
    private final UnitService unitService;

    public UnitController(UnitService unitService) {
        this.unitService = unitService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('UNIT_READ')")
    List<UnitResponse> list() {
        return unitService.list();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('UNIT_CREATE')")
    UnitResponse create(@Valid @RequestBody CreateUnitRequest request) {
        return unitService.create(request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('UNIT_READ')")
    UnitResponse get(@PathVariable UUID id) {
        return unitService.get(id);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('UNIT_UPDATE')")
    UnitResponse update(@PathVariable UUID id, @RequestBody UpdateUnitRequest request) {
        return unitService.update(id, request);
    }
}

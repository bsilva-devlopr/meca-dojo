package br.com.mecadojo.api.people.api;

import br.com.mecadojo.api.people.api.dto.GuardianDtos.CreateGuardianRequest;
import br.com.mecadojo.api.people.api.dto.GuardianDtos.GuardianResponse;
import br.com.mecadojo.api.people.api.dto.GuardianDtos.LinkUserRequest;
import br.com.mecadojo.api.people.api.dto.GuardianDtos.UpdateGuardianRequest;
import br.com.mecadojo.api.people.api.dto.PageResponse;
import br.com.mecadojo.api.people.service.GuardianService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/guardians")
@Tag(name = "Guardians")
public class GuardianController {
    private final GuardianService guardianService;

    public GuardianController(GuardianService guardianService) {
        this.guardianService = guardianService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('GUARDIAN_READ')")
    PageResponse<GuardianResponse> list(@RequestParam(required = false) String search, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return guardianService.list(search, page, size);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('GUARDIAN_CREATE')")
    GuardianResponse create(@Valid @RequestBody CreateGuardianRequest request) {
        return guardianService.create(request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('GUARDIAN_READ')")
    GuardianResponse get(@PathVariable UUID id) {
        return guardianService.get(id);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('GUARDIAN_UPDATE')")
    GuardianResponse update(@PathVariable UUID id, @RequestBody UpdateGuardianRequest request) {
        return guardianService.update(id, request);
    }

    @PutMapping("/{id}/user")
    @PreAuthorize("hasAuthority('GUARDIAN_UPDATE')")
    GuardianResponse linkUser(@PathVariable UUID id, @RequestBody LinkUserRequest request) {
        return guardianService.linkUser(id, request.userId());
    }
}

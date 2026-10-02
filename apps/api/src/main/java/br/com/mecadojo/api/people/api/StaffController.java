package br.com.mecadojo.api.people.api;

import br.com.mecadojo.api.people.api.dto.PageResponse;
import br.com.mecadojo.api.people.api.dto.StaffDtos.CreateStaffRequest;
import br.com.mecadojo.api.people.api.dto.StaffDtos.StaffResponse;
import br.com.mecadojo.api.people.api.dto.StaffDtos.UpdateStaffRequest;
import br.com.mecadojo.api.people.domain.StaffStatus;
import br.com.mecadojo.api.people.domain.StaffType;
import br.com.mecadojo.api.people.service.StaffService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/staff")
@Tag(name = "Staff")
public class StaffController {
    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('STAFF_READ')")
    PageResponse<StaffResponse> list(
        @RequestParam(required = false) StaffType type,
        @RequestParam(required = false) StaffStatus status,
        @RequestParam(required = false) UUID unitId,
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return staffService.list(type, status, unitId, search, page, size);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('STAFF_CREATE')")
    StaffResponse create(@Valid @RequestBody CreateStaffRequest request) {
        return staffService.create(request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('STAFF_READ')")
    StaffResponse get(@PathVariable UUID id) {
        return staffService.get(id);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('STAFF_UPDATE')")
    StaffResponse update(@PathVariable UUID id, @RequestBody UpdateStaffRequest request) {
        return staffService.update(id, request);
    }
}

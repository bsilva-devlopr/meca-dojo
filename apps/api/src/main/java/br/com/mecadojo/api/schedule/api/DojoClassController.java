package br.com.mecadojo.api.schedule.api;

import br.com.mecadojo.api.people.api.dto.PageResponse;
import br.com.mecadojo.api.schedule.api.dto.ScheduleDtos.ClassResponse;
import br.com.mecadojo.api.schedule.api.dto.ScheduleDtos.CreateClassRequest;
import br.com.mecadojo.api.schedule.api.dto.ScheduleDtos.UpdateClassRequest;
import br.com.mecadojo.api.schedule.domain.ClassAudience;
import br.com.mecadojo.api.schedule.domain.ClassLevel;
import br.com.mecadojo.api.schedule.domain.ClassModality;
import br.com.mecadojo.api.schedule.domain.ClassStatus;
import br.com.mecadojo.api.schedule.domain.ClassType;
import br.com.mecadojo.api.schedule.service.DojoClassService;
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
@RequestMapping("/api/v1/classes")
@Tag(name = "Classes")
public class DojoClassController {
    private final DojoClassService service;

    public DojoClassController(DojoClassService service) { this.service = service; }

    @GetMapping
    @PreAuthorize("hasAuthority('CLASS_READ')")
    PageResponse<ClassResponse> list(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) ClassStatus status,
        @RequestParam(required = false) ClassModality modality,
        @RequestParam(required = false) ClassAudience audience,
        @RequestParam(required = false) ClassType classType,
        @RequestParam(required = false) ClassLevel level,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return service.list(search, status, modality, audience, classType, level, page, size);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CLASS_CREATE')")
    ClassResponse create(@Valid @RequestBody CreateClassRequest request) { return service.create(request); }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CLASS_READ')")
    ClassResponse get(@PathVariable UUID id) { return service.get(id); }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('CLASS_UPDATE')")
    ClassResponse update(@PathVariable UUID id, @RequestBody UpdateClassRequest request) { return service.update(id, request); }
}

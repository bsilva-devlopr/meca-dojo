package br.com.mecadojo.api.schedule.api;

import br.com.mecadojo.api.people.api.dto.PageResponse;
import br.com.mecadojo.api.schedule.api.dto.ScheduleDtos.CreateScheduleRequest;
import br.com.mecadojo.api.schedule.api.dto.ScheduleDtos.ScheduleResponse;
import br.com.mecadojo.api.schedule.api.dto.ScheduleDtos.UpdateScheduleRequest;
import br.com.mecadojo.api.schedule.api.dto.ScheduleDtos.WeeklyScheduleResponse;
import br.com.mecadojo.api.schedule.domain.ClassAudience;
import br.com.mecadojo.api.schedule.domain.ClassModality;
import br.com.mecadojo.api.schedule.domain.ClassScheduleStatus;
import br.com.mecadojo.api.schedule.service.ClassScheduleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.DayOfWeek;
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
@RequestMapping("/api/v1/schedules")
@Tag(name = "Schedules")
public class ClassScheduleController {
    private final ClassScheduleService service;

    public ClassScheduleController(ClassScheduleService service) { this.service = service; }

    @GetMapping
    @PreAuthorize("hasAuthority('SCHEDULE_READ')")
    PageResponse<ScheduleResponse> list(
        @RequestParam(required = false) UUID unitId,
        @RequestParam(required = false) UUID classId,
        @RequestParam(required = false) DayOfWeek dayOfWeek,
        @RequestParam(required = false) ClassScheduleStatus status,
        @RequestParam(required = false) UUID staffId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return service.list(unitId, classId, dayOfWeek, status, staffId, page, size);
    }

    @GetMapping("/weekly")
    @PreAuthorize("hasAuthority('SCHEDULE_READ')")
    WeeklyScheduleResponse weekly(@RequestParam(required = false) UUID unitId, @RequestParam(required = false) ClassModality modality, @RequestParam(required = false) ClassAudience audience) {
        return service.weekly(unitId, modality, audience);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SCHEDULE_CREATE')")
    ScheduleResponse create(@Valid @RequestBody CreateScheduleRequest request) { return service.create(request); }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SCHEDULE_READ')")
    ScheduleResponse get(@PathVariable UUID id) { return service.get(id); }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('SCHEDULE_UPDATE')")
    ScheduleResponse update(@PathVariable UUID id, @RequestBody UpdateScheduleRequest request) { return service.update(id, request); }
}

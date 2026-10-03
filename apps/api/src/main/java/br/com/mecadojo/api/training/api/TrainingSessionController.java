package br.com.mecadojo.api.training.api;

import br.com.mecadojo.api.people.api.dto.PageResponse;
import br.com.mecadojo.api.training.api.dto.TrainingSessionDtos.CancelTrainingSessionRequest;
import br.com.mecadojo.api.training.api.dto.TrainingSessionDtos.CreateTrainingSessionRequest;
import br.com.mecadojo.api.training.api.dto.TrainingSessionDtos.GenerateTrainingSessionsRequest;
import br.com.mecadojo.api.training.api.dto.TrainingSessionDtos.GenerateTrainingSessionsResponse;
import br.com.mecadojo.api.training.api.dto.TrainingSessionDtos.TrainingSessionResponse;
import br.com.mecadojo.api.training.api.dto.TrainingSessionDtos.UpdateTrainingSessionRequest;
import br.com.mecadojo.api.training.domain.TrainingSessionStatus;
import br.com.mecadojo.api.training.service.TrainingSessionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
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
@RequestMapping("/api/v1/training-sessions")
@Tag(name = "Training Sessions")
public class TrainingSessionController {
    private final TrainingSessionService service;

    public TrainingSessionController(TrainingSessionService service) { this.service = service; }

    @GetMapping
    @PreAuthorize("hasAuthority('TRAINING_SESSION_READ')")
    PageResponse<TrainingSessionResponse> list(
        @RequestParam(required = false) LocalDate from,
        @RequestParam(required = false) LocalDate to,
        @RequestParam(required = false) UUID unitId,
        @RequestParam(required = false) UUID classId,
        @RequestParam(required = false) UUID staffId,
        @RequestParam(required = false) TrainingSessionStatus status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size
    ) {
        return service.list(from, to, unitId, classId, staffId, status, page, size);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('TRAINING_SESSION_CREATE')")
    TrainingSessionResponse create(@Valid @RequestBody CreateTrainingSessionRequest request) { return service.create(request); }

    @PostMapping("/generate")
    @PreAuthorize("hasAuthority('TRAINING_SESSION_CREATE')")
    GenerateTrainingSessionsResponse generate(@Valid @RequestBody GenerateTrainingSessionsRequest request) { return service.generate(request); }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TRAINING_SESSION_READ')")
    TrainingSessionResponse get(@PathVariable UUID id) { return service.get(id); }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('TRAINING_SESSION_UPDATE')")
    TrainingSessionResponse update(@PathVariable UUID id, @RequestBody UpdateTrainingSessionRequest request) { return service.update(id, request); }

    @PostMapping("/{id}/open")
    @PreAuthorize("hasAuthority('TRAINING_SESSION_OPEN')")
    TrainingSessionResponse open(@PathVariable UUID id) { return service.open(id); }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('TRAINING_SESSION_COMPLETE')")
    TrainingSessionResponse complete(@PathVariable UUID id) { return service.complete(id); }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('TRAINING_SESSION_CANCEL')")
    TrainingSessionResponse cancel(@PathVariable UUID id, @Valid @RequestBody CancelTrainingSessionRequest request) { return service.cancel(id, request); }
}

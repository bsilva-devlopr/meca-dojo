package br.com.mecadojo.api.graduation.api;

import br.com.mecadojo.api.graduation.api.dto.GraduationDtos.BeltPromotionRequest;
import br.com.mecadojo.api.graduation.api.dto.GraduationDtos.CorrectionRequest;
import br.com.mecadojo.api.graduation.api.dto.GraduationDtos.CurrentGraduationResponse;
import br.com.mecadojo.api.graduation.api.dto.GraduationDtos.DegreePromotionRequest;
import br.com.mecadojo.api.graduation.api.dto.GraduationDtos.GraduationEventResponse;
import br.com.mecadojo.api.graduation.api.dto.GraduationDtos.InitialGraduationRequest;
import br.com.mecadojo.api.graduation.service.GraduationService;
import br.com.mecadojo.api.people.api.dto.PageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/students/{studentId}")
@Tag(name = "Graduations")
public class GraduationController {
    private final GraduationService graduationService;

    public GraduationController(GraduationService graduationService) {
        this.graduationService = graduationService;
    }

    @GetMapping("/graduation")
    @PreAuthorize("hasAuthority('GRADUATION_READ')")
    CurrentGraduationResponse current(@PathVariable UUID studentId) {
        return graduationService.current(studentId);
    }

    @GetMapping("/graduations")
    @PreAuthorize("hasAuthority('GRADUATION_READ')")
    PageResponse<GraduationEventResponse> history(
        @PathVariable UUID studentId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return graduationService.history(studentId, page, size);
    }

    @PostMapping("/graduations/initial")
    @PreAuthorize("hasAuthority('GRADUATION_CREATE')")
    GraduationEventResponse initial(@PathVariable UUID studentId, @Valid @RequestBody InitialGraduationRequest request) {
        return graduationService.initial(studentId, request);
    }

    @PostMapping("/graduations/degree")
    @PreAuthorize("hasAuthority('GRADUATION_CREATE')")
    GraduationEventResponse degree(@PathVariable UUID studentId, @Valid @RequestBody DegreePromotionRequest request) {
        return graduationService.promoteDegree(studentId, request);
    }

    @PostMapping("/graduations/belt")
    @PreAuthorize("hasAuthority('GRADUATION_CREATE')")
    GraduationEventResponse belt(@PathVariable UUID studentId, @Valid @RequestBody BeltPromotionRequest request) {
        return graduationService.promoteBelt(studentId, request);
    }

    @PostMapping("/graduations/correction")
    @PreAuthorize("hasAuthority('GRADUATION_CORRECT')")
    GraduationEventResponse correction(@PathVariable UUID studentId, @Valid @RequestBody CorrectionRequest request) {
        return graduationService.correction(studentId, request);
    }
}

package br.com.mecadojo.api.student.api;

import br.com.mecadojo.api.people.api.dto.PageResponse;
import br.com.mecadojo.api.student.api.dto.StudentExperienceDtos.*;
import br.com.mecadojo.api.student.service.StudentExperienceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/student/me")
@Tag(name = "Student Experience")
public class StudentExperienceController {
    private final StudentExperienceService service;

    public StudentExperienceController(StudentExperienceService service) {
        this.service = service;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("isAuthenticated()")
    StudentDashboardResponse dashboard() {
        return service.dashboard();
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    StudentProfileResponse profile() {
        return service.profile();
    }

    @GetMapping("/training-history")
    @PreAuthorize("isAuthenticated()")
    PageResponse<TrainingHistoryItem> trainingHistory(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return service.trainingHistory(page, size);
    }

    @GetMapping("/graduation")
    @PreAuthorize("isAuthenticated()")
    GraduationResponse graduation() {
        return service.graduation();
    }
}

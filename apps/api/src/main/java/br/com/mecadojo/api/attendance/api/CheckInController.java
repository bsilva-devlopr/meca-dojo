package br.com.mecadojo.api.attendance.api;

import br.com.mecadojo.api.attendance.api.dto.AttendanceDtos.*;
import br.com.mecadojo.api.attendance.service.CheckInService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/check-ins")
@Tag(name = "Check-in")
public class CheckInController {
    private final CheckInService checkInService;

    public CheckInController(CheckInService checkInService) {
        this.checkInService = checkInService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    CheckInResponse selfCheckIn(@Valid @RequestBody SelfCheckInRequest request) {
        return checkInService.selfCheckIn(request);
    }
}

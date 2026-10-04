package br.com.mecadojo.api.attendance.api;

import br.com.mecadojo.api.attendance.api.dto.AttendanceDtos.*;
import br.com.mecadojo.api.attendance.service.AttendanceService;
import br.com.mecadojo.api.attendance.service.CheckInService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/api/v1/training-sessions/{sessionId}")
@Tag(name = "Attendance")
public class TrainingSessionAttendanceController {
    private final AttendanceService attendanceService;
    private final CheckInService checkInService;

    public TrainingSessionAttendanceController(AttendanceService attendanceService, CheckInService checkInService) {
        this.attendanceService = attendanceService;
        this.checkInService = checkInService;
    }

    @GetMapping("/attendance")
    @PreAuthorize("hasAuthority('ATTENDANCE_READ')")
    List<AttendanceResponse> list(@PathVariable UUID sessionId, @RequestParam(defaultValue = "false") boolean includeVoided) {
        return attendanceService.listForSession(sessionId, includeVoided);
    }

    @PostMapping("/attendance")
    @PreAuthorize("hasAuthority('ATTENDANCE_CREATE')")
    AttendanceResponse create(@PathVariable UUID sessionId, @Valid @RequestBody ManualAttendanceRequest request) {
        return attendanceService.registerManual(sessionId, request);
    }

    @PostMapping("/check-in-token")
    @PreAuthorize("hasAuthority('CHECKIN_TOKEN_CREATE')")
    CreateCheckInTokenResponse createToken(@PathVariable UUID sessionId) {
        return checkInService.createToken(sessionId);
    }
}

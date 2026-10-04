package br.com.mecadojo.api.attendance.api;

import br.com.mecadojo.api.attendance.api.dto.AttendanceDtos.*;
import br.com.mecadojo.api.attendance.service.AttendanceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/attendance")
@Tag(name = "Attendance")
public class AttendanceController {
    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/{id}/void")
    @PreAuthorize("hasAuthority('ATTENDANCE_VOID')")
    AttendanceResponse voidAttendance(@PathVariable UUID id, @Valid @RequestBody VoidAttendanceRequest request) {
        return attendanceService.voidAttendance(id, request);
    }
}

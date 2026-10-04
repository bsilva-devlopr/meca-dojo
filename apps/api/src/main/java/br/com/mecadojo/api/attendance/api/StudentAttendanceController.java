package br.com.mecadojo.api.attendance.api;

import br.com.mecadojo.api.attendance.api.dto.AttendanceDtos.StudentAttendanceHistoryItem;
import br.com.mecadojo.api.attendance.api.dto.AttendanceDtos.StudentAttendanceSummary;
import br.com.mecadojo.api.attendance.service.AttendanceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/students/{studentId}/attendance")
@Tag(name = "Student Attendance")
public class StudentAttendanceController {
    private final AttendanceService attendanceService;

    public StudentAttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ATTENDANCE_READ')")
    List<StudentAttendanceHistoryItem> history(@PathVariable UUID studentId) {
        return attendanceService.studentHistory(studentId);
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('ATTENDANCE_READ')")
    StudentAttendanceSummary summary(@PathVariable UUID studentId) {
        return attendanceService.studentSummary(studentId);
    }
}

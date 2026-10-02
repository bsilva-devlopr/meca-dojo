package br.com.mecadojo.api.people.api;

import br.com.mecadojo.api.people.api.dto.PageResponse;
import br.com.mecadojo.api.people.api.dto.StudentDtos.CreateGuardianForStudentRequest;
import br.com.mecadojo.api.people.api.dto.StudentDtos.CreateStudentRequest;
import br.com.mecadojo.api.people.api.dto.StudentDtos.EmergencyContactRequest;
import br.com.mecadojo.api.people.api.dto.StudentDtos.LinkGuardianRequest;
import br.com.mecadojo.api.people.api.dto.StudentDtos.LinkUserRequest;
import br.com.mecadojo.api.people.api.dto.StudentDtos.StudentListItemResponse;
import br.com.mecadojo.api.people.api.dto.StudentDtos.StudentResponse;
import br.com.mecadojo.api.people.api.dto.StudentDtos.UpdateStudentRequest;
import br.com.mecadojo.api.people.domain.StudentStatus;
import br.com.mecadojo.api.people.service.StudentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/students")
@Tag(name = "Students")
public class StudentController {
    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('STUDENT_READ')")
    PageResponse<StudentListItemResponse> list(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) StudentStatus status,
        @RequestParam(required = false) UUID unitId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return studentService.list(search, status, unitId, page, size);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('STUDENT_CREATE')")
    StudentResponse create(@Valid @RequestBody CreateStudentRequest request) {
        return studentService.create(request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('STUDENT_READ')")
    StudentResponse get(@PathVariable UUID id) {
        return studentService.get(id);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('STUDENT_UPDATE')")
    StudentResponse update(@PathVariable UUID id, @RequestBody UpdateStudentRequest request) {
        return studentService.update(id, request);
    }

    @PutMapping("/{id}/user")
    @PreAuthorize("hasAuthority('STUDENT_UPDATE')")
    StudentResponse linkUser(@PathVariable UUID id, @RequestBody LinkUserRequest request) {
        return studentService.linkUser(id, request.userId());
    }

    @PutMapping("/{id}/emergency-contact")
    @PreAuthorize("hasAuthority('STUDENT_UPDATE')")
    StudentResponse upsertEmergencyContact(@PathVariable UUID id, @Valid @RequestBody EmergencyContactRequest request) {
        return studentService.upsertEmergencyContact(id, request);
    }

    @DeleteMapping("/{id}/emergency-contact")
    @PreAuthorize("hasAuthority('STUDENT_UPDATE')")
    void deleteEmergencyContact(@PathVariable UUID id) {
        studentService.deleteEmergencyContact(id);
    }

    @PostMapping("/{id}/guardians")
    @PreAuthorize("hasAuthority('GUARDIAN_UPDATE')")
    StudentResponse linkGuardian(@PathVariable UUID id, @Valid @RequestBody LinkGuardianRequest request) {
        return studentService.linkGuardian(id, request);
    }

    @PostMapping("/{id}/guardians/new")
    @PreAuthorize("hasAuthority('GUARDIAN_CREATE')")
    StudentResponse createGuardianAndLink(@PathVariable UUID id, @Valid @RequestBody CreateGuardianForStudentRequest request) {
        return studentService.createGuardianAndLink(id, request);
    }

    @DeleteMapping("/{studentId}/guardians/{guardianId}")
    @PreAuthorize("hasAuthority('GUARDIAN_UPDATE')")
    void unlinkGuardian(@PathVariable UUID studentId, @PathVariable UUID guardianId) {
        studentService.unlinkGuardian(studentId, guardianId);
    }
}

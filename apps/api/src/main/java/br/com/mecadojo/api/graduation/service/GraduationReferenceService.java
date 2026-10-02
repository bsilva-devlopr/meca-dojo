package br.com.mecadojo.api.graduation.service;

import br.com.mecadojo.api.identity.security.CurrentUser;
import br.com.mecadojo.api.people.domain.StaffProfile;
import br.com.mecadojo.api.people.domain.StaffStatus;
import br.com.mecadojo.api.people.domain.StaffType;
import br.com.mecadojo.api.people.domain.Student;
import br.com.mecadojo.api.people.repository.StaffProfileRepository;
import br.com.mecadojo.api.people.repository.StudentRepository;
import br.com.mecadojo.api.shared.exception.NotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class GraduationReferenceService {
    private final StudentRepository studentRepository;
    private final StaffProfileRepository staffProfileRepository;

    public GraduationReferenceService(StudentRepository studentRepository, StaffProfileRepository staffProfileRepository) {
        this.studentRepository = studentRepository;
        this.staffProfileRepository = staffProfileRepository;
    }

    public Student student(UUID studentId) {
        return studentRepository.findByIdAndTenantId(studentId, CurrentUser.get().tenantId())
            .orElseThrow(() -> new NotFoundException("Student not found"));
    }

    public StaffProfile awardingStaff(UUID staffId) {
        if (staffId == null) {
            return null;
        }
        StaffProfile staff = staffProfileRepository.findByIdAndTenantId(staffId, CurrentUser.get().tenantId())
            .orElseThrow(() -> new NotFoundException("Staff profile not found"));
        boolean allowedType = staff.getType() == StaffType.PROFESSOR || staff.getType() == StaffType.INSTRUCTOR;
        if (staff.getStatus() != StaffStatus.ACTIVE || !allowedType) {
            throw new NotFoundException("Staff profile not found");
        }
        return staff;
    }
}

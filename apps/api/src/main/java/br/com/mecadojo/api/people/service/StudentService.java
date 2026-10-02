package br.com.mecadojo.api.people.service;

import br.com.mecadojo.api.identity.domain.Unit;
import br.com.mecadojo.api.identity.domain.User;
import br.com.mecadojo.api.identity.security.CurrentUser;
import br.com.mecadojo.api.identity.service.SlugNormalizer;
import br.com.mecadojo.api.people.api.dto.PageResponse;
import br.com.mecadojo.api.people.api.dto.StudentDtos.CreateGuardianForStudentRequest;
import br.com.mecadojo.api.people.api.dto.StudentDtos.CreateStudentRequest;
import br.com.mecadojo.api.people.api.dto.StudentDtos.EmergencyContactRequest;
import br.com.mecadojo.api.people.api.dto.StudentDtos.EmergencyContactResponse;
import br.com.mecadojo.api.people.api.dto.StudentDtos.LinkGuardianRequest;
import br.com.mecadojo.api.people.api.dto.StudentDtos.StudentGuardianResponse;
import br.com.mecadojo.api.people.api.dto.StudentDtos.StudentListItemResponse;
import br.com.mecadojo.api.people.api.dto.StudentDtos.StudentResponse;
import br.com.mecadojo.api.people.api.dto.StudentDtos.UpdateStudentRequest;
import br.com.mecadojo.api.people.domain.Guardian;
import br.com.mecadojo.api.people.domain.GuardianStudent;
import br.com.mecadojo.api.people.domain.Student;
import br.com.mecadojo.api.people.domain.StudentEmergencyContact;
import br.com.mecadojo.api.people.domain.StudentStatus;
import br.com.mecadojo.api.people.repository.GuardianRepository;
import br.com.mecadojo.api.people.repository.GuardianStudentRepository;
import br.com.mecadojo.api.people.repository.StudentEmergencyContactRepository;
import br.com.mecadojo.api.people.repository.StudentRepository;
import br.com.mecadojo.api.shared.exception.ConflictException;
import br.com.mecadojo.api.shared.exception.NotFoundException;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentService {
    private final StudentRepository studentRepository;
    private final StudentEmergencyContactRepository emergencyContactRepository;
    private final GuardianRepository guardianRepository;
    private final GuardianStudentRepository guardianStudentRepository;
    private final PeopleReferenceService references;

    public StudentService(
        StudentRepository studentRepository,
        StudentEmergencyContactRepository emergencyContactRepository,
        GuardianRepository guardianRepository,
        GuardianStudentRepository guardianStudentRepository,
        PeopleReferenceService references
    ) {
        this.studentRepository = studentRepository;
        this.emergencyContactRepository = emergencyContactRepository;
        this.guardianRepository = guardianRepository;
        this.guardianStudentRepository = guardianStudentRepository;
        this.references = references;
    }

    @Transactional(readOnly = true)
    public PageResponse<StudentListItemResponse> list(String search, StudentStatus status, UUID unitId, int page, int size) {
        if (unitId != null) {
            references.unit(unitId);
        }
        return PeopleMapper.page(studentRepository.findAll(spec(search, status, unitId), PageRequest.of(page, Math.min(size, 100), Sort.by("fullName").ascending()))
            .map(this::toListItem));
    }

    @Transactional
    public StudentResponse create(CreateStudentRequest request) {
        Unit unit = references.unit(request.primaryUnitId());
        Student student = new Student(references.currentTenant(), unit, request.fullName().trim(), request.joinDate(), request.status());
        applyStudentFields(student, request.userId(), request.preferredName(), request.birthDate(), request.email(), request.phone(), request.notes());
        return toResponse(studentRepository.save(student));
    }

    @Transactional(readOnly = true)
    public StudentResponse get(UUID id) {
        return toResponse(load(id));
    }

    @Transactional
    public StudentResponse update(UUID id, UpdateStudentRequest request) {
        Student student = load(id);
        if (request.primaryUnitId() != null) {
            student.setPrimaryUnit(references.unit(request.primaryUnitId()));
        }
        if (request.fullName() != null) {
            student.setFullName(request.fullName().trim());
        }
        if (request.status() != null) {
            student.setStatus(request.status());
        }
        if (request.joinDate() != null) {
            student.setJoinDate(request.joinDate());
        }
        applyStudentFields(student, request.userId(), request.preferredName(), request.birthDate(), request.email(), request.phone(), request.notes());
        return toResponse(studentRepository.save(student));
    }

    @Transactional
    public StudentResponse linkUser(UUID id, UUID userId) {
        Student student = load(id);
        student.setUser(userId == null ? null : references.user(userId));
        return toResponse(studentRepository.save(student));
    }

    @Transactional
    public StudentResponse upsertEmergencyContact(UUID id, EmergencyContactRequest request) {
        Student student = load(id);
        StudentEmergencyContact contact = emergencyContactRepository.findByStudentId(student.getId())
            .orElseGet(() -> new StudentEmergencyContact(student, request.name().trim(), request.relationship().trim(), request.phone().trim()));
        contact.setName(request.name().trim());
        contact.setRelationship(request.relationship().trim());
        contact.setPhone(request.phone().trim());
        emergencyContactRepository.save(contact);
        return toResponse(student);
    }

    @Transactional
    public void deleteEmergencyContact(UUID id) {
        Student student = load(id);
        emergencyContactRepository.deleteByStudentId(student.getId());
    }

    @Transactional
    public StudentResponse linkGuardian(UUID studentId, LinkGuardianRequest request) {
        Student student = load(studentId);
        Guardian guardian = guardianRepository.findByIdAndTenantId(request.guardianId(), CurrentUser.get().tenantId())
            .orElseThrow(() -> new NotFoundException("Guardian not found"));
        return saveGuardianLink(student, guardian, request.relationship(), request.primary());
    }

    @Transactional
    public StudentResponse createGuardianAndLink(UUID studentId, CreateGuardianForStudentRequest request) {
        Student student = load(studentId);
        Guardian guardian = new Guardian(references.currentTenant(), request.fullName().trim(), request.phone().trim());
        guardian.setEmail(normalizeOptionalEmail(request.email()));
        guardianRepository.save(guardian);
        return saveGuardianLink(student, guardian, request.relationship(), request.primary());
    }

    @Transactional
    public void unlinkGuardian(UUID studentId, UUID guardianId) {
        Student student = load(studentId);
        Guardian guardian = guardianRepository.findByIdAndTenantId(guardianId, CurrentUser.get().tenantId())
            .orElseThrow(() -> new NotFoundException("Guardian not found"));
        guardianStudentRepository.deleteById(new br.com.mecadojo.api.people.domain.GuardianStudentId(guardian.getId(), student.getId()));
    }

    private StudentResponse saveGuardianLink(Student student, Guardian guardian, br.com.mecadojo.api.people.domain.GuardianRelationship relationship, boolean primary) {
        GuardianStudent link = guardianStudentRepository.findByGuardianIdAndStudentId(guardian.getId(), student.getId())
            .orElseGet(() -> new GuardianStudent(guardian, student, relationship, primary));
        link.setRelationship(relationship);
        if (primary) {
            guardianStudentRepository.findAllByStudentId(student.getId()).forEach(existing -> {
                existing.setPrimary(false);
                guardianStudentRepository.save(existing);
            });
            guardianStudentRepository.flush();
        }
        link.setPrimary(primary);
        guardianStudentRepository.save(link);
        return toResponse(student);
    }

    private Student load(UUID id) {
        return studentRepository.findByIdAndTenantId(id, CurrentUser.get().tenantId())
            .orElseThrow(() -> new NotFoundException("Student not found"));
    }

    private void applyStudentFields(Student student, UUID userId, String preferredName, java.time.LocalDate birthDate, String email, String phone, String notes) {
        if (userId != null) {
            student.setUser(references.user(userId));
        }
        student.setPreferredName(blankToNull(preferredName));
        student.setBirthDate(birthDate);
        student.setEmail(normalizeOptionalEmail(email));
        student.setPhone(blankToNull(phone));
        student.setNotes(blankToNull(notes));
    }

    private Specification<Student> spec(String search, StudentStatus status, UUID unitId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("tenant").get("id"), CurrentUser.get().tenantId()));
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (unitId != null) {
                predicates.add(cb.equal(root.get("primaryUnit").get("id"), unitId));
            }
            if (search != null && !search.isBlank()) {
                String term = "%" + search.trim().toLowerCase(java.util.Locale.ROOT) + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("fullName")), term),
                    cb.like(cb.lower(root.get("preferredName")), term),
                    cb.like(cb.lower(root.get("email")), term)
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private StudentListItemResponse toListItem(Student student) {
        return new StudentListItemResponse(student.getId(), student.getFullName(), student.getPreferredName(), student.getStatus(), student.getJoinDate(), PeopleMapper.unit(student.getPrimaryUnit()));
    }

    private StudentResponse toResponse(Student student) {
        EmergencyContactResponse contact = emergencyContactRepository.findByStudentId(student.getId())
            .map(value -> new EmergencyContactResponse(value.getId(), value.getName(), value.getRelationship(), value.getPhone()))
            .orElse(null);
        List<StudentGuardianResponse> guardians = guardianStudentRepository.findAllByStudentId(student.getId()).stream()
            .map(link -> new StudentGuardianResponse(
                link.getGuardian().getId(),
                link.getGuardian().getFullName(),
                link.getGuardian().getEmail(),
                link.getGuardian().getPhone(),
                link.getRelationship(),
                link.isPrimary()
            ))
            .toList();
        return new StudentResponse(
            student.getId(),
            student.getFullName(),
            student.getPreferredName(),
            student.getBirthDate(),
            student.getEmail(),
            student.getPhone(),
            student.getStatus(),
            student.getJoinDate(),
            PeopleMapper.unit(student.getPrimaryUnit()),
            PeopleMapper.user(student.getUser()),
            contact,
            guardians,
            student.getNotes()
        );
    }

    private String normalizeOptionalEmail(String value) {
        String email = SlugNormalizer.email(value);
        return email.isBlank() ? null : email;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

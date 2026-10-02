package br.com.mecadojo.api.graduation.service;

import br.com.mecadojo.api.graduation.api.dto.GraduationDtos.BeltPromotionRequest;
import br.com.mecadojo.api.graduation.api.dto.GraduationDtos.CorrectionRequest;
import br.com.mecadojo.api.graduation.api.dto.GraduationDtos.CurrentGraduationResponse;
import br.com.mecadojo.api.graduation.api.dto.GraduationDtos.DegreePromotionRequest;
import br.com.mecadojo.api.graduation.api.dto.GraduationDtos.GraduationEventResponse;
import br.com.mecadojo.api.graduation.api.dto.GraduationDtos.InitialGraduationRequest;
import br.com.mecadojo.api.graduation.domain.Belt;
import br.com.mecadojo.api.graduation.domain.GraduationType;
import br.com.mecadojo.api.graduation.domain.StudentGraduation;
import br.com.mecadojo.api.graduation.repository.StudentGraduationRepository;
import br.com.mecadojo.api.identity.domain.User;
import br.com.mecadojo.api.identity.repository.UserRepository;
import br.com.mecadojo.api.identity.security.CurrentUser;
import br.com.mecadojo.api.people.api.dto.PageResponse;
import br.com.mecadojo.api.people.domain.StaffProfile;
import br.com.mecadojo.api.people.domain.Student;
import br.com.mecadojo.api.people.service.PeopleReferenceService;
import br.com.mecadojo.api.shared.exception.BadRequestException;
import br.com.mecadojo.api.shared.exception.ConflictException;
import br.com.mecadojo.api.shared.exception.NotFoundException;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GraduationService {
    private final StudentGraduationRepository graduationRepository;
    private final UserRepository userRepository;
    private final PeopleReferenceService peopleReferences;
    private final GraduationReferenceService graduationReferences;
    private final BeltResolver beltResolver;
    private final GraduationEligibilityService eligibilityService;

    public GraduationService(
        StudentGraduationRepository graduationRepository,
        UserRepository userRepository,
        PeopleReferenceService peopleReferences,
        GraduationReferenceService graduationReferences,
        BeltResolver beltResolver,
        GraduationEligibilityService eligibilityService
    ) {
        this.graduationRepository = graduationRepository;
        this.userRepository = userRepository;
        this.peopleReferences = peopleReferences;
        this.graduationReferences = graduationReferences;
        this.beltResolver = beltResolver;
        this.eligibilityService = eligibilityService;
    }

    @Transactional(readOnly = true)
    public CurrentGraduationResponse current(UUID studentId) {
        graduationReferences.student(studentId);
        StudentGraduation current = graduationRepository.findFirstByTenantIdAndStudentIdOrderByCreatedAtDesc(CurrentUser.get().tenantId(), studentId)
            .orElse(null);
        return new CurrentGraduationResponse(current == null ? null : GraduationMapper.event(current), eligibilityService.evaluate(current));
    }

    @Transactional(readOnly = true)
    public PageResponse<GraduationEventResponse> history(UUID studentId, int page, int size) {
        graduationReferences.student(studentId);
        var result = graduationRepository.findByTenantIdAndStudentId(
            CurrentUser.get().tenantId(),
            studentId,
            PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt").descending())
        ).map(GraduationMapper::event);
        return new PageResponse<>(result.getContent(), result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @Transactional
    public GraduationEventResponse initial(UUID studentId, InitialGraduationRequest request) {
        Student student = graduationReferences.student(studentId);
        if (graduationRepository.existsByTenantIdAndStudentIdAndType(CurrentUser.get().tenantId(), studentId, GraduationType.INITIAL)) {
            throw new ConflictException("Initial graduation already registered");
        }
        if (!currentForWrite(studentId).isEmpty()) {
            throw new ConflictException("Student already has graduation history");
        }
        Belt belt = beltResolver.resolve(request.beltCode(), request.system());
        validateDegree(belt, request.degree());
        return save(student, belt, request.degree(), request.graduatedAt(), request.awardedByStaffId(), GraduationType.INITIAL, request.notes(), null, null);
    }

    @Transactional
    public GraduationEventResponse promoteDegree(UUID studentId, DegreePromotionRequest request) {
        Student student = graduationReferences.student(studentId);
        StudentGraduation current = requireCurrent(currentForWrite(studentId));
        int nextDegree = current.getDegree() + 1;
        validateDegree(current.getBelt(), nextDegree);
        return save(student, current.getBelt(), nextDegree, request.graduatedAt(), request.awardedByStaffId(), GraduationType.DEGREE_PROMOTION, request.notes(), null, null);
    }

    @Transactional
    public GraduationEventResponse promoteBelt(UUID studentId, BeltPromotionRequest request) {
        Student student = graduationReferences.student(studentId);
        StudentGraduation current = requireCurrent(currentForWrite(studentId));
        Belt target = beltResolver.resolve(request.beltCode(), request.system() == null ? current.getBelt().getSystem() : request.system());
        if (target.getSystem() != current.getBelt().getSystem() || target.getSequence() != current.getBelt().getSequence() + 1) {
            throw new BadRequestException("Invalid belt progression");
        }
        return save(student, target, 0, request.graduatedAt(), request.awardedByStaffId(), GraduationType.BELT_PROMOTION, request.notes(), null, null);
    }

    @Transactional
    public GraduationEventResponse correction(UUID studentId, CorrectionRequest request) {
        Student student = graduationReferences.student(studentId);
        var history = currentForWrite(studentId);
        StudentGraduation current = history.isEmpty() ? null : history.getFirst();
        StudentGraduation corrects = request.correctsGraduationId() == null
            ? current
            : graduationRepository.findByIdAndTenantIdAndStudentId(request.correctsGraduationId(), CurrentUser.get().tenantId(), studentId)
                .orElseThrow(() -> new NotFoundException("Graduation event not found"));
        if (corrects == null) {
            throw new BadRequestException("Correction requires an existing graduation event");
        }
        Belt belt = beltResolver.resolve(request.beltCode(), request.system() == null ? corrects.getBelt().getSystem() : request.system());
        validateDegree(belt, request.degree());
        return save(student, belt, request.degree(), request.graduatedAt(), request.awardedByStaffId(), GraduationType.CORRECTION, request.notes(), corrects, request.reason().trim());
    }

    private java.util.List<StudentGraduation> currentForWrite(UUID studentId) {
        return graduationRepository.findByTenantIdAndStudentIdOrderByCreatedAtDesc(CurrentUser.get().tenantId(), studentId);
    }

    private StudentGraduation requireCurrent(java.util.List<StudentGraduation> history) {
        if (history.isEmpty()) {
            throw new BadRequestException("Student has no current graduation");
        }
        return history.getFirst();
    }

    private GraduationEventResponse save(Student student, Belt belt, int degree, java.time.LocalDate graduatedAt, UUID awardedByStaffId, GraduationType type, String notes, StudentGraduation corrects, String correctionReason) {
        StaffProfile awardedBy = graduationReferences.awardingStaff(awardedByStaffId);
        User createdBy = userRepository.findByIdAndTenantId(CurrentUser.get().userId(), CurrentUser.get().tenantId())
            .orElseThrow(() -> new NotFoundException("User not found"));
        StudentGraduation graduation = new StudentGraduation(
            peopleReferences.currentTenant(),
            student,
            belt,
            degree,
            graduatedAt,
            awardedBy,
            type,
            blankToNull(notes),
            createdBy
        );
        if (type == GraduationType.CORRECTION) {
            graduation.markCorrection(corrects, correctionReason);
        }
        return GraduationMapper.event(graduationRepository.save(graduation));
    }

    private void validateDegree(Belt belt, int degree) {
        if (degree < 0 || degree > belt.getMaxDegree()) {
            throw new BadRequestException("Degree is outside the allowed range for this belt");
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

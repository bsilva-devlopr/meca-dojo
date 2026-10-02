package br.com.mecadojo.api.graduation.service;

import br.com.mecadojo.api.graduation.api.dto.GraduationDtos.GraduationPolicyResponse;
import br.com.mecadojo.api.graduation.api.dto.GraduationDtos.UpdateGraduationPolicyRequest;
import br.com.mecadojo.api.graduation.domain.Belt;
import br.com.mecadojo.api.graduation.domain.GraduationPolicy;
import br.com.mecadojo.api.graduation.repository.GraduationPolicyRepository;
import br.com.mecadojo.api.identity.security.CurrentUser;
import br.com.mecadojo.api.people.service.PeopleReferenceService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GraduationPolicyService {
    private final GraduationPolicyRepository policyRepository;
    private final PeopleReferenceService peopleReferences;
    private final BeltResolver beltResolver;

    public GraduationPolicyService(GraduationPolicyRepository policyRepository, PeopleReferenceService peopleReferences, BeltResolver beltResolver) {
        this.policyRepository = policyRepository;
        this.peopleReferences = peopleReferences;
        this.beltResolver = beltResolver;
    }

    @Transactional(readOnly = true)
    public List<GraduationPolicyResponse> list() {
        return policyRepository.findAllByTenantIdOrderByBeltSystemAscBeltSequenceAsc(CurrentUser.get().tenantId()).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public GraduationPolicyResponse update(String beltCode, br.com.mecadojo.api.graduation.domain.BeltSystem system, UpdateGraduationPolicyRequest request) {
        Belt belt = beltResolver.resolve(beltCode, system);
        GraduationPolicy policy = policyRepository.findByTenantIdAndBeltId(CurrentUser.get().tenantId(), belt.getId())
            .orElseGet(() -> new GraduationPolicy(peopleReferences.currentTenant(), belt));
        policy.setMinimumTrainingCount(request.minimumTrainingCount());
        policy.setMinimumDaysInGrade(request.minimumDaysInGrade());
        if (request.active() != null) {
            policy.setActive(request.active());
        }
        return toResponse(policyRepository.save(policy));
    }

    private GraduationPolicyResponse toResponse(GraduationPolicy policy) {
        return new GraduationPolicyResponse(
            policy.getId(),
            GraduationMapper.belt(policy.getBelt()),
            policy.getMinimumTrainingCount(),
            policy.getMinimumDaysInGrade(),
            policy.isActive()
        );
    }
}

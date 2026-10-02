package br.com.mecadojo.api.people.service;

import br.com.mecadojo.api.identity.security.CurrentUser;
import br.com.mecadojo.api.identity.service.SlugNormalizer;
import br.com.mecadojo.api.people.api.dto.GuardianDtos.CreateGuardianRequest;
import br.com.mecadojo.api.people.api.dto.GuardianDtos.GuardianResponse;
import br.com.mecadojo.api.people.api.dto.GuardianDtos.UpdateGuardianRequest;
import br.com.mecadojo.api.people.api.dto.PageResponse;
import br.com.mecadojo.api.people.domain.Guardian;
import br.com.mecadojo.api.people.repository.GuardianRepository;
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
public class GuardianService {
    private final GuardianRepository guardianRepository;
    private final PeopleReferenceService references;

    public GuardianService(GuardianRepository guardianRepository, PeopleReferenceService references) {
        this.guardianRepository = guardianRepository;
        this.references = references;
    }

    @Transactional(readOnly = true)
    public PageResponse<GuardianResponse> list(String search, int page, int size) {
        return PeopleMapper.page(guardianRepository.findAll(spec(search), PageRequest.of(page, Math.min(size, 100), Sort.by("fullName").ascending()))
            .map(this::toResponse));
    }

    @Transactional
    public GuardianResponse create(CreateGuardianRequest request) {
        Guardian guardian = new Guardian(references.currentTenant(), request.fullName().trim(), request.phone().trim());
        guardian.setEmail(normalizeOptionalEmail(request.email()));
        if (request.userId() != null) {
            guardian.setUser(references.user(request.userId()));
        }
        return toResponse(guardianRepository.save(guardian));
    }

    @Transactional(readOnly = true)
    public GuardianResponse get(UUID id) {
        return toResponse(load(id));
    }

    @Transactional
    public GuardianResponse update(UUID id, UpdateGuardianRequest request) {
        Guardian guardian = load(id);
        if (request.userId() != null) {
            guardian.setUser(references.user(request.userId()));
        }
        if (request.fullName() != null) {
            guardian.setFullName(request.fullName().trim());
        }
        if (request.email() != null) {
            guardian.setEmail(normalizeOptionalEmail(request.email()));
        }
        if (request.phone() != null) {
            guardian.setPhone(request.phone().trim());
        }
        return toResponse(guardianRepository.save(guardian));
    }

    @Transactional
    public GuardianResponse linkUser(UUID id, UUID userId) {
        Guardian guardian = load(id);
        guardian.setUser(userId == null ? null : references.user(userId));
        return toResponse(guardianRepository.save(guardian));
    }

    private Guardian load(UUID id) {
        return guardianRepository.findByIdAndTenantId(id, CurrentUser.get().tenantId())
            .orElseThrow(() -> new NotFoundException("Guardian not found"));
    }

    private Specification<Guardian> spec(String search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("tenant").get("id"), CurrentUser.get().tenantId()));
            if (search != null && !search.isBlank()) {
                String term = "%" + search.trim().toLowerCase(java.util.Locale.ROOT) + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("fullName")), term),
                    cb.like(cb.lower(root.get("email")), term),
                    cb.like(cb.lower(root.get("phone")), term)
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private GuardianResponse toResponse(Guardian guardian) {
        return new GuardianResponse(guardian.getId(), guardian.getFullName(), guardian.getEmail(), guardian.getPhone(), PeopleMapper.user(guardian.getUser()));
    }

    private String normalizeOptionalEmail(String value) {
        String email = SlugNormalizer.email(value);
        return email.isBlank() ? null : email;
    }
}

package br.com.mecadojo.api.schedule.service;

import br.com.mecadojo.api.identity.security.CurrentUser;
import br.com.mecadojo.api.people.api.dto.PageResponse;
import br.com.mecadojo.api.people.service.PeopleMapper;
import br.com.mecadojo.api.people.service.PeopleReferenceService;
import br.com.mecadojo.api.schedule.api.dto.ScheduleDtos.ClassResponse;
import br.com.mecadojo.api.schedule.api.dto.ScheduleDtos.CreateClassRequest;
import br.com.mecadojo.api.schedule.api.dto.ScheduleDtos.UpdateClassRequest;
import br.com.mecadojo.api.schedule.domain.ClassAudience;
import br.com.mecadojo.api.schedule.domain.ClassLevel;
import br.com.mecadojo.api.schedule.domain.ClassModality;
import br.com.mecadojo.api.schedule.domain.ClassStatus;
import br.com.mecadojo.api.schedule.domain.ClassType;
import br.com.mecadojo.api.schedule.domain.DojoClass;
import br.com.mecadojo.api.schedule.repository.DojoClassRepository;
import br.com.mecadojo.api.shared.exception.BadRequestException;
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
public class DojoClassService {
    private final DojoClassRepository dojoClassRepository;
    private final PeopleReferenceService references;

    public DojoClassService(DojoClassRepository dojoClassRepository, PeopleReferenceService references) {
        this.dojoClassRepository = dojoClassRepository;
        this.references = references;
    }

    @Transactional(readOnly = true)
    public PageResponse<ClassResponse> list(String search, ClassStatus status, ClassModality modality, ClassAudience audience, ClassType classType, ClassLevel level, int page, int size) {
        return PeopleMapper.page(dojoClassRepository.findAll(spec(search, status, modality, audience, classType, level), PageRequest.of(page, Math.min(size, 100), Sort.by("name").ascending())).map(this::toResponse));
    }

    @Transactional
    public ClassResponse create(CreateClassRequest request) {
        validateCapacity(request.defaultCapacity());
        DojoClass dojoClass = new DojoClass(references.currentTenant(), request.name().trim(), request.modality(), request.audience(), request.classType(), request.level());
        dojoClass.setDescription(blankToNull(request.description()));
        dojoClass.setStatus(request.status() == null ? ClassStatus.ACTIVE : request.status());
        dojoClass.setDefaultCapacity(request.defaultCapacity());
        return toResponse(dojoClassRepository.save(dojoClass));
    }

    @Transactional(readOnly = true)
    public ClassResponse get(UUID id) {
        return toResponse(load(id));
    }

    @Transactional
    public ClassResponse update(UUID id, UpdateClassRequest request) {
        DojoClass dojoClass = load(id);
        if (request.name() != null) {
            if (request.name().isBlank()) throw new BadRequestException("Class name is required");
            dojoClass.setName(request.name().trim());
        }
        if (request.description() != null) dojoClass.setDescription(blankToNull(request.description()));
        if (request.modality() != null) dojoClass.setModality(request.modality());
        if (request.audience() != null) dojoClass.setAudience(request.audience());
        if (request.classType() != null) dojoClass.setClassType(request.classType());
        if (request.level() != null) dojoClass.setLevel(request.level());
        if (request.status() != null) dojoClass.setStatus(request.status());
        if (request.defaultCapacity() != null) {
            validateCapacity(request.defaultCapacity());
            dojoClass.setDefaultCapacity(request.defaultCapacity());
        }
        return toResponse(dojoClassRepository.save(dojoClass));
    }

    public DojoClass load(UUID id) {
        return dojoClassRepository.findByIdAndTenantId(id, CurrentUser.get().tenantId()).orElseThrow(() -> new NotFoundException("Class not found"));
    }

    ClassResponse toResponse(DojoClass dojoClass) {
        return new ClassResponse(dojoClass.getId(), dojoClass.getName(), dojoClass.getDescription(), dojoClass.getModality(), dojoClass.getAudience(), dojoClass.getClassType(), dojoClass.getLevel(), dojoClass.getStatus(), dojoClass.getDefaultCapacity());
    }

    private Specification<DojoClass> spec(String search, ClassStatus status, ClassModality modality, ClassAudience audience, ClassType classType, ClassLevel level) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("tenant").get("id"), CurrentUser.get().tenantId()));
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            if (modality != null) predicates.add(cb.equal(root.get("modality"), modality));
            if (audience != null) predicates.add(cb.equal(root.get("audience"), audience));
            if (classType != null) predicates.add(cb.equal(root.get("classType"), classType));
            if (level != null) predicates.add(cb.equal(root.get("level"), level));
            if (search != null && !search.isBlank()) {
                String term = "%" + search.trim().toLowerCase(java.util.Locale.ROOT) + "%";
                predicates.add(cb.or(cb.like(cb.lower(root.get("name")), term), cb.like(cb.lower(root.get("description")), term)));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private void validateCapacity(Integer capacity) {
        if (capacity != null && capacity < 1) throw new BadRequestException("Capacity must be positive");
    }

    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}

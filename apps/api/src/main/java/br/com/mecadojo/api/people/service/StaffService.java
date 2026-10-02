package br.com.mecadojo.api.people.service;

import br.com.mecadojo.api.identity.domain.Unit;
import br.com.mecadojo.api.identity.domain.User;
import br.com.mecadojo.api.identity.security.CurrentUser;
import br.com.mecadojo.api.people.api.dto.PageResponse;
import br.com.mecadojo.api.people.api.dto.StaffDtos.CreateStaffRequest;
import br.com.mecadojo.api.people.api.dto.StaffDtos.StaffResponse;
import br.com.mecadojo.api.people.api.dto.StaffDtos.UpdateStaffRequest;
import br.com.mecadojo.api.people.domain.StaffProfile;
import br.com.mecadojo.api.people.domain.StaffStatus;
import br.com.mecadojo.api.people.domain.StaffType;
import br.com.mecadojo.api.people.repository.StaffProfileRepository;
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
public class StaffService {
    private final StaffProfileRepository staffProfileRepository;
    private final PeopleReferenceService references;

    public StaffService(StaffProfileRepository staffProfileRepository, PeopleReferenceService references) {
        this.staffProfileRepository = staffProfileRepository;
        this.references = references;
    }

    @Transactional(readOnly = true)
    public PageResponse<StaffResponse> list(StaffType type, StaffStatus status, UUID unitId, String search, int page, int size) {
        if (unitId != null) {
            references.unit(unitId);
        }
        return PeopleMapper.page(staffProfileRepository.findAll(spec(type, status, unitId, search), PageRequest.of(page, Math.min(size, 100), Sort.by("displayName").ascending()))
            .map(this::toResponse));
    }

    @Transactional
    public StaffResponse create(CreateStaffRequest request) {
        User user = references.activeUser(request.userId());
        if (staffProfileRepository.existsByTenantIdAndUserIdAndType(CurrentUser.get().tenantId(), user.getId(), request.type())) {
            throw new ConflictException("Staff profile already exists for user and type");
        }
        StaffProfile staff = new StaffProfile(references.currentTenant(), user, request.type(), request.displayName().trim());
        if (request.primaryUnitId() != null) {
            staff.setPrimaryUnit(references.unit(request.primaryUnitId()));
        }
        return toResponse(staffProfileRepository.save(staff));
    }

    @Transactional(readOnly = true)
    public StaffResponse get(UUID id) {
        return toResponse(load(id));
    }

    @Transactional
    public StaffResponse update(UUID id, UpdateStaffRequest request) {
        StaffProfile staff = load(id);
        if (request.type() != null && request.type() != staff.getType()) {
            if (staffProfileRepository.existsByTenantIdAndUserIdAndType(CurrentUser.get().tenantId(), staff.getUser().getId(), request.type())) {
                throw new ConflictException("Staff profile already exists for user and type");
            }
            staff.setType(request.type());
        }
        if (request.status() != null) {
            staff.setStatus(request.status());
        }
        if (request.displayName() != null) {
            staff.setDisplayName(request.displayName().trim());
        }
        if (request.primaryUnitId() != null) {
            staff.setPrimaryUnit(references.unit(request.primaryUnitId()));
        }
        return toResponse(staffProfileRepository.save(staff));
    }

    private StaffProfile load(UUID id) {
        return staffProfileRepository.findByIdAndTenantId(id, CurrentUser.get().tenantId())
            .orElseThrow(() -> new NotFoundException("Staff profile not found"));
    }

    private Specification<StaffProfile> spec(StaffType type, StaffStatus status, UUID unitId, String search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("tenant").get("id"), CurrentUser.get().tenantId()));
            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (unitId != null) {
                predicates.add(cb.equal(root.get("primaryUnit").get("id"), unitId));
            }
            if (search != null && !search.isBlank()) {
                String term = "%" + search.trim().toLowerCase(java.util.Locale.ROOT) + "%";
                predicates.add(cb.like(cb.lower(root.get("displayName")), term));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private StaffResponse toResponse(StaffProfile staff) {
        Unit unit = staff.getPrimaryUnit();
        return new StaffResponse(staff.getId(), staff.getType(), staff.getStatus(), staff.getDisplayName(), PeopleMapper.user(staff.getUser()), PeopleMapper.unit(unit));
    }
}

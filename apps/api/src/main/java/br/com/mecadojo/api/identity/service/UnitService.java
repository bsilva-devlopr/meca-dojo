package br.com.mecadojo.api.identity.service;

import br.com.mecadojo.api.identity.api.dto.UnitDtos.CreateUnitRequest;
import br.com.mecadojo.api.identity.api.dto.UnitDtos.UnitResponse;
import br.com.mecadojo.api.identity.api.dto.UnitDtos.UpdateUnitRequest;
import br.com.mecadojo.api.identity.domain.Tenant;
import br.com.mecadojo.api.identity.domain.Unit;
import br.com.mecadojo.api.identity.repository.TenantRepository;
import br.com.mecadojo.api.identity.repository.UnitRepository;
import br.com.mecadojo.api.identity.security.CurrentUser;
import br.com.mecadojo.api.shared.exception.ConflictException;
import br.com.mecadojo.api.shared.exception.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UnitService {
    private final UnitRepository unitRepository;
    private final TenantRepository tenantRepository;

    public UnitService(UnitRepository unitRepository, TenantRepository tenantRepository) {
        this.unitRepository = unitRepository;
        this.tenantRepository = tenantRepository;
    }

    @Transactional(readOnly = true)
    public List<UnitResponse> list() {
        return unitRepository.findAllByTenantIdOrderByNameAsc(CurrentUser.get().tenantId()).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public UnitResponse create(CreateUnitRequest request) {
        UUID tenantId = CurrentUser.get().tenantId();
        String slug = SlugNormalizer.slug(request.slug());
        if (unitRepository.existsByTenantIdAndSlug(tenantId, slug)) {
            throw new ConflictException("Unit slug already exists");
        }
        Tenant tenant = tenantRepository.findById(tenantId).orElseThrow(() -> new NotFoundException("Tenant not found"));
        Unit unit = new Unit(tenant, request.name().trim(), slug);
        return toResponse(unitRepository.save(unit));
    }

    @Transactional(readOnly = true)
    public UnitResponse get(UUID id) {
        return toResponse(loadScoped(id));
    }

    @Transactional
    public UnitResponse update(UUID id, UpdateUnitRequest request) {
        Unit unit = loadScoped(id);
        if (request.name() != null) {
            unit.setName(request.name().trim());
        }
        if (request.status() != null) {
            unit.setStatus(request.status());
        }
        return toResponse(unitRepository.save(unit));
    }

    private Unit loadScoped(UUID id) {
        return unitRepository.findByIdAndTenantId(id, CurrentUser.get().tenantId())
            .orElseThrow(() -> new NotFoundException("Unit not found"));
    }

    private UnitResponse toResponse(Unit unit) {
        return new UnitResponse(unit.getId(), unit.getName(), unit.getSlug(), unit.getStatus());
    }
}

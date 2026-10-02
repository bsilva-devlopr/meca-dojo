package br.com.mecadojo.api.identity.service;

import br.com.mecadojo.api.identity.api.dto.TenantDtos.TenantResponse;
import br.com.mecadojo.api.identity.domain.Tenant;
import br.com.mecadojo.api.identity.repository.TenantRepository;
import br.com.mecadojo.api.identity.security.CurrentUser;
import br.com.mecadojo.api.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantService {
    private final TenantRepository tenantRepository;

    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Transactional(readOnly = true)
    public TenantResponse currentTenant() {
        return toResponse(loadCurrentTenant());
    }

    @Transactional
    public TenantResponse updateCurrentTenant(String name) {
        Tenant tenant = loadCurrentTenant();
        tenant.setName(name.trim());
        return toResponse(tenantRepository.save(tenant));
    }

    private Tenant loadCurrentTenant() {
        return tenantRepository.findById(CurrentUser.get().tenantId())
            .orElseThrow(() -> new NotFoundException("Tenant not found"));
    }

    private TenantResponse toResponse(Tenant tenant) {
        return new TenantResponse(tenant.getId(), tenant.getName(), tenant.getSlug(), tenant.getStatus().name());
    }
}

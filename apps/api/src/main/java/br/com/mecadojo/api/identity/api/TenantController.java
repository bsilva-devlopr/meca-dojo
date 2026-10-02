package br.com.mecadojo.api.identity.api;

import br.com.mecadojo.api.identity.api.dto.TenantDtos.TenantResponse;
import br.com.mecadojo.api.identity.api.dto.TenantDtos.UpdateTenantRequest;
import br.com.mecadojo.api.identity.service.TenantService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tenant")
@Tag(name = "Tenant")
public class TenantController {
    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('TENANT_READ')")
    TenantResponse get() {
        return tenantService.currentTenant();
    }

    @PatchMapping
    @PreAuthorize("hasAuthority('TENANT_UPDATE')")
    TenantResponse update(@Valid @RequestBody UpdateTenantRequest request) {
        return tenantService.updateCurrentTenant(request.name());
    }
}

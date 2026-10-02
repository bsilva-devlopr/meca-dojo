package br.com.mecadojo.api.identity.security;

import br.com.mecadojo.api.identity.domain.PermissionName;
import br.com.mecadojo.api.identity.domain.RoleName;
import java.util.Set;
import java.util.UUID;

public record AuthenticatedUser(
    UUID userId,
    UUID tenantId,
    Set<RoleName> roles,
    Set<PermissionName> permissions
) {
    public boolean hasRole(RoleName roleName) {
        return roles.contains(roleName);
    }
}

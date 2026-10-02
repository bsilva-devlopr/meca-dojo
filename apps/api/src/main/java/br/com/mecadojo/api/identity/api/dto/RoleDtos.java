package br.com.mecadojo.api.identity.api.dto;

import br.com.mecadojo.api.identity.domain.PermissionName;
import br.com.mecadojo.api.identity.domain.RoleName;
import java.util.Set;

public final class RoleDtos {
    private RoleDtos() {
    }

    public record RoleResponse(RoleName name, Set<PermissionName> permissions) {
    }
}

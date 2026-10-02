package br.com.mecadojo.api.identity.service;

import br.com.mecadojo.api.identity.domain.PermissionName;
import br.com.mecadojo.api.identity.domain.RoleName;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class RbacCatalog {
    public static final Map<RoleName, Set<PermissionName>> ROLE_PERMISSIONS = Map.of(
        RoleName.OWNER, EnumSet.allOf(PermissionName.class),
        RoleName.ADMIN, EnumSet.of(
            PermissionName.TENANT_READ,
            PermissionName.TENANT_UPDATE,
            PermissionName.UNIT_READ,
            PermissionName.UNIT_CREATE,
            PermissionName.UNIT_UPDATE,
            PermissionName.USER_READ,
            PermissionName.USER_CREATE,
            PermissionName.USER_UPDATE,
            PermissionName.ROLE_READ,
            PermissionName.ROLE_ASSIGN
        ),
        RoleName.PROFESSOR, EnumSet.noneOf(PermissionName.class),
        RoleName.INSTRUCTOR, EnumSet.noneOf(PermissionName.class),
        RoleName.RECEPTION, EnumSet.noneOf(PermissionName.class),
        RoleName.STUDENT, EnumSet.noneOf(PermissionName.class),
        RoleName.GUARDIAN, EnumSet.noneOf(PermissionName.class)
    );

    private RbacCatalog() {
    }
}

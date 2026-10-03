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
            PermissionName.ROLE_ASSIGN,
            PermissionName.STUDENT_READ,
            PermissionName.STUDENT_CREATE,
            PermissionName.STUDENT_UPDATE,
            PermissionName.GUARDIAN_READ,
            PermissionName.GUARDIAN_CREATE,
            PermissionName.GUARDIAN_UPDATE,
            PermissionName.STAFF_READ,
            PermissionName.STAFF_CREATE,
            PermissionName.STAFF_UPDATE,
            PermissionName.GRADUATION_READ,
            PermissionName.GRADUATION_CREATE,
            PermissionName.GRADUATION_CORRECT,
            PermissionName.GRADUATION_POLICY_READ,
            PermissionName.GRADUATION_POLICY_UPDATE,
            PermissionName.CLASS_READ,
            PermissionName.CLASS_CREATE,
            PermissionName.CLASS_UPDATE,
            PermissionName.SCHEDULE_READ,
            PermissionName.SCHEDULE_CREATE,
            PermissionName.SCHEDULE_UPDATE,
            PermissionName.TRAINING_SESSION_READ,
            PermissionName.TRAINING_SESSION_CREATE,
            PermissionName.TRAINING_SESSION_UPDATE,
            PermissionName.TRAINING_SESSION_OPEN,
            PermissionName.TRAINING_SESSION_COMPLETE,
            PermissionName.TRAINING_SESSION_CANCEL
        ),
        RoleName.PROFESSOR, EnumSet.of(
            PermissionName.STUDENT_READ,
            PermissionName.GUARDIAN_READ,
            PermissionName.GRADUATION_READ,
            PermissionName.GRADUATION_CREATE,
            PermissionName.CLASS_READ,
            PermissionName.SCHEDULE_READ,
            PermissionName.TRAINING_SESSION_READ,
            PermissionName.TRAINING_SESSION_CREATE,
            PermissionName.TRAINING_SESSION_UPDATE,
            PermissionName.TRAINING_SESSION_OPEN,
            PermissionName.TRAINING_SESSION_COMPLETE,
            PermissionName.TRAINING_SESSION_CANCEL
        ),
        RoleName.INSTRUCTOR, EnumSet.of(
            PermissionName.GRADUATION_READ,
            PermissionName.GRADUATION_CREATE,
            PermissionName.CLASS_READ,
            PermissionName.SCHEDULE_READ,
            PermissionName.TRAINING_SESSION_READ,
            PermissionName.TRAINING_SESSION_OPEN,
            PermissionName.TRAINING_SESSION_COMPLETE
        ),
        RoleName.RECEPTION, EnumSet.of(
            PermissionName.STUDENT_READ,
            PermissionName.STUDENT_CREATE,
            PermissionName.STUDENT_UPDATE,
            PermissionName.GUARDIAN_READ,
            PermissionName.GUARDIAN_CREATE,
            PermissionName.GUARDIAN_UPDATE,
            PermissionName.GRADUATION_READ,
            PermissionName.CLASS_READ,
            PermissionName.SCHEDULE_READ,
            PermissionName.TRAINING_SESSION_READ,
            PermissionName.TRAINING_SESSION_CREATE,
            PermissionName.TRAINING_SESSION_UPDATE,
            PermissionName.TRAINING_SESSION_CANCEL
        ),
        RoleName.STUDENT, EnumSet.noneOf(PermissionName.class),
        RoleName.GUARDIAN, EnumSet.noneOf(PermissionName.class)
    );

    private RbacCatalog() {
    }
}

package br.com.mecadojo.api.identity.service;

import br.com.mecadojo.api.identity.domain.PermissionName;
import br.com.mecadojo.api.identity.domain.RoleName;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class RbacCatalog {
    public static final Map<RoleName, Set<PermissionName>> ROLE_PERMISSIONS = Map.of(
        RoleName.OWNER, EnumSet.allOf(PermissionName.class),
        RoleName.ADMIN, EnumSet.allOf(PermissionName.class),
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
            PermissionName.TRAINING_SESSION_CANCEL,
            PermissionName.ATTENDANCE_READ,
            PermissionName.ATTENDANCE_CREATE,
            PermissionName.ATTENDANCE_VOID,
            PermissionName.CHECKIN_TOKEN_CREATE,
            PermissionName.CHECKIN_AUDIT_READ
        ),
        RoleName.INSTRUCTOR, EnumSet.of(
            PermissionName.GRADUATION_READ,
            PermissionName.GRADUATION_CREATE,
            PermissionName.CLASS_READ,
            PermissionName.SCHEDULE_READ,
            PermissionName.TRAINING_SESSION_READ,
            PermissionName.TRAINING_SESSION_OPEN,
            PermissionName.TRAINING_SESSION_COMPLETE,
            PermissionName.ATTENDANCE_READ,
            PermissionName.ATTENDANCE_CREATE,
            PermissionName.CHECKIN_TOKEN_CREATE
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
            PermissionName.TRAINING_SESSION_CANCEL,
            PermissionName.ATTENDANCE_READ,
            PermissionName.ATTENDANCE_CREATE,
            PermissionName.ATTENDANCE_VOID,
            PermissionName.CHECKIN_AUDIT_READ
        ),
        RoleName.STUDENT, EnumSet.noneOf(PermissionName.class),
        RoleName.GUARDIAN, EnumSet.noneOf(PermissionName.class)
    );

    private RbacCatalog() {
    }
}

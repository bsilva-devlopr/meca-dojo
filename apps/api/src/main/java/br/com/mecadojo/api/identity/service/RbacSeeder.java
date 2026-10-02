package br.com.mecadojo.api.identity.service;

import br.com.mecadojo.api.identity.domain.Permission;
import br.com.mecadojo.api.identity.domain.PermissionName;
import br.com.mecadojo.api.identity.domain.Role;
import br.com.mecadojo.api.identity.domain.RoleName;
import br.com.mecadojo.api.identity.repository.PermissionRepository;
import br.com.mecadojo.api.identity.repository.RoleRepository;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(1)
public class RbacSeeder implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RbacSeeder(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        for (PermissionName permissionName : PermissionName.values()) {
            permissionRepository.findByName(permissionName)
                .orElseGet(() -> permissionRepository.save(new Permission(permissionName)));
        }
        for (RoleName roleName : RoleName.values()) {
            roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(roleName)));
        }

        Map<PermissionName, Permission> permissions = permissionRepository.findAll().stream()
            .collect(Collectors.toMap(Permission::getName, Function.identity()));

        for (RoleName roleName : Arrays.asList(RoleName.values())) {
            Role role = roleRepository.findByName(roleName).orElseThrow();
            role.getPermissions().clear();
            RbacCatalog.ROLE_PERMISSIONS.get(roleName).stream()
                .map(permissions::get)
                .forEach(role.getPermissions()::add);
            roleRepository.save(role);
        }
    }
}

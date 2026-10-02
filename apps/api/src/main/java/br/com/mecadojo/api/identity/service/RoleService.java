package br.com.mecadojo.api.identity.service;

import br.com.mecadojo.api.identity.api.dto.RoleDtos.RoleResponse;
import br.com.mecadojo.api.identity.repository.RoleRepository;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> list() {
        return roleRepository.findAll().stream()
            .map(role -> new RoleResponse(
                role.getName(),
                role.getPermissions().stream().map(permission -> permission.getName()).collect(Collectors.toSet())
            ))
            .sorted(Comparator.comparing(response -> response.name().name()))
            .toList();
    }
}

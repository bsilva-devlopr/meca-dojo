package br.com.mecadojo.api.identity.service;

import br.com.mecadojo.api.identity.api.dto.UserDtos.CreateUserRequest;
import br.com.mecadojo.api.identity.api.dto.UserDtos.UpdateUserRequest;
import br.com.mecadojo.api.identity.api.dto.UserDtos.UpdateUserRolesRequest;
import br.com.mecadojo.api.identity.api.dto.UserDtos.UserResponse;
import br.com.mecadojo.api.identity.domain.Role;
import br.com.mecadojo.api.identity.domain.RoleName;
import br.com.mecadojo.api.identity.domain.Tenant;
import br.com.mecadojo.api.identity.domain.User;
import br.com.mecadojo.api.identity.domain.UserStatus;
import br.com.mecadojo.api.identity.repository.RoleRepository;
import br.com.mecadojo.api.identity.repository.TenantRepository;
import br.com.mecadojo.api.identity.repository.UserRepository;
import br.com.mecadojo.api.identity.security.CurrentUser;
import br.com.mecadojo.api.shared.exception.ConflictException;
import br.com.mecadojo.api.shared.exception.NotFoundException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
        UserRepository userRepository,
        TenantRepository tenantRepository,
        RoleRepository roleRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> list() {
        return userRepository.findAllByTenantIdOrderByNameAsc(CurrentUser.get().tenantId()).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        UUID tenantId = CurrentUser.get().tenantId();
        String email = SlugNormalizer.email(request.email());
        if (userRepository.existsByTenantIdAndEmail(tenantId, email)) {
            throw new ConflictException("User email already exists");
        }
        Tenant tenant = tenantRepository.findById(tenantId).orElseThrow(() -> new NotFoundException("Tenant not found"));
        Set<Role> roles = loadRoles(request.roles());
        User user = new User(tenant, request.name().trim(), email, passwordEncoder.encode(request.password()), roles);
        return toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse get(UUID id) {
        return toResponse(loadScoped(id));
    }

    @Transactional
    public UserResponse update(UUID id, UpdateUserRequest request) {
        User user = loadScoped(id);
        if (request.status() == UserStatus.INACTIVE || request.status() == UserStatus.LOCKED) {
            ensureCanDeactivate(user);
        }
        if (request.name() != null) {
            user.setName(request.name().trim());
        }
        if (request.email() != null) {
            String email = SlugNormalizer.email(request.email());
            if (!email.equals(user.getEmail()) && userRepository.existsByTenantIdAndEmail(CurrentUser.get().tenantId(), email)) {
                throw new ConflictException("User email already exists");
            }
            user.setEmail(email);
        }
        if (request.status() != null) {
            user.setStatus(request.status());
        }
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateRoles(UUID id, UpdateUserRolesRequest request) {
        User user = loadScoped(id);
        Set<RoleName> newRoleNames = request.roles();
        ensureCanReplaceRoles(user, newRoleNames);
        user.replaceRoles(loadRoles(newRoleNames));
        return toResponse(userRepository.save(user));
    }

    private void ensureCanReplaceRoles(User user, Set<RoleName> resultingRoles) {
        boolean removingOwner = user.getRoles().stream().anyMatch(role -> role.getName() == RoleName.OWNER)
            && !resultingRoles.contains(RoleName.OWNER);
        if (!removingOwner) {
            return;
        }
        ensureAnotherActiveOwnerExists();
    }

    private void ensureCanDeactivate(User user) {
        boolean deactivatingOwner = user.getStatus() == UserStatus.ACTIVE
            && user.getRoles().stream().anyMatch(role -> role.getName() == RoleName.OWNER);
        if (!deactivatingOwner) {
            return;
        }
        ensureAnotherActiveOwnerExists();
    }

    private void ensureAnotherActiveOwnerExists() {
        long activeOwners = userRepository.findAllByTenantIdOrderByNameAsc(CurrentUser.get().tenantId()).stream()
            .filter(candidate -> candidate.getStatus() == UserStatus.ACTIVE)
            .filter(candidate -> candidate.getRoles().stream().anyMatch(role -> role.getName() == RoleName.OWNER))
            .count();
        if (activeOwners <= 1) {
            throw new ConflictException("Tenant must keep at least one active OWNER");
        }
    }

    private User loadScoped(UUID id) {
        return userRepository.findByIdAndTenantId(id, CurrentUser.get().tenantId())
            .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private Set<Role> loadRoles(Set<RoleName> roleNames) {
        Set<Role> roles = roleRepository.findAllByNameIn(roleNames).stream().collect(Collectors.toSet());
        if (roles.size() != roleNames.size()) {
            throw new NotFoundException("Role not found");
        }
        return roles;
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getStatus(),
            user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()),
            user.getCreatedAt(),
            user.getUpdatedAt(),
            user.getLastLoginAt()
        );
    }
}

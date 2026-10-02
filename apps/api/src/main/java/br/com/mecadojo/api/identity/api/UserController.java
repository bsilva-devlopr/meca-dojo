package br.com.mecadojo.api.identity.api;

import br.com.mecadojo.api.identity.api.dto.UserDtos.CreateUserRequest;
import br.com.mecadojo.api.identity.api.dto.UserDtos.UpdateUserRequest;
import br.com.mecadojo.api.identity.api.dto.UserDtos.UpdateUserRolesRequest;
import br.com.mecadojo.api.identity.api.dto.UserDtos.UserResponse;
import br.com.mecadojo.api.identity.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER_READ')")
    List<UserResponse> list() {
        return userService.list();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER_CREATE')")
    UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        return userService.create(request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_READ')")
    UserResponse get(@PathVariable UUID id) {
        return userService.get(id);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    UserResponse update(@PathVariable UUID id, @RequestBody UpdateUserRequest request) {
        return userService.update(id, request);
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('ROLE_ASSIGN')")
    UserResponse updateRoles(@PathVariable UUID id, @Valid @RequestBody UpdateUserRolesRequest request) {
        return userService.updateRoles(id, request);
    }
}

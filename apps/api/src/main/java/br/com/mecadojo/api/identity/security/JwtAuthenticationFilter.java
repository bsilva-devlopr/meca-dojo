package br.com.mecadojo.api.identity.security;

import br.com.mecadojo.api.identity.domain.PermissionName;
import br.com.mecadojo.api.identity.domain.TenantStatus;
import br.com.mecadojo.api.identity.domain.UserStatus;
import br.com.mecadojo.api.identity.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                authenticate(header.substring(7));
            } catch (RuntimeException exception) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(String token) {
        JwtService.JwtClaims claims = jwtService.parse(token);
        userRepository.findWithTenantAndRolesById(claims.userId())
            .filter(user -> user.getTenant().getId().equals(claims.tenantId()))
            .filter(user -> user.getTenant().getStatus() == TenantStatus.ACTIVE)
            .filter(user -> user.getStatus() == UserStatus.ACTIVE)
            .ifPresent(user -> {
                Set<PermissionName> permissions = user.getRoles().stream()
                    .flatMap(role -> role.getPermissions().stream())
                    .map(permission -> permission.getName())
                    .collect(Collectors.toSet());
                AuthenticatedUser principal = new AuthenticatedUser(
                    user.getId(),
                    user.getTenant().getId(),
                    user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toSet()),
                    permissions
                );
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    permissions.stream().map(permission -> new SimpleGrantedAuthority(permission.name())).toList()
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            });
    }
}

package br.com.mecadojo.api.identity.service;

import br.com.mecadojo.api.identity.api.dto.AuthDtos.MeResponse;
import br.com.mecadojo.api.identity.api.dto.AuthDtos.TenantSummary;
import br.com.mecadojo.api.identity.api.dto.AuthDtos.TokenResponse;
import br.com.mecadojo.api.identity.domain.PermissionName;
import br.com.mecadojo.api.identity.domain.RefreshToken;
import br.com.mecadojo.api.identity.domain.RoleName;
import br.com.mecadojo.api.identity.domain.Tenant;
import br.com.mecadojo.api.identity.domain.TenantStatus;
import br.com.mecadojo.api.identity.domain.User;
import br.com.mecadojo.api.identity.domain.UserStatus;
import br.com.mecadojo.api.identity.repository.RefreshTokenRepository;
import br.com.mecadojo.api.identity.repository.TenantRepository;
import br.com.mecadojo.api.identity.repository.UserRepository;
import br.com.mecadojo.api.identity.security.AuthenticatedUser;
import br.com.mecadojo.api.identity.security.CurrentUser;
import br.com.mecadojo.api.identity.security.JwtProperties;
import br.com.mecadojo.api.identity.security.JwtService;
import br.com.mecadojo.api.shared.exception.UnauthorizedException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final Duration refreshTokenTtl;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(
        TenantRepository tenantRepository,
        UserRepository userRepository,
        RefreshTokenRepository refreshTokenRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService,
        JwtProperties jwtProperties,
        @Value("${app.refresh-token-ttl}") Duration refreshTokenTtl
    ) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.refreshTokenTtl = refreshTokenTtl;
    }

    @Transactional
    public TokenResponse login(String tenantSlug, String email, String password) {
        String normalizedTenant = SlugNormalizer.slug(tenantSlug);
        String normalizedEmail = SlugNormalizer.email(email);
        Tenant tenant = tenantRepository.findBySlug(normalizedTenant)
            .orElseThrow(() -> authFailure(normalizedTenant, normalizedEmail));
        if (tenant.getStatus() != TenantStatus.ACTIVE) {
            throw authFailure(normalizedTenant, normalizedEmail);
        }
        User user = userRepository.findByTenantIdAndEmail(tenant.getId(), normalizedEmail)
            .orElseThrow(() -> authFailure(normalizedTenant, normalizedEmail));
        if (user.getStatus() != UserStatus.ACTIVE || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw authFailure(normalizedTenant, normalizedEmail);
        }

        user.markLoginNow();
        userRepository.save(user);
        LOGGER.info("login success userId={} tenantId={}", user.getId(), tenant.getId());
        return issueTokens(user);
    }

    @Transactional
    public TokenResponse refresh(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hash(refreshTokenValue))
            .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        if (!refreshToken.isUsable(Instant.now())) {
            throw new UnauthorizedException("Invalid refresh token");
        }
        User user = refreshToken.getUser();
        if (user.getStatus() != UserStatus.ACTIVE || user.getTenant().getStatus() != TenantStatus.ACTIVE) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);
        LOGGER.info("refresh token rotation userId={} tenantId={}", user.getId(), user.getTenant().getId());
        return issueTokens(user);
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenRepository.findByTokenHash(hash(refreshTokenValue))
            .filter(token -> token.getRevokedAt() == null)
            .ifPresent(token -> {
                token.revoke();
                refreshTokenRepository.save(token);
                LOGGER.info("logout userId={} tenantId={}", token.getUser().getId(), token.getUser().getTenant().getId());
            });
    }

    @Transactional(readOnly = true)
    public MeResponse me() {
        AuthenticatedUser current = CurrentUser.get();
        User user = userRepository.findByIdAndTenantId(current.userId(), current.tenantId())
            .orElseThrow(() -> new UnauthorizedException("Authentication required"));
        return toMe(user);
    }

    private UnauthorizedException authFailure(String tenantSlug, String email) {
        LOGGER.info("login failure tenant={} email={}", tenantSlug, email);
        return new UnauthorizedException("Invalid credentials");
    }

    private TokenResponse issueTokens(User user) {
        Set<RoleName> roles = user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toSet());
        String accessToken = jwtService.issueAccessToken(user.getId(), user.getTenant().getId(), roles);
        String refreshTokenValue = randomToken();
        refreshTokenRepository.save(new RefreshToken(
            user,
            hash(refreshTokenValue),
            Instant.now().plus(refreshTokenTtl)
        ));
        return new TokenResponse(accessToken, refreshTokenValue, jwtProperties.accessTokenTtl().toSeconds());
    }

    private MeResponse toMe(User user) {
        Set<RoleName> roles = user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toSet());
        Set<PermissionName> permissions = user.getRoles().stream()
            .flatMap(role -> role.getPermissions().stream())
            .map(permission -> permission.getName())
            .collect(Collectors.toSet());
        return new MeResponse(
            user.getId(),
            new TenantSummary(user.getTenant().getId(), user.getTenant().getName(), user.getTenant().getSlug()),
            user.getName(),
            user.getEmail(),
            roles,
            permissions
        );
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return java.util.HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Could not hash token", exception);
        }
    }
}

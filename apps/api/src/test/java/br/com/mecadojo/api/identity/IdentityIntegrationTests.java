package br.com.mecadojo.api.identity;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.mecadojo.api.identity.api.dto.AuthDtos.MeResponse;
import br.com.mecadojo.api.identity.api.dto.AuthDtos.TokenResponse;
import br.com.mecadojo.api.identity.api.dto.UserDtos.UserResponse;
import br.com.mecadojo.api.identity.domain.Role;
import br.com.mecadojo.api.identity.domain.RoleName;
import br.com.mecadojo.api.identity.domain.Tenant;
import br.com.mecadojo.api.identity.domain.TenantStatus;
import br.com.mecadojo.api.identity.domain.Unit;
import br.com.mecadojo.api.identity.domain.User;
import br.com.mecadojo.api.identity.domain.UserStatus;
import br.com.mecadojo.api.identity.repository.RoleRepository;
import br.com.mecadojo.api.identity.repository.TenantRepository;
import br.com.mecadojo.api.identity.repository.UnitRepository;
import br.com.mecadojo.api.identity.repository.UserRepository;
import br.com.mecadojo.api.support.AbstractPostgreSqlTestcontainer;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IdentityIntegrationTests extends AbstractPostgreSqlTestcontainer {
    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    TenantRepository tenantRepository;

    @Autowired
    UnitRepository unitRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanIdentityData() {
        jdbcTemplate.execute("delete from training_session_staff");
        jdbcTemplate.execute("delete from training_sessions");
        jdbcTemplate.execute("delete from class_schedule_staff");
        jdbcTemplate.execute("delete from class_schedules");
        jdbcTemplate.execute("delete from dojo_classes");
        jdbcTemplate.execute("delete from graduation_policies");
        jdbcTemplate.execute("delete from student_graduations");
        jdbcTemplate.execute("delete from guardian_students");
        jdbcTemplate.execute("delete from student_emergency_contacts");
        jdbcTemplate.execute("delete from staff_profiles");
        jdbcTemplate.execute("delete from guardians");
        jdbcTemplate.execute("delete from students");
        jdbcTemplate.execute("delete from refresh_tokens");
        jdbcTemplate.execute("delete from user_roles");
        jdbcTemplate.execute("delete from users");
        jdbcTemplate.execute("delete from units");
        jdbcTemplate.execute("delete from tenants");
    }

    @Test
    void authenticatesAndReturnsMeForValidCredentials() {
        TestUser owner = createUser("Meca Dojo", "meca-dojo", "Admin Local", "admin@mecadojo.local", "secret", RoleName.OWNER);

        TokenResponse tokens = login("meca-dojo", "admin@mecadojo.local", "secret").getBody();

        ResponseEntity<MeResponse> me = restTemplate.exchange(
            url("/api/v1/auth/me"),
            HttpMethod.GET,
            bearer(tokens.accessToken()),
            MeResponse.class
        );
        assertThat(me.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(me.getBody().id()).isEqualTo(owner.user().getId());
        assertThat(me.getBody().tenant().slug()).isEqualTo("meca-dojo");
        assertThat(me.getBody().roles()).contains(RoleName.OWNER);
        assertThat(me.getBody().permissions()).contains(br.com.mecadojo.api.identity.domain.PermissionName.TENANT_READ);
    }

    @Test
    void rejectsInvalidPasswordInvalidTenantInactiveTenantAndInactiveUser() {
        TestUser active = createUser("Meca Dojo", "meca-dojo", "Admin", "admin@mecadojo.local", "secret", RoleName.OWNER);
        TestUser inactiveUser = createUser("Other", "other", "Other", "other@mecadojo.local", "secret", RoleName.OWNER);
        inactiveUser.user().setStatus(UserStatus.INACTIVE);
        userRepository.save(inactiveUser.user());
        active.tenant().setStatus(TenantStatus.INACTIVE);
        tenantRepository.save(active.tenant());

        assertThat(login("meca-dojo", "admin@mecadojo.local", "wrong").getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(login("missing", "admin@mecadojo.local", "secret").getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(login("meca-dojo", "admin@mecadojo.local", "secret").getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(login("other", "other@mecadojo.local", "secret").getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void refreshRotatesTokenAndLogoutRevokesRefreshToken() {
        createUser("Meca Dojo", "meca-dojo", "Admin", "admin@mecadojo.local", "secret", RoleName.OWNER);
        TokenResponse first = login("meca-dojo", "admin@mecadojo.local", "secret").getBody();

        ResponseEntity<TokenResponse> rotated = post("/api/v1/auth/refresh", Map.of("refreshToken", first.refreshToken()), TokenResponse.class);
        assertThat(rotated.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(rotated.getBody().refreshToken()).isNotEqualTo(first.refreshToken());

        assertThat(post("/api/v1/auth/refresh", Map.of("refreshToken", first.refreshToken()), TokenResponse.class).getStatusCode())
            .isEqualTo(HttpStatus.UNAUTHORIZED);

        restTemplate.exchange(
            url("/api/v1/auth/logout"),
            HttpMethod.POST,
            bearer(rotated.getBody().accessToken(), Map.of("refreshToken", rotated.getBody().refreshToken())),
            Void.class
        );
        assertThat(post("/api/v1/auth/refresh", Map.of("refreshToken", rotated.getBody().refreshToken()), TokenResponse.class).getStatusCode())
            .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void meRequiresAuthentication() {
        ResponseEntity<String> response = restTemplate.getForEntity(url("/api/v1/auth/me"), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void tenantIsolationPreventsReadingUnitAndUserFromAnotherTenant() {
        TestUser tenantA = createUser("Tenant A", "tenant-a", "Owner A", "owner@a.test", "secret", RoleName.OWNER);
        TestUser tenantB = createUser("Tenant B", "tenant-b", "Owner B", "owner@b.test", "secret", RoleName.OWNER);
        Unit otherUnit = unitRepository.save(new Unit(tenantB.tenant(), "B Unit", "b-unit"));
        User otherUser = tenantB.user();
        String tokenA = login("tenant-a", "owner@a.test", "secret").getBody().accessToken();

        assertThat(restTemplate.exchange(url("/api/v1/units/" + otherUnit.getId()), HttpMethod.GET, bearer(tokenA), String.class).getStatusCode())
            .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(restTemplate.exchange(url("/api/v1/users/" + otherUser.getId()), HttpMethod.GET, bearer(tokenA), String.class).getStatusCode())
            .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(tenantA.tenant().getId()).isNotEqualTo(tenantB.tenant().getId());
    }

    @Test
    void userWithoutPermissionReceivesForbiddenAndOwnerHasExpectedPermissions() {
        createUser("Meca Dojo", "meca-dojo", "Student", "student@mecadojo.local", "secret", RoleName.STUDENT);
        String studentToken = login("meca-dojo", "student@mecadojo.local", "secret").getBody().accessToken();

        ResponseEntity<String> forbidden = restTemplate.exchange(url("/api/v1/units"), HttpMethod.GET, bearer(studentToken), String.class);
        assertThat(forbidden.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        createUser("Admin Tenant", "admin-tenant", "Owner", "owner@mecadojo.local", "secret", RoleName.OWNER);
        MeResponse me = restTemplate.exchange(
            url("/api/v1/auth/me"),
            HttpMethod.GET,
            bearer(login("admin-tenant", "owner@mecadojo.local", "secret").getBody().accessToken()),
            MeResponse.class
        ).getBody();
        assertThat(me.permissions()).contains(
            br.com.mecadojo.api.identity.domain.PermissionName.USER_CREATE,
            br.com.mecadojo.api.identity.domain.PermissionName.ROLE_ASSIGN
        );
    }

    @Test
    void userEmailIsUniqueInsideTenantButCanRepeatAcrossTenants() {
        createUser("Tenant A", "tenant-a", "Owner A", "owner@a.test", "secret", RoleName.OWNER);
        createUser("Tenant B", "tenant-b", "Owner B", "owner@b.test", "secret", RoleName.OWNER);
        String tokenA = login("tenant-a", "owner@a.test", "secret").getBody().accessToken();
        String tokenB = login("tenant-b", "owner@b.test", "secret").getBody().accessToken();
        Map<String, Object> request = Map.of("name", "Joao", "email", "same@example.com", "password", "secret", "roles", List.of("STUDENT"));

        assertThat(restTemplate.exchange(url("/api/v1/users"), HttpMethod.POST, bearer(tokenA, request), UserResponse.class).getStatusCode())
            .isEqualTo(HttpStatus.OK);
        assertThat(restTemplate.exchange(url("/api/v1/users"), HttpMethod.POST, bearer(tokenB, request), UserResponse.class).getStatusCode())
            .isEqualTo(HttpStatus.OK);
        assertThat(restTemplate.exchange(url("/api/v1/users"), HttpMethod.POST, bearer(tokenA, request), String.class).getStatusCode())
            .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void cannotRemoveOrDeactivateLastActiveOwner() {
        TestUser owner = createUser("Meca Dojo", "meca-dojo", "Owner", "owner@mecadojo.local", "secret", RoleName.OWNER);
        String token = login("meca-dojo", "owner@mecadojo.local", "secret").getBody().accessToken();

        assertThat(restTemplate.exchange(
            url("/api/v1/users/" + owner.user().getId() + "/roles"),
            HttpMethod.PUT,
            bearer(token, Map.of("roles", List.of("ADMIN"))),
            String.class
        ).getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        assertThat(restTemplate.exchange(
            url("/api/v1/users/" + owner.user().getId()),
            HttpMethod.PATCH,
            bearer(token, Map.of("status", "INACTIVE")),
            String.class
        ).getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    private ResponseEntity<TokenResponse> login(String tenant, String email, String password) {
        return post("/api/v1/auth/login", Map.of("tenant", tenant, "email", email, "password", password), TokenResponse.class);
    }

    private <T> ResponseEntity<T> post(String path, Object body, Class<T> responseType) {
        return restTemplate.postForEntity(url(path), body, responseType);
    }

    private HttpEntity<Void> bearer(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        return new HttpEntity<>(headers);
    }

    private HttpEntity<Object> bearer(String accessToken, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        return new HttpEntity<>(body, headers);
    }

    private TestUser createUser(String tenantName, String tenantSlug, String name, String email, String password, RoleName roleName) {
        Tenant tenant = tenantRepository.save(new Tenant(tenantName, tenantSlug));
        Role role = roleRepository.findByName(roleName).orElseThrow();
        User user = userRepository.save(new User(tenant, name, email, passwordEncoder.encode(password), Set.of(role)));
        return new TestUser(tenant, user);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private record TestUser(Tenant tenant, User user) {
    }
}

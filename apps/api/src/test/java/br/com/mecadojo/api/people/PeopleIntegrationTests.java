package br.com.mecadojo.api.people;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.mecadojo.api.identity.api.dto.AuthDtos.TokenResponse;
import br.com.mecadojo.api.identity.domain.Role;
import br.com.mecadojo.api.identity.domain.RoleName;
import br.com.mecadojo.api.identity.domain.Tenant;
import br.com.mecadojo.api.identity.domain.Unit;
import br.com.mecadojo.api.identity.domain.User;
import br.com.mecadojo.api.identity.repository.RoleRepository;
import br.com.mecadojo.api.identity.repository.TenantRepository;
import br.com.mecadojo.api.identity.repository.UnitRepository;
import br.com.mecadojo.api.identity.repository.UserRepository;
import br.com.mecadojo.api.people.api.dto.PageResponse;
import br.com.mecadojo.api.people.api.dto.StaffDtos.StaffResponse;
import br.com.mecadojo.api.people.api.dto.StudentDtos.StudentResponse;
import br.com.mecadojo.api.people.domain.StaffProfile;
import br.com.mecadojo.api.people.domain.StaffType;
import br.com.mecadojo.api.people.domain.Student;
import br.com.mecadojo.api.people.repository.StaffProfileRepository;
import br.com.mecadojo.api.people.repository.StudentRepository;
import br.com.mecadojo.api.support.AbstractPostgreSqlTestcontainer;
import java.time.LocalDate;
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
class PeopleIntegrationTests extends AbstractPostgreSqlTestcontainer {
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
    StudentRepository studentRepository;

    @Autowired
    StaffProfileRepository staffProfileRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanData() {
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
    void createsListsSearchesFiltersAndUpdatesStudent() {
        Fixture fixture = fixture("meca", RoleName.OWNER);
        String token = login(fixture.tenant().getSlug(), fixture.user().getEmail());

        ResponseEntity<StudentResponse> created = restTemplate.exchange(
            url("/api/v1/students"),
            HttpMethod.POST,
            bearer(token, studentRequest(fixture.unit().getId(), "Joao da Silva", "ACTIVE")),
            StudentResponse.class
        );

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(created.getBody().primaryUnit().id()).isEqualTo(fixture.unit().getId());

        ResponseEntity<PageResponse> listBySearch = restTemplate.exchange(
            url("/api/v1/students?search=joao&page=0&size=10"),
            HttpMethod.GET,
            bearer(token),
            PageResponse.class
        );
        assertThat(listBySearch.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listBySearch.getBody().totalElements()).isEqualTo(1);

        ResponseEntity<PageResponse> listByStatusAndUnit = restTemplate.exchange(
            url("/api/v1/students?status=ACTIVE&unitId=" + fixture.unit().getId()),
            HttpMethod.GET,
            bearer(token),
            PageResponse.class
        );
        assertThat(listByStatusAndUnit.getBody().totalElements()).isEqualTo(1);

        ResponseEntity<StudentResponse> updated = restTemplate.exchange(
            url("/api/v1/students/" + created.getBody().id()),
            HttpMethod.PATCH,
            bearer(token, Map.of("preferredName", "Joao", "status", "TRIAL")),
            StudentResponse.class
        );
        assertThat(updated.getBody().preferredName()).isEqualTo("Joao");
        assertThat(updated.getBody().status().name()).isEqualTo("TRIAL");
    }

    @Test
    void rejectsInvalidUnitAndCrossTenantStudentReferences() {
        Fixture tenantA = fixture("tenant-a", RoleName.OWNER);
        Fixture tenantB = fixture("tenant-b", RoleName.OWNER);
        String tokenA = login(tenantA.tenant().getSlug(), tenantA.user().getEmail());
        Student studentB = studentRepository.save(new Student(tenantB.tenant(), tenantB.unit(), "Aluno B", LocalDate.now(), br.com.mecadojo.api.people.domain.StudentStatus.ACTIVE));

        assertThat(restTemplate.exchange(url("/api/v1/students"), HttpMethod.POST, bearer(tokenA, studentRequest(tenantB.unit().getId(), "Cross Unit", "ACTIVE")), String.class).getStatusCode())
            .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(restTemplate.exchange(url("/api/v1/students"), HttpMethod.POST, bearer(tokenA, studentRequestWithUser(tenantA.unit().getId(), tenantB.user().getId())), String.class).getStatusCode())
            .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(restTemplate.exchange(url("/api/v1/students/" + studentB.getId()), HttpMethod.GET, bearer(tokenA), String.class).getStatusCode())
            .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(restTemplate.exchange(url("/api/v1/students/" + studentB.getId()), HttpMethod.PATCH, bearer(tokenA, Map.of("fullName", "Hacked")), String.class).getStatusCode())
            .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void managesEmergencyContact() {
        Fixture fixture = fixture("meca", RoleName.OWNER);
        String token = login(fixture.tenant().getSlug(), fixture.user().getEmail());
        StudentResponse student = createStudent(token, fixture.unit().getId());

        StudentResponse withContact = restTemplate.exchange(
            url("/api/v1/students/" + student.id() + "/emergency-contact"),
            HttpMethod.PUT,
            bearer(token, Map.of("name", "Maria", "relationship", "Mae", "phone", "14999999999")),
            StudentResponse.class
        ).getBody();
        assertThat(withContact.emergencyContact().name()).isEqualTo("Maria");

        restTemplate.exchange(url("/api/v1/students/" + student.id() + "/emergency-contact"), HttpMethod.DELETE, bearer(token), Void.class);
        StudentResponse withoutContact = restTemplate.exchange(url("/api/v1/students/" + student.id()), HttpMethod.GET, bearer(token), StudentResponse.class).getBody();
        assertThat(withoutContact.emergencyContact()).isNull();
    }

    @Test
    void createsGuardianLinksToStudentAndKeepsSinglePrimary() {
        Fixture fixture = fixture("meca", RoleName.OWNER);
        String token = login(fixture.tenant().getSlug(), fixture.user().getEmail());
        StudentResponse student = createStudent(token, fixture.unit().getId());

        StudentResponse first = restTemplate.exchange(
            url("/api/v1/students/" + student.id() + "/guardians/new"),
            HttpMethod.POST,
            bearer(token, Map.of("fullName", "Pai", "phone", "111", "relationship", "FATHER", "primary", true)),
            StudentResponse.class
        ).getBody();
        StudentResponse second = restTemplate.exchange(
            url("/api/v1/students/" + student.id() + "/guardians/new"),
            HttpMethod.POST,
            bearer(token, Map.of("fullName", "Mae", "phone", "222", "relationship", "MOTHER", "primary", true)),
            StudentResponse.class
        ).getBody();

        assertThat(first.guardians()).hasSize(1);
        assertThat(second.guardians()).hasSize(2);
        assertThat(second.guardians().stream().filter(guardian -> guardian.primary()).count()).isEqualTo(1);
        assertThat(second.guardians().stream().filter(guardian -> guardian.primary()).findFirst().orElseThrow().fullName()).isEqualTo("Mae");
    }

    @Test
    void rejectsCrossTenantGuardianLink() {
        Fixture tenantA = fixture("tenant-a", RoleName.OWNER);
        Fixture tenantB = fixture("tenant-b", RoleName.OWNER);
        String tokenA = login(tenantA.tenant().getSlug(), tenantA.user().getEmail());
        StudentResponse studentA = createStudent(tokenA, tenantA.unit().getId());
        String tokenB = login(tenantB.tenant().getSlug(), tenantB.user().getEmail());
        UUID guardianB = restTemplate.exchange(url("/api/v1/guardians"), HttpMethod.POST, bearer(tokenB, Map.of("fullName", "Guardian B", "phone", "333")), br.com.mecadojo.api.people.api.dto.GuardianDtos.GuardianResponse.class).getBody().id();

        assertThat(restTemplate.exchange(url("/api/v1/students/" + studentA.id() + "/guardians"), HttpMethod.POST, bearer(tokenA, Map.of("guardianId", guardianB, "relationship", "OTHER", "primary", false)), String.class).getStatusCode())
            .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void createsStaffAndRejectsCrossTenantReferences() {
        Fixture tenantA = fixture("tenant-a", RoleName.OWNER);
        Fixture tenantB = fixture("tenant-b", RoleName.OWNER);
        String tokenA = login(tenantA.tenant().getSlug(), tenantA.user().getEmail());

        ResponseEntity<StaffResponse> professor = restTemplate.exchange(
            url("/api/v1/staff"),
            HttpMethod.POST,
            bearer(tokenA, Map.of("userId", tenantA.user().getId(), "type", "PROFESSOR", "displayName", "Sensei", "primaryUnitId", tenantA.unit().getId())),
            StaffResponse.class
        );
        assertThat(professor.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(professor.getBody().type().name()).isEqualTo("PROFESSOR");

        assertThat(restTemplate.exchange(url("/api/v1/staff"), HttpMethod.POST, bearer(tokenA, Map.of("userId", tenantB.user().getId(), "type", "INSTRUCTOR", "displayName", "Cross")), String.class).getStatusCode())
            .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(restTemplate.exchange(url("/api/v1/staff"), HttpMethod.POST, bearer(tokenA, Map.of("userId", tenantA.user().getId(), "type", "INSTRUCTOR", "displayName", "Cross Unit", "primaryUnitId", tenantB.unit().getId())), String.class).getStatusCode())
            .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void sameUserCanBeStudentAndStaff() {
        Fixture fixture = fixture("meca", RoleName.OWNER);
        String token = login(fixture.tenant().getSlug(), fixture.user().getEmail());
        StudentResponse student = restTemplate.exchange(url("/api/v1/students"), HttpMethod.POST, bearer(token, studentRequestWithUser(fixture.unit().getId(), fixture.user().getId())), StudentResponse.class).getBody();
        StaffResponse staff = restTemplate.exchange(url("/api/v1/staff"), HttpMethod.POST, bearer(token, Map.of("userId", fixture.user().getId(), "type", "PROFESSOR", "displayName", "Sensei")), StaffResponse.class).getBody();

        assertThat(student.user().id()).isEqualTo(fixture.user().getId());
        assertThat(staff.user().id()).isEqualTo(fixture.user().getId());
    }

    @Test
    void rbacForStudentPermissions() {
        Fixture studentRole = fixture("student-role", RoleName.STUDENT);
        String studentToken = login(studentRole.tenant().getSlug(), studentRole.user().getEmail());
        assertThat(restTemplate.exchange(url("/api/v1/students"), HttpMethod.GET, bearer(studentToken), String.class).getStatusCode())
            .isEqualTo(HttpStatus.FORBIDDEN);

        Fixture reception = fixture("reception", RoleName.RECEPTION);
        String receptionToken = login(reception.tenant().getSlug(), reception.user().getEmail());
        assertThat(restTemplate.exchange(url("/api/v1/students"), HttpMethod.POST, bearer(receptionToken, studentRequest(reception.unit().getId(), "Reception Student", "ACTIVE")), StudentResponse.class).getStatusCode())
            .isEqualTo(HttpStatus.OK);

        Fixture professor = fixture("professor", RoleName.PROFESSOR);
        studentRepository.save(new Student(professor.tenant(), professor.unit(), "Professor Visible", LocalDate.now(), br.com.mecadojo.api.people.domain.StudentStatus.ACTIVE));
        String professorToken = login(professor.tenant().getSlug(), professor.user().getEmail());
        assertThat(restTemplate.exchange(url("/api/v1/students"), HttpMethod.GET, bearer(professorToken), PageResponse.class).getStatusCode())
            .isEqualTo(HttpStatus.OK);
    }

    private StudentResponse createStudent(String token, UUID unitId) {
        return restTemplate.exchange(url("/api/v1/students"), HttpMethod.POST, bearer(token, studentRequest(unitId, "Joao da Silva", "ACTIVE")), StudentResponse.class).getBody();
    }

    private Map<String, Object> studentRequest(UUID unitId, String fullName, String status) {
        return Map.of(
            "primaryUnitId", unitId,
            "fullName", fullName,
            "status", status,
            "joinDate", LocalDate.now().toString()
        );
    }

    private Map<String, Object> studentRequestWithUser(UUID unitId, UUID userId) {
        return Map.of(
            "primaryUnitId", unitId,
            "fullName", "Linked Student",
            "status", "ACTIVE",
            "joinDate", LocalDate.now().toString(),
            "userId", userId
        );
    }

    private String login(String tenant, String email) {
        TokenResponse response = restTemplate.postForEntity(
            url("/api/v1/auth/login"),
            Map.of("tenant", tenant, "email", email, "password", "secret"),
            TokenResponse.class
        ).getBody();
        return response.accessToken();
    }

    private Fixture fixture(String slug, RoleName roleName) {
        Tenant tenant = tenantRepository.save(new Tenant("Tenant " + slug, slug));
        Unit unit = unitRepository.save(new Unit(tenant, "Unidade " + slug, "unit-" + slug));
        Role role = roleRepository.findByName(roleName).orElseThrow();
        User user = userRepository.save(new User(tenant, roleName.name() + " " + slug, roleName.name().toLowerCase() + "@" + slug + ".test", passwordEncoder.encode("secret"), Set.of(role)));
        return new Fixture(tenant, unit, user);
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

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private record Fixture(Tenant tenant, Unit unit, User user) {
    }
}

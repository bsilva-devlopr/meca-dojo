package br.com.mecadojo.api.graduation;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.mecadojo.api.graduation.api.dto.GraduationDtos.CurrentGraduationResponse;
import br.com.mecadojo.api.graduation.api.dto.GraduationDtos.GraduationEventResponse;
import br.com.mecadojo.api.graduation.repository.BeltRepository;
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
import br.com.mecadojo.api.people.domain.StaffProfile;
import br.com.mecadojo.api.people.domain.StaffType;
import br.com.mecadojo.api.people.domain.Student;
import br.com.mecadojo.api.people.domain.StudentStatus;
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
class GraduationIntegrationTests extends AbstractPostgreSqlTestcontainer {
    @LocalServerPort
    int port;

    @Autowired TestRestTemplate restTemplate;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired TenantRepository tenantRepository;
    @Autowired UnitRepository unitRepository;
    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired StudentRepository studentRepository;
    @Autowired StaffProfileRepository staffProfileRepository;
    @Autowired BeltRepository beltRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanData() {
        jdbcTemplate.execute("update check_ins set attendance_id = null");
        jdbcTemplate.execute("update attendances set check_in_id = null");
        jdbcTemplate.execute("delete from attendances");
        jdbcTemplate.execute("delete from check_ins");
        jdbcTemplate.execute("delete from check_in_tokens");
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
    void catalogSeedsGeneralAndKidsBeltsInOrder() {
        Fixture fixture = fixture("catalog", RoleName.OWNER);
        String token = login(fixture);

        ResponseEntity<List> general = restTemplate.exchange(url("/api/v1/belts?system=GENERAL"), HttpMethod.GET, bearer(token), List.class);
        ResponseEntity<List> kids = restTemplate.exchange(url("/api/v1/belts?system=KIDS"), HttpMethod.GET, bearer(token), List.class);

        assertThat(general.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(general.getBody()).hasSize(5);
        assertThat((String) ((Map<?, ?>) general.getBody().getFirst()).get("code")).isEqualTo("WHITE");
        assertThat(kids.getBody()).hasSize(13);
        assertThat((String) ((Map<?, ?>) kids.getBody().getFirst()).get("code")).isEqualTo("WHITE");
        assertThat((String) ((Map<?, ?>) kids.getBody().getLast()).get("code")).isEqualTo("GREEN_BLACK");
    }

    @Test
    void registersInitialGraduationAndRejectsSecondInitial() {
        Fixture fixture = fixture("initial", RoleName.OWNER);
        String token = login(fixture);
        Student student = student(fixture, "Aluno Initial");
        StaffProfile staff = staff(fixture, StaffType.PROFESSOR);

        ResponseEntity<GraduationEventResponse> created = restTemplate.exchange(
            url("/api/v1/students/" + student.getId() + "/graduations/initial"),
            HttpMethod.POST,
            bearer(token, Map.of("beltCode", "BLUE", "system", "GENERAL", "degree", 2, "graduatedAt", "2025-06-20", "awardedByStaffId", staff.getId(), "notes", "Historico anterior")),
            GraduationEventResponse.class
        );

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(created.getBody().belt().code()).isEqualTo("BLUE");
        assertThat(created.getBody().degree()).isEqualTo(2);
        assertThat(created.getBody().awardedBy().id()).isEqualTo(staff.getId());
        assertThat(created.getBody().createdBy().id()).isEqualTo(fixture.user().getId());

        assertThat(restTemplate.exchange(url("/api/v1/students/" + student.getId() + "/graduations/initial"), HttpMethod.POST, bearer(token, Map.of("beltCode", "BLUE", "system", "GENERAL", "degree", 2)), String.class).getStatusCode())
            .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void promotesDegreeAndRejectsDegreeBeyondConfiguredLimit() {
        Fixture fixture = fixture("degree", RoleName.OWNER);
        String token = login(fixture);
        Student student = student(fixture, "Aluno Degree");
        initial(token, student, "WHITE", 3);

        GraduationEventResponse degree4 = restTemplate.exchange(url("/api/v1/students/" + student.getId() + "/graduations/degree"), HttpMethod.POST, bearer(token, Map.of("graduatedAt", "2026-10-02")), GraduationEventResponse.class).getBody();
        assertThat(degree4.degree()).isEqualTo(4);

        assertThat(restTemplate.exchange(url("/api/v1/students/" + student.getId() + "/graduations/degree"), HttpMethod.POST, bearer(token, Map.of("graduatedAt", "2026-10-02")), String.class).getStatusCode())
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void promotesBeltsOnlyToNextBeltInSameSystem() {
        Fixture fixture = fixture("belt", RoleName.OWNER);
        String token = login(fixture);
        Student student = student(fixture, "Aluno Belt");
        initial(token, student, "WHITE", 4);

        assertThat(restTemplate.exchange(url("/api/v1/students/" + student.getId() + "/graduations/belt"), HttpMethod.POST, bearer(token, Map.of("beltCode", "BROWN", "system", "GENERAL", "graduatedAt", "2026-10-02")), String.class).getStatusCode())
            .isEqualTo(HttpStatus.BAD_REQUEST);

        GraduationEventResponse blue = restTemplate.exchange(url("/api/v1/students/" + student.getId() + "/graduations/belt"), HttpMethod.POST, bearer(token, Map.of("beltCode", "BLUE", "system", "GENERAL", "graduatedAt", "2026-10-02")), GraduationEventResponse.class).getBody();
        assertThat(blue.belt().code()).isEqualTo("BLUE");
        assertThat(blue.degree()).isZero();
    }

    @Test
    void correctionRequiresReasonAndPreservesOriginalHistory() {
        Fixture fixture = fixture("correction", RoleName.ADMIN);
        String token = login(fixture);
        Student student = student(fixture, "Aluno Correction");
        GraduationEventResponse initial = initial(token, student, "BLUE", 1);

        assertThat(restTemplate.exchange(url("/api/v1/students/" + student.getId() + "/graduations/correction"), HttpMethod.POST, bearer(token, Map.of("beltCode", "BLUE", "system", "GENERAL", "degree", 2)), String.class).getStatusCode())
            .isEqualTo(HttpStatus.BAD_REQUEST);

        GraduationEventResponse correction = restTemplate.exchange(
            url("/api/v1/students/" + student.getId() + "/graduations/correction"),
            HttpMethod.POST,
            bearer(token, Map.of("beltCode", "BLUE", "system", "GENERAL", "degree", 2, "correctsGraduationId", initial.id(), "reason", "Lancamento inicial com grau incorreto")),
            GraduationEventResponse.class
        ).getBody();

        assertThat(correction.type().name()).isEqualTo("CORRECTION");
        assertThat(correction.correctsGraduationId()).isEqualTo(initial.id());
        assertThat(jdbcTemplate.queryForObject("select count(*) from student_graduations where student_id = ?", Long.class, student.getId())).isEqualTo(2L);
    }

    @Test
    void isolatesStudentStaffAndPolicyByTenant() {
        Fixture tenantA = fixture("tenant-a-grad", RoleName.OWNER);
        Fixture tenantB = fixture("tenant-b-grad", RoleName.OWNER);
        String tokenA = login(tenantA);
        Student studentB = student(tenantB, "Aluno B");
        StaffProfile staffB = staff(tenantB, StaffType.PROFESSOR);
        Student studentA = student(tenantA, "Aluno A");

        assertThat(restTemplate.exchange(url("/api/v1/students/" + studentB.getId() + "/graduation"), HttpMethod.GET, bearer(tokenA), String.class).getStatusCode())
            .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(restTemplate.exchange(url("/api/v1/students/" + studentB.getId() + "/graduations/initial"), HttpMethod.POST, bearer(tokenA, Map.of("beltCode", "WHITE", "system", "GENERAL", "degree", 0)), String.class).getStatusCode())
            .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(restTemplate.exchange(url("/api/v1/students/" + studentA.getId() + "/graduations/initial"), HttpMethod.POST, bearer(tokenA, Map.of("beltCode", "WHITE", "system", "GENERAL", "degree", 0, "awardedByStaffId", staffB.getId())), String.class).getStatusCode())
            .isEqualTo(HttpStatus.NOT_FOUND);

        restTemplate.exchange(url("/api/v1/graduation-policies/BLUE?system=GENERAL"), HttpMethod.PUT, bearer(tokenA, Map.of("minimumDaysInGrade", 90)), String.class);
        assertThat(jdbcTemplate.queryForObject("select count(*) from graduation_policies where tenant_id = ?", Long.class, tenantA.tenant().getId())).isEqualTo(1L);
        assertThat(jdbcTemplate.queryForObject("select count(*) from graduation_policies where tenant_id = ?", Long.class, tenantB.tenant().getId())).isZero();
    }

    @Test
    void eligibilityUsesDaysPolicyAndWarnsAboutTrainingCount() {
        Fixture fixture = fixture("eligibility", RoleName.OWNER);
        String token = login(fixture);
        Student student = student(fixture, "Aluno Eligibility");
        initial(token, student, "WHITE", 0, LocalDate.now().minusDays(5));

        restTemplate.exchange(url("/api/v1/graduation-policies/WHITE?system=GENERAL"), HttpMethod.PUT, bearer(token, Map.of("minimumDaysInGrade", 30, "minimumTrainingCount", 10)), String.class);
        CurrentGraduationResponse notEligible = restTemplate.exchange(url("/api/v1/students/" + student.getId() + "/graduation"), HttpMethod.GET, bearer(token), CurrentGraduationResponse.class).getBody();
        assertThat(notEligible.eligibility().eligible()).isFalse();
        assertThat(notEligible.eligibility().reasons()).contains("Tempo minimo na graduacao atual ainda nao atingido");
        assertThat(notEligible.eligibility().reasons()).contains("Quantidade minima de treinos ainda nao atingida");
        assertThat(notEligible.eligibility().warnings()).doesNotContain("Criterio de frequencia ainda nao disponivel neste ciclo");

        restTemplate.exchange(url("/api/v1/graduation-policies/WHITE?system=GENERAL"), HttpMethod.PUT, bearer(token, Map.of("minimumDaysInGrade", 1)), String.class);
        CurrentGraduationResponse eligible = restTemplate.exchange(url("/api/v1/students/" + student.getId() + "/graduation"), HttpMethod.GET, bearer(token), CurrentGraduationResponse.class).getBody();
        assertThat(eligible.eligibility().eligible()).isTrue();
    }

    @Test
    void rbacAllowsProfessorCreateRejectsReceptionCreateAndAllowsAdminCorrection() {
        Fixture professor = fixture("prof-role", RoleName.PROFESSOR);
        String professorToken = login(professor);
        Student professorStudent = student(professor, "Aluno Professor");
        assertThat(restTemplate.exchange(url("/api/v1/students/" + professorStudent.getId() + "/graduations/initial"), HttpMethod.POST, bearer(professorToken, Map.of("beltCode", "WHITE", "system", "GENERAL", "degree", 0)), String.class).getStatusCode())
            .isEqualTo(HttpStatus.OK);

        Fixture reception = fixture("reception-role", RoleName.RECEPTION);
        String receptionToken = login(reception);
        Student receptionStudent = student(reception, "Aluno Reception");
        assertThat(restTemplate.exchange(url("/api/v1/students/" + receptionStudent.getId() + "/graduations/initial"), HttpMethod.POST, bearer(receptionToken, Map.of("beltCode", "WHITE", "system", "GENERAL", "degree", 0)), String.class).getStatusCode())
            .isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(restTemplate.exchange(url("/api/v1/students/" + receptionStudent.getId() + "/graduation"), HttpMethod.GET, bearer(receptionToken), String.class).getStatusCode())
            .isEqualTo(HttpStatus.OK);

        Fixture admin = fixture("admin-role", RoleName.ADMIN);
        String adminToken = login(admin);
        Student adminStudent = student(admin, "Aluno Admin");
        GraduationEventResponse initial = initial(adminToken, adminStudent, "WHITE", 0);
        assertThat(restTemplate.exchange(url("/api/v1/students/" + adminStudent.getId() + "/graduations/correction"), HttpMethod.POST, bearer(adminToken, Map.of("beltCode", "WHITE", "system", "GENERAL", "degree", 1, "correctsGraduationId", initial.id(), "reason", "Ajuste administrativo")), String.class).getStatusCode())
            .isEqualTo(HttpStatus.OK);
    }

    private GraduationEventResponse initial(String token, Student student, String beltCode, int degree) {
        return initial(token, student, beltCode, degree, LocalDate.of(2026, 10, 2));
    }

    private GraduationEventResponse initial(String token, Student student, String beltCode, int degree, LocalDate date) {
        return restTemplate.exchange(
            url("/api/v1/students/" + student.getId() + "/graduations/initial"),
            HttpMethod.POST,
            bearer(token, Map.of("beltCode", beltCode, "system", "GENERAL", "degree", degree, "graduatedAt", date.toString())),
            GraduationEventResponse.class
        ).getBody();
    }

    private StaffProfile staff(Fixture fixture, StaffType type) {
        return staffProfileRepository.save(new StaffProfile(fixture.tenant(), fixture.user(), type, "Sensei " + fixture.tenant().getSlug()));
    }

    private Student student(Fixture fixture, String name) {
        return studentRepository.save(new Student(fixture.tenant(), fixture.unit(), name, LocalDate.of(2026, 10, 2), StudentStatus.ACTIVE));
    }

    private Fixture fixture(String slug, RoleName roleName) {
        Tenant tenant = tenantRepository.save(new Tenant("Tenant " + slug, slug));
        Unit unit = unitRepository.save(new Unit(tenant, "Unidade Centro", "centro"));
        Role role = roleRepository.findByName(roleName).orElseThrow();
        User user = userRepository.save(new User(tenant, "User " + slug, slug + "@mecadojo.local", passwordEncoder.encode("secret"), Set.of(role)));
        return new Fixture(tenant, unit, user);
    }

    private String login(Fixture fixture) {
        return restTemplate.postForEntity(url("/api/v1/auth/login"), Map.of("tenant", fixture.tenant().getSlug(), "email", fixture.user().getEmail(), "password", "secret"), TokenResponse.class).getBody().accessToken();
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



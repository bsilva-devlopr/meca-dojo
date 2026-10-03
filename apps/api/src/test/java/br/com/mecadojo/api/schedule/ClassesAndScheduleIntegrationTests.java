package br.com.mecadojo.api.schedule;

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
import br.com.mecadojo.api.people.domain.StaffProfile;
import br.com.mecadojo.api.people.domain.StaffStatus;
import br.com.mecadojo.api.people.domain.StaffType;
import br.com.mecadojo.api.people.repository.StaffProfileRepository;
import br.com.mecadojo.api.schedule.api.dto.ScheduleDtos.ClassResponse;
import br.com.mecadojo.api.schedule.api.dto.ScheduleDtos.ScheduleResponse;
import br.com.mecadojo.api.schedule.api.dto.ScheduleDtos.WeeklyScheduleResponse;
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
class ClassesAndScheduleIntegrationTests extends AbstractPostgreSqlTestcontainer {
    @LocalServerPort
    int port;

    @Autowired TestRestTemplate restTemplate;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired TenantRepository tenantRepository;
    @Autowired UnitRepository unitRepository;
    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired StaffProfileRepository staffRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanData() {
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
    void createsListsFiltersUpdatesAndInactivatesClass() {
        Fixture fixture = fixture("classes", RoleName.OWNER);
        String token = login(fixture.tenant().getSlug(), fixture.user().getEmail());

        ClassResponse created = createClass(token, "Jiu-Jitsu Adulto", "GI", 30);
        assertThat(created.name()).isEqualTo("Jiu-Jitsu Adulto");
        assertThat(created.defaultCapacity()).isEqualTo(30);

        ResponseEntity<PageResponse> bySearch = restTemplate.exchange(url("/api/v1/classes?search=adulto&page=0&size=10"), HttpMethod.GET, bearer(token), PageResponse.class);
        assertThat(bySearch.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(bySearch.getBody().totalElements()).isEqualTo(1);

        ResponseEntity<PageResponse> byModality = restTemplate.exchange(url("/api/v1/classes?modality=GI"), HttpMethod.GET, bearer(token), PageResponse.class);
        assertThat(byModality.getBody().totalElements()).isEqualTo(1);

        ClassResponse updated = restTemplate.exchange(url("/api/v1/classes/" + created.id()), HttpMethod.PATCH, bearer(token, Map.of("status", "INACTIVE", "description", "Treino tecnico")), ClassResponse.class).getBody();
        assertThat(updated.status().name()).isEqualTo("INACTIVE");
        assertThat(updated.description()).isEqualTo("Treino tecnico");
    }

    @Test
    void createsListsWeeklyAndUpdatesSchedule() {
        Fixture fixture = fixture("schedule", RoleName.OWNER);
        String token = login(fixture.tenant().getSlug(), fixture.user().getEmail());
        ClassResponse dojoClass = createClass(token, "No-Gi Adulto", "NO_GI", 25);
        StaffProfile lead = staff(fixture, StaffType.PROFESSOR, "Sensei Moranga", StaffStatus.ACTIVE);
        StaffProfile assistant = staff(fixture, StaffType.INSTRUCTOR, "Bruno", StaffStatus.ACTIVE);

        ScheduleResponse created = createSchedule(token, dojoClass.id(), fixture.unit().getId(), "TUESDAY", "20:30", "22:00", 20, List.of(staffReq(lead.getId(), "LEAD"), staffReq(assistant.getId(), "ASSISTANT")));
        assertThat(created.effectiveCapacity()).isEqualTo(20);
        assertThat(created.staff()).hasSize(2);

        ResponseEntity<PageResponse> list = restTemplate.exchange(url("/api/v1/schedules?unitId=" + fixture.unit().getId() + "&dayOfWeek=TUESDAY"), HttpMethod.GET, bearer(token), PageResponse.class);
        assertThat(list.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(list.getBody().totalElements()).isEqualTo(1);

        WeeklyScheduleResponse weekly = restTemplate.exchange(url("/api/v1/schedules/weekly?unitId=" + fixture.unit().getId() + "&modality=NO_GI"), HttpMethod.GET, bearer(token), WeeklyScheduleResponse.class).getBody();
        assertThat(weekly.days().stream().filter(day -> day.dayOfWeek().name().equals("TUESDAY")).findFirst().orElseThrow().schedules()).hasSize(1);

        ScheduleResponse updated = restTemplate.exchange(url("/api/v1/schedules/" + created.id()), HttpMethod.PATCH, bearer(token, Map.of("dayOfWeek", "THURSDAY", "startTime", "19:00", "endTime", "20:30")), ScheduleResponse.class).getBody();
        assertThat(updated.dayOfWeek().name()).isEqualTo("THURSDAY");
        assertThat(updated.startTime().toString()).isEqualTo("19:00");
    }

    @Test
    void rejectsInvalidScheduleTimesAndStaffRules() {
        Fixture fixture = fixture("rules", RoleName.OWNER);
        String token = login(fixture.tenant().getSlug(), fixture.user().getEmail());
        ClassResponse dojoClass = createClass(token, "Fundamentos", "GI", null);
        StaffProfile lead = staff(fixture, StaffType.PROFESSOR, "Lead", StaffStatus.ACTIVE);
        StaffProfile otherLead = staff(fixture, StaffType.INSTRUCTOR, "Other", StaffStatus.ACTIVE);
        StaffProfile inactive = staff(fixture, StaffType.PROFESSOR, "Inactive", StaffStatus.INACTIVE);

        assertThat(restTemplate.exchange(url("/api/v1/schedules"), HttpMethod.POST, bearer(token, scheduleRequest(dojoClass.id(), fixture.unit().getId(), "MONDAY", "10:00", "09:00", null, List.of())), String.class).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(restTemplate.exchange(url("/api/v1/schedules"), HttpMethod.POST, bearer(token, scheduleRequest(dojoClass.id(), fixture.unit().getId(), "MONDAY", "10:00", "11:00", null, List.of(staffReq(lead.getId(), "LEAD"), staffReq(lead.getId(), "ASSISTANT")))), String.class).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(restTemplate.exchange(url("/api/v1/schedules"), HttpMethod.POST, bearer(token, scheduleRequest(dojoClass.id(), fixture.unit().getId(), "MONDAY", "10:00", "11:00", null, List.of(staffReq(lead.getId(), "LEAD"), staffReq(otherLead.getId(), "LEAD")))), String.class).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(restTemplate.exchange(url("/api/v1/schedules"), HttpMethod.POST, bearer(token, scheduleRequest(dojoClass.id(), fixture.unit().getId(), "MONDAY", "10:00", "11:00", null, List.of(staffReq(inactive.getId(), "LEAD")))), String.class).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void enforcesTenantIsolationForClassesSchedulesUnitsAndStaff() {
        Fixture tenantA = fixture("tenant-a", RoleName.OWNER);
        Fixture tenantB = fixture("tenant-b", RoleName.OWNER);
        String tokenA = login(tenantA.tenant().getSlug(), tenantA.user().getEmail());
        String tokenB = login(tenantB.tenant().getSlug(), tenantB.user().getEmail());
        ClassResponse classB = createClass(tokenB, "Class B", "GI", 10);
        StaffProfile staffB = staff(tenantB, StaffType.PROFESSOR, "Staff B", StaffStatus.ACTIVE);
        ScheduleResponse scheduleB = createSchedule(tokenB, classB.id(), tenantB.unit().getId(), "TUESDAY", "20:30", "22:00", null, List.of(staffReq(staffB.getId(), "LEAD")));
        ClassResponse classA = createClass(tokenA, "Class A", "GI", 10);

        assertThat(restTemplate.exchange(url("/api/v1/classes/" + classB.id()), HttpMethod.GET, bearer(tokenA), String.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(restTemplate.exchange(url("/api/v1/classes/" + classB.id()), HttpMethod.PATCH, bearer(tokenA, Map.of("name", "Hack")), String.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(restTemplate.exchange(url("/api/v1/schedules/" + scheduleB.id()), HttpMethod.GET, bearer(tokenA), String.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(restTemplate.exchange(url("/api/v1/schedules/" + scheduleB.id()), HttpMethod.PATCH, bearer(tokenA, Map.of("status", "INACTIVE")), String.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(restTemplate.exchange(url("/api/v1/schedules"), HttpMethod.POST, bearer(tokenA, scheduleRequest(classB.id(), tenantA.unit().getId(), "MONDAY", "10:00", "11:00", null, List.of())), String.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(restTemplate.exchange(url("/api/v1/schedules"), HttpMethod.POST, bearer(tokenA, scheduleRequest(classA.id(), tenantB.unit().getId(), "MONDAY", "10:00", "11:00", null, List.of())), String.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(restTemplate.exchange(url("/api/v1/schedules"), HttpMethod.POST, bearer(tokenA, scheduleRequest(classA.id(), tenantA.unit().getId(), "MONDAY", "10:00", "11:00", null, List.of(staffReq(staffB.getId(), "LEAD")))), String.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void calculatesEffectiveCapacity() {
        Fixture fixture = fixture("capacity", RoleName.OWNER);
        String token = login(fixture.tenant().getSlug(), fixture.user().getEmail());
        ClassResponse withDefault = createClass(token, "Adulto", "GI", 30);
        ClassResponse withoutDefault = createClass(token, "Open Mat", "MIXED", null);

        assertThat(createSchedule(token, withDefault.id(), fixture.unit().getId(), "MONDAY", "08:00", "09:00", 18, List.of()).effectiveCapacity()).isEqualTo(18);
        assertThat(createSchedule(token, withDefault.id(), fixture.unit().getId(), "TUESDAY", "08:00", "09:00", null, List.of()).effectiveCapacity()).isEqualTo(30);
        assertThat(createSchedule(token, withoutDefault.id(), fixture.unit().getId(), "WEDNESDAY", "08:00", "09:00", null, List.of()).effectiveCapacity()).isNull();
    }

    @Test
    void rbacAllowsProfessorReadButNotCreate() {
        Fixture professor = fixture("professor", RoleName.PROFESSOR);
        String professorToken = login(professor.tenant().getSlug(), professor.user().getEmail());
        assertThat(restTemplate.exchange(url("/api/v1/classes"), HttpMethod.GET, bearer(professorToken), PageResponse.class).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(restTemplate.exchange(url("/api/v1/schedules/weekly"), HttpMethod.GET, bearer(professorToken), WeeklyScheduleResponse.class).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(restTemplate.exchange(url("/api/v1/classes"), HttpMethod.POST, bearer(professorToken, classRequest("Forbidden", "GI", 10)), String.class).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private ClassResponse createClass(String token, String name, String modality, Integer capacity) {
        return restTemplate.exchange(url("/api/v1/classes"), HttpMethod.POST, bearer(token, classRequest(name, modality, capacity)), ClassResponse.class).getBody();
    }

    private Map<String, Object> classRequest(String name, String modality, Integer capacity) {
        java.util.HashMap<String, Object> body = new java.util.HashMap<>();
        body.put("name", name);
        body.put("description", "");
        body.put("modality", modality);
        body.put("audience", "ADULT");
        body.put("classType", "REGULAR");
        body.put("level", "ALL_LEVELS");
        body.put("status", "ACTIVE");
        if (capacity != null) body.put("defaultCapacity", capacity);
        return body;
    }

    private ScheduleResponse createSchedule(String token, UUID classId, UUID unitId, String day, String start, String end, Integer capacity, List<Map<String, Object>> staff) {
        return restTemplate.exchange(url("/api/v1/schedules"), HttpMethod.POST, bearer(token, scheduleRequest(classId, unitId, day, start, end, capacity, staff)), ScheduleResponse.class).getBody();
    }

    private Map<String, Object> scheduleRequest(UUID classId, UUID unitId, String day, String start, String end, Integer capacity, List<Map<String, Object>> staff) {
        java.util.HashMap<String, Object> body = new java.util.HashMap<>();
        body.put("classId", classId);
        body.put("unitId", unitId);
        body.put("dayOfWeek", day);
        body.put("startTime", start);
        body.put("endTime", end);
        body.put("status", "ACTIVE");
        if (capacity != null) body.put("capacityOverride", capacity);
        body.put("staff", staff);
        return body;
    }

    private Map<String, Object> staffReq(UUID staffId, String role) {
        return Map.of("staffId", staffId, "role", role);
    }

    private StaffProfile staff(Fixture fixture, StaffType type, String displayName, StaffStatus status) {
        Role role = roleRepository.findByName(type == StaffType.PROFESSOR ? RoleName.PROFESSOR : RoleName.INSTRUCTOR).orElseThrow();
        User user = userRepository.save(new User(fixture.tenant(), displayName, displayName.toLowerCase().replace(" ", ".") + "@" + fixture.tenant().getSlug() + ".test", passwordEncoder.encode("secret"), Set.of(role)));
        StaffProfile staff = new StaffProfile(fixture.tenant(), user, type, displayName);
        staff.setStatus(status);
        staff.setPrimaryUnit(fixture.unit());
        return staffRepository.save(staff);
    }

    private String login(String tenant, String email) {
        TokenResponse response = restTemplate.postForEntity(url("/api/v1/auth/login"), Map.of("tenant", tenant, "email", email, "password", "secret"), TokenResponse.class).getBody();
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

    private String url(String path) { return "http://localhost:" + port + path; }

    private record Fixture(Tenant tenant, Unit unit, User user) {}
}

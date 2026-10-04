package br.com.mecadojo.api.training;

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
import br.com.mecadojo.api.schedule.domain.ClassAudience;
import br.com.mecadojo.api.schedule.domain.ClassLevel;
import br.com.mecadojo.api.schedule.domain.ClassModality;
import br.com.mecadojo.api.schedule.domain.ClassType;
import br.com.mecadojo.api.schedule.domain.DojoClass;
import br.com.mecadojo.api.schedule.repository.DojoClassRepository;
import br.com.mecadojo.api.support.AbstractPostgreSqlTestcontainer;
import br.com.mecadojo.api.training.api.dto.TrainingSessionDtos.GenerateTrainingSessionsResponse;
import br.com.mecadojo.api.training.api.dto.TrainingSessionDtos.TrainingSessionResponse;
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
class TrainingSessionIntegrationTests extends AbstractPostgreSqlTestcontainer {
    @LocalServerPort int port;
    @Autowired TestRestTemplate restTemplate;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired TenantRepository tenantRepository;
    @Autowired UnitRepository unitRepository;
    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired StaffProfileRepository staffRepository;
    @Autowired DojoClassRepository dojoClassRepository;
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
    void generatesSessionsFromScheduleCopiesSnapshotStaffAndIsIdempotent() {
        Fixture fixture = fixture("generate", RoleName.OWNER);
        String token = login(fixture.tenant().getSlug(), fixture.user().getEmail());
        ClassResponse dojoClass = createClass(token, "Jiu-Jitsu Adulto", 30);
        StaffProfile lead = staff(fixture, StaffType.PROFESSOR, "Sensei", StaffStatus.ACTIVE);
        ScheduleResponse schedule = createSchedule(token, dojoClass.id(), fixture.unit().getId(), "TUESDAY", "20:30", "22:00", 24, List.of(staffReq(lead.getId(), "LEAD")));
        LocalDate from = nextOrSame(java.time.DayOfWeek.MONDAY);
        LocalDate to = from.plusDays(6);

        GenerateTrainingSessionsResponse first = generate(token, from, to, fixture.unit().getId());
        GenerateTrainingSessionsResponse second = generate(token, from, to, fixture.unit().getId());

        assertThat(first.created()).isEqualTo(1);
        assertThat(second.created()).isZero();
        assertThat(second.existing()).isEqualTo(1);
        TrainingSessionResponse session = firstSession(token, from, to);
        assertThat(session.classSchedule().id()).isEqualTo(schedule.id());
        assertThat(session.dojoClass().id()).isEqualTo(dojoClass.id());
        assertThat(session.unit().id()).isEqualTo(fixture.unit().getId());
        assertThat(session.startTime().toString()).isEqualTo("20:30");
        assertThat(session.capacity()).isEqualTo(24);
        assertThat(session.staff()).hasSize(1);
    }

    @Test
    void rejectsGenerationRangeAboveLimit() {
        Fixture fixture = fixture("range", RoleName.OWNER);
        String token = login(fixture.tenant().getSlug(), fixture.user().getEmail());
        LocalDate from = LocalDate.now();

        assertThat(restTemplate.exchange(url("/api/v1/training-sessions/generate"), HttpMethod.POST, bearer(token, Map.of("from", from.toString(), "to", from.plusDays(31).toString())), String.class).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void manualSessionHasNoScheduleAndCanBeOpenedCompletedOrCancelledWithReason() {
        Fixture fixture = fixture("manual", RoleName.OWNER);
        String token = login(fixture.tenant().getSlug(), fixture.user().getEmail());
        ClassResponse dojoClass = createClass(token, "Treino Especial", null);
        StaffProfile lead = staff(fixture, StaffType.PROFESSOR, "Sensei", StaffStatus.ACTIVE);
        TrainingSessionResponse manual = createManual(token, dojoClass.id(), fixture.unit().getId(), LocalDate.now(), "14:00", "16:00", List.of(staffReq(lead.getId(), "LEAD")));

        assertThat(manual.classSchedule()).isNull();
        assertThat(manual.status().name()).isEqualTo("SCHEDULED");
        TrainingSessionResponse open = action(token, manual.id(), "open");
        assertThat(open.status().name()).isEqualTo("OPEN");
        TrainingSessionResponse completed = action(token, manual.id(), "complete");
        assertThat(completed.status().name()).isEqualTo("COMPLETED");
        assertThat(restTemplate.exchange(url("/api/v1/training-sessions/" + manual.id() + "/open"), HttpMethod.POST, bearer(token, Map.of()), String.class).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(restTemplate.exchange(url("/api/v1/training-sessions/" + manual.id() + "/cancel"), HttpMethod.POST, bearer(token, Map.of("reason", "late cancel")), String.class).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        TrainingSessionResponse cancellable = createManual(token, dojoClass.id(), fixture.unit().getId(), LocalDate.now().plusDays(1), "10:00", "11:00", List.of());
        assertThat(restTemplate.exchange(url("/api/v1/training-sessions/" + cancellable.id() + "/cancel"), HttpMethod.POST, bearer(token, Map.of("reason", " ")), String.class).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        TrainingSessionResponse cancelled = restTemplate.exchange(url("/api/v1/training-sessions/" + cancellable.id() + "/cancel"), HttpMethod.POST, bearer(token, Map.of("reason", "Professor indisponivel")), TrainingSessionResponse.class).getBody();
        assertThat(cancelled.status().name()).isEqualTo("CANCELLED");
        assertThat(cancelled.cancelledAt()).isNotNull();
        assertThat(cancelled.cancelledBy()).isNotNull();
        assertThat(cancelled.cancellationReason()).isEqualTo("Professor indisponivel");

        TrainingSessionResponse openCancellable = action(token, createManual(token, dojoClass.id(), fixture.unit().getId(), LocalDate.now().plusDays(2), "12:00", "13:00", List.of()).id(), "open");
        TrainingSessionResponse openCancelled = restTemplate.exchange(url("/api/v1/training-sessions/" + openCancellable.id() + "/cancel"), HttpMethod.POST, bearer(token, Map.of("reason", "Unidade fechada")), TrainingSessionResponse.class).getBody();
        assertThat(openCancelled.status().name()).isEqualTo("CANCELLED");
    }

    @Test
    void rejectsInvalidStateTransitions() {
        Fixture fixture = fixture("states", RoleName.OWNER);
        String token = login(fixture.tenant().getSlug(), fixture.user().getEmail());
        ClassResponse dojoClass = createClass(token, "Adulto", null);
        TrainingSessionResponse scheduled = createManual(token, dojoClass.id(), fixture.unit().getId(), LocalDate.now(), "10:00", "11:00", List.of());
        assertThat(restTemplate.exchange(url("/api/v1/training-sessions/" + scheduled.id() + "/complete"), HttpMethod.POST, bearer(token, Map.of()), String.class).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        TrainingSessionResponse cancelled = restTemplate.exchange(url("/api/v1/training-sessions/" + scheduled.id() + "/cancel"), HttpMethod.POST, bearer(token, Map.of("reason", "Cancelado")), TrainingSessionResponse.class).getBody();
        assertThat(restTemplate.exchange(url("/api/v1/training-sessions/" + cancelled.id() + "/open"), HttpMethod.POST, bearer(token, Map.of()), String.class).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(restTemplate.exchange(url("/api/v1/training-sessions/" + cancelled.id() + "/complete"), HttpMethod.POST, bearer(token, Map.of()), String.class).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void editsScheduledSessionAndDoesNotMutateScheduleSnapshot() {
        Fixture fixture = fixture("snapshot", RoleName.OWNER);
        String token = login(fixture.tenant().getSlug(), fixture.user().getEmail());
        ClassResponse dojoClass = createClass(token, "Adulto", 30);
        StaffProfile original = staff(fixture, StaffType.PROFESSOR, "Original", StaffStatus.ACTIVE);
        StaffProfile substitute = staff(fixture, StaffType.INSTRUCTOR, "Substitute", StaffStatus.ACTIVE);
        ScheduleResponse schedule = createSchedule(token, dojoClass.id(), fixture.unit().getId(), "TUESDAY", "20:30", "22:00", null, List.of(staffReq(original.getId(), "LEAD")));
        LocalDate from = nextOrSame(java.time.DayOfWeek.MONDAY);
        generate(token, from, from.plusDays(6), fixture.unit().getId());
        TrainingSessionResponse session = firstSession(token, from, from.plusDays(6));

        TrainingSessionResponse edited = restTemplate.exchange(url("/api/v1/training-sessions/" + session.id()), HttpMethod.PATCH, bearer(token, Map.of("startTime", "21:00", "endTime", "22:30", "staff", List.of(staffReq(substitute.getId(), "LEAD")), "notes", "Professor substituto")), TrainingSessionResponse.class).getBody();
        assertThat(edited.startTime().toString()).isEqualTo("21:00");
        assertThat(edited.staff().getFirst().id()).isEqualTo(substitute.getId());

        ScheduleResponse unchanged = restTemplate.exchange(url("/api/v1/schedules/" + schedule.id()), HttpMethod.GET, bearer(token), ScheduleResponse.class).getBody();
        assertThat(unchanged.startTime().toString()).isEqualTo("20:30");
        assertThat(unchanged.staff().getFirst().id()).isEqualTo(original.getId());
        action(token, edited.id(), "open");
        assertThat(restTemplate.exchange(url("/api/v1/training-sessions/" + edited.id()), HttpMethod.PATCH, bearer(token, Map.of("unitId", fixture.unit().getId())), String.class).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void generatedSessionKeepsSnapshotAfterScheduleChanges() {
        Fixture fixture = fixture("schedule-snapshot", RoleName.OWNER);
        String token = login(fixture.tenant().getSlug(), fixture.user().getEmail());
        ClassResponse dojoClass = createClass(token, "Snapshot Adulto", 30);
        StaffProfile original = staff(fixture, StaffType.PROFESSOR, "Original Snapshot", StaffStatus.ACTIVE);
        StaffProfile replacement = staff(fixture, StaffType.PROFESSOR, "Replacement Snapshot", StaffStatus.ACTIVE);
        ScheduleResponse schedule = createSchedule(token, dojoClass.id(), fixture.unit().getId(), "TUESDAY", "20:30", "22:00", 24, List.of(staffReq(original.getId(), "LEAD")));
        LocalDate from = nextOrSame(java.time.DayOfWeek.MONDAY);
        generate(token, from, from.plusDays(6), fixture.unit().getId());
        TrainingSessionResponse before = firstSession(token, from, from.plusDays(6));

        restTemplate.exchange(url("/api/v1/schedules/" + schedule.id()), HttpMethod.PATCH, bearer(token, Map.of("startTime", "21:00", "endTime", "22:30", "capacityOverride", 18, "staff", List.of(staffReq(replacement.getId(), "LEAD")))), ScheduleResponse.class);
        TrainingSessionResponse after = restTemplate.exchange(url("/api/v1/training-sessions/" + before.id()), HttpMethod.GET, bearer(token), TrainingSessionResponse.class).getBody();

        assertThat(after.startTime().toString()).isEqualTo("20:30");
        assertThat(after.endTime().toString()).isEqualTo("22:00");
        assertThat(after.capacity()).isEqualTo(24);
        assertThat(after.staff().getFirst().id()).isEqualTo(original.getId());
    }

    @Test
    void enforcesTenantIsolationForReferencesAndSessionActions() {
        Fixture a = fixture("tenant-a", RoleName.OWNER);
        Fixture b = fixture("tenant-b", RoleName.OWNER);
        String tokenA = login(a.tenant().getSlug(), a.user().getEmail());
        String tokenB = login(b.tenant().getSlug(), b.user().getEmail());
        ClassResponse classA = createClass(tokenA, "A", null);
        ClassResponse classB = createClass(tokenB, "B", null);
        StaffProfile staffB = staff(b, StaffType.PROFESSOR, "Staff B", StaffStatus.ACTIVE);
        TrainingSessionResponse sessionB = createManual(tokenB, classB.id(), b.unit().getId(), LocalDate.now(), "10:00", "11:00", List.of(staffReq(staffB.getId(), "LEAD")));
        ScheduleResponse scheduleB = createSchedule(tokenB, classB.id(), b.unit().getId(), "MONDAY", "10:00", "11:00", null, List.of());

        assertThat(restTemplate.exchange(url("/api/v1/training-sessions/" + sessionB.id()), HttpMethod.GET, bearer(tokenA), String.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(restTemplate.exchange(url("/api/v1/training-sessions/" + sessionB.id() + "/open"), HttpMethod.POST, bearer(tokenA, Map.of()), String.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(restTemplate.exchange(url("/api/v1/training-sessions"), HttpMethod.POST, bearer(tokenA, manualRequest(classB.id(), a.unit().getId(), LocalDate.now(), "09:00", "10:00", List.of())), String.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(restTemplate.exchange(url("/api/v1/training-sessions"), HttpMethod.POST, bearer(tokenA, manualRequest(classA.id(), b.unit().getId(), LocalDate.now(), "09:00", "10:00", List.of())), String.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(restTemplate.exchange(url("/api/v1/training-sessions"), HttpMethod.POST, bearer(tokenA, manualRequest(classA.id(), a.unit().getId(), LocalDate.now(), "09:00", "10:00", List.of(staffReq(staffB.getId(), "LEAD")))), String.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(restTemplate.exchange(url("/api/v1/training-sessions/generate"), HttpMethod.POST, bearer(tokenA, Map.of("from", LocalDate.now().toString(), "to", LocalDate.now().toString(), "unitId", b.unit().getId())), String.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(scheduleB.id()).isNotNull();
    }

    @Test
    void rbacForCriticalTrainingSessionActions() {
        Fixture reception = fixture("reception", RoleName.RECEPTION);
        String receptionToken = login(reception.tenant().getSlug(), reception.user().getEmail());
        ClassResponse dojoClass = directClass(reception, "Recepcao Aula");
        TrainingSessionResponse session = createManual(receptionToken, dojoClass.id(), reception.unit().getId(), LocalDate.now(), "10:00", "11:00", List.of());
        assertThat(session.id()).isNotNull();
        assertThat(restTemplate.exchange(url("/api/v1/training-sessions/" + session.id() + "/open"), HttpMethod.POST, bearer(receptionToken, Map.of()), String.class).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(restTemplate.exchange(url("/api/v1/training-sessions/" + session.id() + "/cancel"), HttpMethod.POST, bearer(receptionToken, Map.of("reason", "Recepcao cancela")), TrainingSessionResponse.class).getStatusCode()).isEqualTo(HttpStatus.OK);

        Fixture instructor = fixture("instructor", RoleName.INSTRUCTOR);
        String instructorToken = login(instructor.tenant().getSlug(), instructor.user().getEmail());
        ClassResponse instructorClass = directClass(instructor, "Instrutor Aula");
        TrainingSessionResponse instructorSession = directSession(instructor, instructorClass.id(), "12:00", "13:00");
        assertThat(restTemplate.exchange(url("/api/v1/training-sessions/" + instructorSession.id() + "/open"), HttpMethod.POST, bearer(instructorToken, Map.of()), TrainingSessionResponse.class).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(restTemplate.exchange(url("/api/v1/training-sessions/" + instructorSession.id() + "/cancel"), HttpMethod.POST, bearer(instructorToken, Map.of("reason", "No")), String.class).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private ClassResponse directClass(Fixture fixture, String name) {
        DojoClass dojoClass = dojoClassRepository.save(new DojoClass(fixture.tenant(), name, ClassModality.GI, ClassAudience.ADULT, ClassType.REGULAR, ClassLevel.ALL_LEVELS));
        return new ClassResponse(dojoClass.getId(), dojoClass.getName(), dojoClass.getDescription(), dojoClass.getModality(), dojoClass.getAudience(), dojoClass.getClassType(), dojoClass.getLevel(), dojoClass.getStatus(), dojoClass.getDefaultCapacity());
    }

    private TrainingSessionResponse directSession(Fixture fixture, UUID classId, String start, String end) {
        Role owner = roleRepository.findByName(RoleName.OWNER).orElseThrow();
        User ownerUser = userRepository.save(new User(fixture.tenant(), "Owner " + fixture.tenant().getSlug(), "owner-" + UUID.randomUUID() + "@" + fixture.tenant().getSlug() + ".test", passwordEncoder.encode("secret"), Set.of(owner)));
        String ownerToken = login(fixture.tenant().getSlug(), ownerUser.getEmail());
        TrainingSessionResponse response = createManual(ownerToken, classId, fixture.unit().getId(), LocalDate.now(), start, end, List.of());
        return response;
    }

    private ClassResponse createClass(String token, String name, Integer capacity) {
        return restTemplate.exchange(url("/api/v1/classes"), HttpMethod.POST, bearer(token, classRequest(name, capacity)), ClassResponse.class).getBody();
    }

    private Map<String, Object> classRequest(String name, Integer capacity) {
        java.util.HashMap<String, Object> body = new java.util.HashMap<>();
        body.put("name", name); body.put("modality", "GI"); body.put("audience", "ADULT"); body.put("classType", "REGULAR"); body.put("level", "ALL_LEVELS"); body.put("status", "ACTIVE");
        if (capacity != null) body.put("defaultCapacity", capacity);
        return body;
    }

    private ScheduleResponse createSchedule(String token, UUID classId, UUID unitId, String day, String start, String end, Integer capacity, List<Map<String, Object>> staff) {
        java.util.HashMap<String, Object> body = new java.util.HashMap<>();
        body.put("classId", classId); body.put("unitId", unitId); body.put("dayOfWeek", day); body.put("startTime", start); body.put("endTime", end); body.put("status", "ACTIVE"); body.put("staff", staff);
        if (capacity != null) body.put("capacityOverride", capacity);
        return restTemplate.exchange(url("/api/v1/schedules"), HttpMethod.POST, bearer(token, body), ScheduleResponse.class).getBody();
    }

    private GenerateTrainingSessionsResponse generate(String token, LocalDate from, LocalDate to, UUID unitId) {
        return restTemplate.exchange(url("/api/v1/training-sessions/generate"), HttpMethod.POST, bearer(token, Map.of("from", from.toString(), "to", to.toString(), "unitId", unitId)), GenerateTrainingSessionsResponse.class).getBody();
    }

    private TrainingSessionResponse firstSession(String token, LocalDate from, LocalDate to) {
        PageResponse page = restTemplate.exchange(url("/api/v1/training-sessions?from=" + from + "&to=" + to), HttpMethod.GET, bearer(token), PageResponse.class).getBody();
        java.util.Map first = (java.util.Map) page.content().getFirst();
        return restTemplate.exchange(url("/api/v1/training-sessions/" + first.get("id")), HttpMethod.GET, bearer(token), TrainingSessionResponse.class).getBody();
    }

    private TrainingSessionResponse createManual(String token, UUID classId, UUID unitId, LocalDate date, String start, String end, List<Map<String, Object>> staff) {
        return restTemplate.exchange(url("/api/v1/training-sessions"), HttpMethod.POST, bearer(token, manualRequest(classId, unitId, date, start, end, staff)), TrainingSessionResponse.class).getBody();
    }

    private Map<String, Object> manualRequest(UUID classId, UUID unitId, LocalDate date, String start, String end, List<Map<String, Object>> staff) {
        return Map.of("dojoClassId", classId, "unitId", unitId, "sessionDate", date.toString(), "startTime", start, "endTime", end, "capacity", 20, "notes", "Teste", "staff", staff);
    }

    private TrainingSessionResponse action(String token, UUID id, String action) {
        return restTemplate.exchange(url("/api/v1/training-sessions/" + id + "/" + action), HttpMethod.POST, bearer(token, Map.of()), TrainingSessionResponse.class).getBody();
    }

    private Map<String, Object> staffReq(UUID staffId, String role) { return Map.of("staffId", staffId, "role", role); }

    private StaffProfile staff(Fixture fixture, StaffType type, String displayName, StaffStatus status) {
        Role role = roleRepository.findByName(type == StaffType.PROFESSOR ? RoleName.PROFESSOR : RoleName.INSTRUCTOR).orElseThrow();
        User user = userRepository.save(new User(fixture.tenant(), displayName, displayName.toLowerCase().replace(" ", ".") + "@" + fixture.tenant().getSlug() + ".test", passwordEncoder.encode("secret"), Set.of(role)));
        StaffProfile staff = new StaffProfile(fixture.tenant(), user, type, displayName);
        staff.setStatus(status); staff.setPrimaryUnit(fixture.unit());
        return staffRepository.save(staff);
    }

    private LocalDate nextOrSame(java.time.DayOfWeek day) {
        LocalDate date = LocalDate.now();
        while (date.getDayOfWeek() != day) date = date.plusDays(1);
        return date;
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

    private HttpEntity<Void> bearer(String accessToken) { HttpHeaders headers = new HttpHeaders(); headers.setBearerAuth(accessToken); return new HttpEntity<>(headers); }
    private HttpEntity<Object> bearer(String accessToken, Object body) { HttpHeaders headers = new HttpHeaders(); headers.setBearerAuth(accessToken); return new HttpEntity<>(body, headers); }
    private String url(String path) { return "http://localhost:" + port + path; }
    private record Fixture(Tenant tenant, Unit unit, User user) {}
}


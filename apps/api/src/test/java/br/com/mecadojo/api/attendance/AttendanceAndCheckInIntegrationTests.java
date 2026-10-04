package br.com.mecadojo.api.attendance;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.mecadojo.api.attendance.api.dto.AttendanceDtos.AttendanceResponse;
import br.com.mecadojo.api.attendance.api.dto.AttendanceDtos.CheckInResponse;
import br.com.mecadojo.api.attendance.api.dto.AttendanceDtos.CreateCheckInTokenResponse;
import br.com.mecadojo.api.attendance.api.dto.AttendanceDtos.StudentAttendanceSummary;
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
import br.com.mecadojo.api.people.domain.Student;
import br.com.mecadojo.api.people.domain.StudentStatus;
import br.com.mecadojo.api.people.repository.StudentRepository;
import br.com.mecadojo.api.schedule.api.dto.ScheduleDtos.ClassResponse;
import br.com.mecadojo.api.schedule.domain.ClassAudience;
import br.com.mecadojo.api.schedule.domain.ClassLevel;
import br.com.mecadojo.api.schedule.domain.ClassModality;
import br.com.mecadojo.api.schedule.domain.ClassType;
import br.com.mecadojo.api.schedule.domain.DojoClass;
import br.com.mecadojo.api.schedule.repository.DojoClassRepository;
import br.com.mecadojo.api.support.AbstractPostgreSqlTestcontainer;
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
class AttendanceAndCheckInIntegrationTests extends AbstractPostgreSqlTestcontainer {
    @LocalServerPort int port;
    @Autowired TestRestTemplate restTemplate;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired TenantRepository tenantRepository;
    @Autowired UnitRepository unitRepository;
    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired StudentRepository studentRepository;
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
    void manualAttendanceRequiresOpenSessionValidStudentPreventsDuplicatesAndSupportsVoid() {
        Fixture fixture = fixture("manual-attendance", RoleName.OWNER);
        String token = login(fixture.tenant().getSlug(), fixture.user().getEmail());
        ClassResponse dojoClass = directClass(fixture, "Jiu-Jitsu Adulto", ClassModality.GI);
        TrainingSessionResponse scheduled = createManual(token, dojoClass.id(), fixture.unit().getId(), "10:00", "11:00");
        Student active = student(fixture, "Aluno Ativo", StudentStatus.ACTIVE, null);
        Student trial = student(fixture, "Aluno Trial", StudentStatus.TRIAL, null);
        Student inactive = student(fixture, "Aluno Inativo", StudentStatus.INACTIVE, null);
        Student suspended = student(fixture, "Aluno Suspenso", StudentStatus.SUSPENDED, null);

        assertThat(postAttendance(token, scheduled.id(), active.getId(), String.class).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        TrainingSessionResponse open = action(token, scheduled.id(), "open");
        AttendanceResponse attendance = postAttendance(token, open.id(), active.getId(), AttendanceResponse.class).getBody();
        assertThat(attendance.status().name()).isEqualTo("ACTIVE");
        assertThat(attendance.source().name()).isEqualTo("MANUAL");
        assertThat(attendance.recordedBy()).isNotNull();
        postAttendance(token, open.id(), active.getId(), AttendanceResponse.class);
        AttendanceResponse trialAttendance = postAttendance(token, open.id(), trial.getId(), AttendanceResponse.class).getBody();
        assertThat(trialAttendance.student().id()).isEqualTo(trial.getId());
        assertThat(countActive(open.id(), active.getId())).isEqualTo(1);
        assertThat(postAttendance(token, open.id(), inactive.getId(), String.class).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(postAttendance(token, open.id(), suspended.getId(), String.class).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        AttendanceResponse voided = restTemplate.exchange(url("/api/v1/attendance/" + attendance.id() + "/void"), HttpMethod.POST, bearer(token, Map.of("reason", "Aluno errado")), AttendanceResponse.class).getBody();
        assertThat(voided.status().name()).isEqualTo("VOIDED");
        assertThat(voided.voidedAt()).isNotNull();
        assertThat(voided.voidedBy()).isNotNull();
        assertThat(voided.voidReason()).isEqualTo("Aluno errado");
        assertThat(listAttendance(token, open.id(), false)).hasSize(1);
        AttendanceResponse recreated = postAttendance(token, open.id(), active.getId(), AttendanceResponse.class).getBody();
        assertThat(recreated.id()).isNotEqualTo(attendance.id());
        assertThat(countActive(open.id(), active.getId())).isEqualTo(1);
        assertThat(restTemplate.exchange(url("/api/v1/attendance/" + recreated.id() + "/void"), HttpMethod.POST, bearer(token, Map.of("reason", " ")), String.class).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        action(token, open.id(), "complete");
        assertThat(postAttendance(token, open.id(), active.getId(), String.class).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        TrainingSessionResponse cancelled = createManual(token, dojoClass.id(), fixture.unit().getId(), "12:00", "13:00");
        restTemplate.exchange(url("/api/v1/training-sessions/" + cancelled.id() + "/cancel"), HttpMethod.POST, bearer(token, Map.of("reason", "Cancelado")), TrainingSessionResponse.class);
        assertThat(postAttendance(token, cancelled.id(), active.getId(), String.class).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void qrTokenSelfCheckInDuplicateExpiredClosedAndUnlinkedFlowsAreAudited() {
        Fixture owner = fixture("qr-flow", RoleName.OWNER);
        String ownerToken = login(owner.tenant().getSlug(), owner.user().getEmail());
        ClassResponse dojoClass = directClass(owner, "No-Gi", ClassModality.NO_GI);
        TrainingSessionResponse session = action(ownerToken, createManual(ownerToken, dojoClass.id(), owner.unit().getId(), "20:00", "21:00").id(), "open");
        User studentUser = user(owner, "Aluno Login", "student@qr-flow.test", RoleName.STUDENT);
        Student student = student(owner, "Aluno Login", StudentStatus.ACTIVE, studentUser);
        User otherUser = user(owner, "Outro Aluno", "other@qr-flow.test", RoleName.STUDENT);
        student(owner, "Outro Aluno", StudentStatus.ACTIVE, otherUser);
        String studentToken = login(owner.tenant().getSlug(), studentUser.getEmail());
        String otherToken = login(owner.tenant().getSlug(), otherUser.getEmail());

        CreateCheckInTokenResponse token = restTemplate.exchange(url("/api/v1/training-sessions/" + session.id() + "/check-in-token"), HttpMethod.POST, bearer(ownerToken, Map.of()), CreateCheckInTokenResponse.class).getBody();
        assertThat(token.token()).isNotBlank();
        assertThat(token.checkInUrl()).contains("/check-in?token=");
        String storedHash = jdbcTemplate.queryForObject("select token_hash from check_in_tokens where training_session_id = ?", String.class, session.id());
        assertThat(storedHash).isNotEqualTo(token.token());
        assertThat(storedHash).hasSize(64);

        CheckInResponse accepted = restTemplate.exchange(url("/api/v1/check-ins"), HttpMethod.POST, bearer(studentToken, Map.of("token", token.token())), CheckInResponse.class).getBody();
        assertThat(accepted.status().name()).isEqualTo("ACCEPTED");
        assertThat(accepted.attendance().student().id()).isEqualTo(student.getId());
        CheckInResponse duplicate = restTemplate.exchange(url("/api/v1/check-ins"), HttpMethod.POST, bearer(studentToken, Map.of("token", token.token())), CheckInResponse.class).getBody();
        assertThat(duplicate.status().name()).isEqualTo("DUPLICATE");
        assertThat(countActive(session.id(), student.getId())).isEqualTo(1);

        jdbcTemplate.update("update check_in_tokens set expires_at = now() - interval '1 second' where token_hash = ?", storedHash);
        CheckInResponse expired = restTemplate.exchange(url("/api/v1/check-ins"), HttpMethod.POST, bearer(otherToken, Map.of("token", token.token())), CheckInResponse.class).getBody();
        assertThat(expired.status().name()).isEqualTo("EXPIRED");

        CreateCheckInTokenResponse closingToken = restTemplate.exchange(url("/api/v1/training-sessions/" + session.id() + "/check-in-token"), HttpMethod.POST, bearer(ownerToken, Map.of()), CreateCheckInTokenResponse.class).getBody();
        action(ownerToken, session.id(), "complete");
        CheckInResponse closed = restTemplate.exchange(url("/api/v1/check-ins"), HttpMethod.POST, bearer(otherToken, Map.of("token", closingToken.token())), CheckInResponse.class).getBody();
        assertThat(closed.status().name()).isEqualTo("SESSION_CLOSED");

        TrainingSessionResponse unlinkedSession = action(ownerToken, createManual(ownerToken, dojoClass.id(), owner.unit().getId(), "22:00", "23:00").id(), "open");
        CreateCheckInTokenResponse unlinkedToken = restTemplate.exchange(url("/api/v1/training-sessions/" + unlinkedSession.id() + "/check-in-token"), HttpMethod.POST, bearer(ownerToken, Map.of()), CreateCheckInTokenResponse.class).getBody();
        User unlinkedUser = user(owner, "Sem Student", "unlinked@qr-flow.test", RoleName.STUDENT);
        String unlinkedUserToken = login(owner.tenant().getSlug(), unlinkedUser.getEmail());
        CheckInResponse unlinked = restTemplate.exchange(url("/api/v1/check-ins"), HttpMethod.POST, bearer(unlinkedUserToken, Map.of("token", unlinkedToken.token())), CheckInResponse.class).getBody();
        assertThat(unlinked.status().name()).isEqualTo("UNLINKED_USER");
    }

    @Test
    void tenantIsolationAndRbacAreEnforcedForAttendanceAndCheckIn() {
        Fixture a = fixture("attendance-a", RoleName.OWNER);
        Fixture b = fixture("attendance-b", RoleName.OWNER);
        String tokenA = login(a.tenant().getSlug(), a.user().getEmail());
        String tokenB = login(b.tenant().getSlug(), b.user().getEmail());
        ClassResponse classA = directClass(a, "A", ClassModality.GI);
        ClassResponse classB = directClass(b, "B", ClassModality.GI);
        TrainingSessionResponse sessionA = action(tokenA, createManual(tokenA, classA.id(), a.unit().getId(), "10:00", "11:00").id(), "open");
        TrainingSessionResponse sessionB = action(tokenB, createManual(tokenB, classB.id(), b.unit().getId(), "10:00", "11:00").id(), "open");
        Student studentA = student(a, "Aluno A", StudentStatus.ACTIVE, null);
        Student studentB = student(b, "Aluno B", StudentStatus.ACTIVE, null);
        AttendanceResponse attendanceA = postAttendance(tokenA, sessionA.id(), studentA.getId(), AttendanceResponse.class).getBody();

        assertThat(postAttendance(tokenA, sessionA.id(), studentB.getId(), String.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(postAttendance(tokenA, sessionB.id(), studentA.getId(), String.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(restTemplate.exchange(url("/api/v1/training-sessions/" + sessionB.id() + "/attendance"), HttpMethod.GET, bearer(tokenA), String.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(restTemplate.exchange(url("/api/v1/attendance/" + attendanceA.id() + "/void"), HttpMethod.POST, bearer(tokenB, Map.of("reason", "cross")), String.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(restTemplate.exchange(url("/api/v1/training-sessions/" + sessionB.id() + "/check-in-token"), HttpMethod.POST, bearer(tokenA, Map.of()), String.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        Fixture instructor = fixture("att-instructor", RoleName.INSTRUCTOR);
        String instructorToken = login(instructor.tenant().getSlug(), instructor.user().getEmail());
        ClassResponse instructorClass = directClass(instructor, "Instrutor", ClassModality.GI);
        TrainingSessionResponse instructorSession = directOwnerSession(instructor, instructorClass.id());
        Student instructorStudent = student(instructor, "Aluno Instrutor", StudentStatus.ACTIVE, null);
        restTemplate.exchange(url("/api/v1/training-sessions/" + instructorSession.id() + "/open"), HttpMethod.POST, bearer(instructorToken, Map.of()), TrainingSessionResponse.class);
        AttendanceResponse instructorAttendance = postAttendance(instructorToken, instructorSession.id(), instructorStudent.getId(), AttendanceResponse.class).getBody();
        assertThat(instructorAttendance.id()).isNotNull();
        assertThat(restTemplate.exchange(url("/api/v1/training-sessions/" + instructorSession.id() + "/check-in-token"), HttpMethod.POST, bearer(instructorToken, Map.of()), CreateCheckInTokenResponse.class).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(restTemplate.exchange(url("/api/v1/attendance/" + instructorAttendance.id() + "/void"), HttpMethod.POST, bearer(instructorToken, Map.of("reason", "no")), String.class).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        Fixture reception = fixture("att-reception", RoleName.RECEPTION);
        String receptionToken = login(reception.tenant().getSlug(), reception.user().getEmail());
        ClassResponse receptionClass = directClass(reception, "Recepcao", ClassModality.GI);
        TrainingSessionResponse receptionSession = directOwnerSession(reception, receptionClass.id());
        Student receptionStudent = student(reception, "Aluno Recepcao", StudentStatus.ACTIVE, null);
        restTemplate.exchange(url("/api/v1/training-sessions/" + receptionSession.id() + "/open"), HttpMethod.POST, bearer(loginAsOwner(reception), Map.of()), TrainingSessionResponse.class);
        AttendanceResponse receptionAttendance = postAttendance(receptionToken, receptionSession.id(), receptionStudent.getId(), AttendanceResponse.class).getBody();
        assertThat(restTemplate.exchange(url("/api/v1/attendance/" + receptionAttendance.id() + "/void"), HttpMethod.POST, bearer(receptionToken, Map.of("reason", "corrigir")), AttendanceResponse.class).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(restTemplate.exchange(url("/api/v1/training-sessions/" + receptionSession.id() + "/check-in-token"), HttpMethod.POST, bearer(receptionToken, Map.of()), String.class).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        String studentToken = login(a.tenant().getSlug(), user(a, "Student Role", "student-role@attendance-a.test", RoleName.STUDENT).getEmail());
        assertThat(postAttendance(studentToken, sessionA.id(), studentA.getId(), String.class).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void attendanceSummaryUsesOnlyActiveAttendancesAndCountsModalities() {
        Fixture fixture = fixture("summary", RoleName.OWNER);
        String token = login(fixture.tenant().getSlug(), fixture.user().getEmail());
        Student student = student(fixture, "Aluno Summary", StudentStatus.ACTIVE, null);
        TrainingSessionResponse gi = action(token, createManual(token, directClass(fixture, "Gi", ClassModality.GI).id(), fixture.unit().getId(), "08:00", "09:00").id(), "open");
        TrainingSessionResponse noGi = action(token, createManual(token, directClass(fixture, "NoGi", ClassModality.NO_GI).id(), fixture.unit().getId(), "10:00", "11:00").id(), "open");
        AttendanceResponse giAttendance = postAttendance(token, gi.id(), student.getId(), AttendanceResponse.class).getBody();
        postAttendance(token, noGi.id(), student.getId(), AttendanceResponse.class);
        restTemplate.exchange(url("/api/v1/attendance/" + giAttendance.id() + "/void"), HttpMethod.POST, bearer(token, Map.of("reason", "corrigir")), AttendanceResponse.class);

        StudentAttendanceSummary summary = restTemplate.exchange(url("/api/v1/students/" + student.getId() + "/attendance/summary"), HttpMethod.GET, bearer(token), StudentAttendanceSummary.class).getBody();
        assertThat(summary.totalTrainings()).isEqualTo(1);
        assertThat(summary.last30Days()).isEqualTo(1);
        assertThat(summary.last90Days()).isEqualTo(1);
        assertThat(summary.giCount()).isZero();
        assertThat(summary.noGiCount()).isEqualTo(1);
        assertThat(summary.currentMonth()).isEqualTo(1);
    }

    private <T> ResponseEntity<T> postAttendance(String token, UUID sessionId, UUID studentId, Class<T> type) {
        return restTemplate.exchange(url("/api/v1/training-sessions/" + sessionId + "/attendance"), HttpMethod.POST, bearer(token, Map.of("studentId", studentId)), type);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> listAttendance(String token, UUID sessionId, boolean includeVoided) {
        return restTemplate.exchange(url("/api/v1/training-sessions/" + sessionId + "/attendance?includeVoided=" + includeVoided), HttpMethod.GET, bearer(token), List.class).getBody();
    }

    private long countActive(UUID sessionId, UUID studentId) {
        return jdbcTemplate.queryForObject("select count(*) from attendances where training_session_id = ? and student_id = ? and status = 'ACTIVE'", Long.class, sessionId, studentId);
    }

    private TrainingSessionResponse directOwnerSession(Fixture fixture, UUID classId) { return createManual(loginAsOwner(fixture), classId, fixture.unit().getId(), "12:00", "13:00"); }
    private String loginAsOwner(Fixture fixture) { User owner = user(fixture, "Owner " + UUID.randomUUID(), "owner-" + UUID.randomUUID() + "@" + fixture.tenant().getSlug() + ".test", RoleName.OWNER); return login(fixture.tenant().getSlug(), owner.getEmail()); }
    private ClassResponse directClass(Fixture fixture, String name, ClassModality modality) { DojoClass dojoClass = dojoClassRepository.save(new DojoClass(fixture.tenant(), name, modality, ClassAudience.ADULT, ClassType.REGULAR, ClassLevel.ALL_LEVELS)); return new ClassResponse(dojoClass.getId(), dojoClass.getName(), dojoClass.getDescription(), dojoClass.getModality(), dojoClass.getAudience(), dojoClass.getClassType(), dojoClass.getLevel(), dojoClass.getStatus(), dojoClass.getDefaultCapacity()); }
    private TrainingSessionResponse createManual(String token, UUID classId, UUID unitId, String start, String end) { return restTemplate.exchange(url("/api/v1/training-sessions"), HttpMethod.POST, bearer(token, Map.of("dojoClassId", classId, "unitId", unitId, "sessionDate", LocalDate.now().toString(), "startTime", start, "endTime", end, "capacity", 20, "staff", List.of())), TrainingSessionResponse.class).getBody(); }
    private TrainingSessionResponse action(String token, UUID id, String action) { return restTemplate.exchange(url("/api/v1/training-sessions/" + id + "/" + action), HttpMethod.POST, bearer(token, Map.of()), TrainingSessionResponse.class).getBody(); }
    private Student student(Fixture fixture, String fullName, StudentStatus status, User user) { Student student = new Student(fixture.tenant(), fixture.unit(), fullName, LocalDate.now().minusMonths(1), status); student.setUser(user); return studentRepository.save(student); }
    private User user(Fixture fixture, String name, String email, RoleName roleName) { Role role = roleRepository.findByName(roleName).orElseThrow(); return userRepository.save(new User(fixture.tenant(), name, email, passwordEncoder.encode("secret"), Set.of(role))); }
    private String login(String tenant, String email) { TokenResponse response = restTemplate.postForEntity(url("/api/v1/auth/login"), Map.of("tenant", tenant, "email", email, "password", "secret"), TokenResponse.class).getBody(); return response.accessToken(); }
    private Fixture fixture(String slug, RoleName roleName) { Tenant tenant = tenantRepository.save(new Tenant("Tenant " + slug, slug)); Unit unit = unitRepository.save(new Unit(tenant, "Unidade " + slug, "unit-" + slug)); User user = userRepository.save(new User(tenant, roleName.name() + " " + slug, roleName.name().toLowerCase() + "@" + slug + ".test", passwordEncoder.encode("secret"), Set.of(roleRepository.findByName(roleName).orElseThrow()))); return new Fixture(tenant, unit, user); }
    private HttpEntity<Void> bearer(String accessToken) { HttpHeaders headers = new HttpHeaders(); headers.setBearerAuth(accessToken); return new HttpEntity<>(headers); }
    private HttpEntity<Object> bearer(String accessToken, Object body) { HttpHeaders headers = new HttpHeaders(); headers.setBearerAuth(accessToken); return new HttpEntity<>(body, headers); }
    private String url(String path) { return "http://localhost:" + port + path; }
    private record Fixture(Tenant tenant, Unit unit, User user) {}
}

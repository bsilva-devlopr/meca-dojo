package br.com.mecadojo.api.attendance.service;

import br.com.mecadojo.api.attendance.api.dto.AttendanceDtos.*;
import br.com.mecadojo.api.attendance.domain.*;
import br.com.mecadojo.api.attendance.repository.CheckInRepository;
import br.com.mecadojo.api.attendance.repository.CheckInTokenRepository;
import br.com.mecadojo.api.identity.domain.Tenant;
import br.com.mecadojo.api.identity.domain.User;
import br.com.mecadojo.api.identity.security.CurrentUser;
import br.com.mecadojo.api.people.domain.Student;
import br.com.mecadojo.api.people.repository.StudentRepository;
import br.com.mecadojo.api.people.service.PeopleReferenceService;
import br.com.mecadojo.api.shared.exception.BadRequestException;
import br.com.mecadojo.api.training.domain.TrainingSession;
import br.com.mecadojo.api.training.domain.TrainingSessionStatus;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CheckInService {
    private final CheckInTokenRepository tokenRepository;
    private final CheckInRepository checkInRepository;
    private final StudentRepository studentRepository;
    private final AttendanceService attendanceService;
    private final PeopleReferenceService references;
    private final SecureRandom secureRandom = new SecureRandom();
    private final long qrTtlSeconds;
    private final String webBaseUrl;

    public CheckInService(
        CheckInTokenRepository tokenRepository,
        CheckInRepository checkInRepository,
        StudentRepository studentRepository,
        AttendanceService attendanceService,
        PeopleReferenceService references,
        @Value("${app.check-in.qr-ttl-seconds:60}") long qrTtlSeconds,
        @Value("${app.check-in.web-base-url:http://localhost:3000}") String webBaseUrl
    ) {
        this.tokenRepository = tokenRepository;
        this.checkInRepository = checkInRepository;
        this.studentRepository = studentRepository;
        this.attendanceService = attendanceService;
        this.references = references;
        this.qrTtlSeconds = qrTtlSeconds;
        this.webBaseUrl = webBaseUrl;
    }

    @Transactional
    public CreateCheckInTokenResponse createToken(UUID sessionId) {
        TrainingSession session = attendanceService.loadSession(sessionId);
        if (session.getStatus() != TrainingSessionStatus.OPEN) {
            throw new BadRequestException("Training session must be open to generate check-in token");
        }
        String token = randomToken();
        Instant expiresAt = Instant.now().plusSeconds(qrTtlSeconds);
        User createdBy = references.user(CurrentUser.get().userId());
        tokenRepository.save(new CheckInToken(references.currentTenant(), session, sha256(token), expiresAt, createdBy));
        return new CreateCheckInTokenResponse(token, expiresAt, normalizedBaseUrl() + "/check-in?token=" + token);
    }

    @Transactional
    public CheckInResponse selfCheckIn(SelfCheckInRequest request) {
        if (request.token() == null || request.token().isBlank()) {
            return rejected(CheckInStatus.INVALID, "QR invalido.", null, null, null);
        }
        Instant now = Instant.now();
        Tenant currentTenant = references.currentTenant();
        var token = tokenRepository.findByTokenHash(sha256(request.token().trim())).orElse(null);
        if (token == null || !token.getTenant().getId().equals(CurrentUser.get().tenantId())) {
            return rejected(CheckInStatus.INVALID, "QR invalido.", currentTenant, null, null);
        }
        TrainingSession session = token.getTrainingSession();
        String tokenIdentifier = token.getId().toString();
        Student student = studentRepository.findByTenantIdAndUserId(CurrentUser.get().tenantId(), CurrentUser.get().userId()).orElse(null);
        if (token.isExpired(now) || token.isRevoked()) {
            return rejected(CheckInStatus.EXPIRED, "Este QR expirou.", token.getTenant(), session, student, tokenIdentifier);
        }
        if (session.getStatus() != TrainingSessionStatus.OPEN) {
            return rejected(CheckInStatus.SESSION_CLOSED, "Este treino nao esta aceitando check-ins.", token.getTenant(), session, student, tokenIdentifier);
        }
        if (student == null) {
            return rejected(CheckInStatus.UNLINKED_USER, "Seu acesso ainda nao esta vinculado a um aluno.", token.getTenant(), session, null, tokenIdentifier);
        }
        try {
            attendanceService.loadValidStudent(student.getId());
        } catch (RuntimeException exception) {
            return rejected(CheckInStatus.STUDENT_INVALID, "Aluno nao habilitado para check-in.", token.getTenant(), session, student, tokenIdentifier);
        }
        if (attendanceService.hasActiveAttendance(session.getId(), student.getId())) {
            CheckIn duplicate = checkInRepository.save(new CheckIn(token.getTenant(), session, student, CheckInStatus.DUPLICATE, now, tokenIdentifier, "Attendance already registered"));
            return new CheckInResponse(CheckInStatus.DUPLICATE, "Sua presenca ja foi registrada neste treino.", null, attendanceService.sessionSummary(session), duplicate.getOccurredAt());
        }
        CheckIn checkIn = checkInRepository.save(new CheckIn(token.getTenant(), session, student, CheckInStatus.ACCEPTED, now, tokenIdentifier, null));
        var attendance = attendanceService.createAttendance(session, student, AttendanceSource.QR, references.user(CurrentUser.get().userId()), checkIn);
        checkIn.setAttendance(attendance);
        checkInRepository.save(checkIn);
        return new CheckInResponse(CheckInStatus.ACCEPTED, "Presenca registrada!", attendanceService.toResponse(attendance), attendanceService.sessionSummary(session), checkIn.getOccurredAt());
    }

    private CheckInResponse rejected(CheckInStatus status, String message, Tenant tenant, TrainingSession session, Student student) {
        return rejected(status, message, tenant, session, student, null);
    }

    private CheckInResponse rejected(CheckInStatus status, String message, Tenant tenant, TrainingSession session, Student student, String tokenIdentifier) {
        CheckIn checkIn = null;
        if (tenant != null) {
            checkIn = checkInRepository.save(new CheckIn(tenant, session, student, status, Instant.now(), tokenIdentifier, message));
        }
        return new CheckInResponse(status, message, null, session == null ? null : attendanceService.sessionSummary(session), checkIn == null ? Instant.now() : checkIn.getOccurredAt());
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sha256(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }

    private String normalizedBaseUrl() {
        return webBaseUrl.endsWith("/") ? webBaseUrl.substring(0, webBaseUrl.length() - 1) : webBaseUrl;
    }
}

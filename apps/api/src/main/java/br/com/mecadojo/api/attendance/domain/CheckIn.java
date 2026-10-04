package br.com.mecadojo.api.attendance.domain;

import br.com.mecadojo.api.identity.domain.Tenant;
import br.com.mecadojo.api.people.domain.Student;
import br.com.mecadojo.api.training.domain.TrainingSession;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "check_ins")
public class CheckIn {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_session_id")
    private TrainingSession trainingSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CheckInMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CheckInStatus status;

    @Column(nullable = false)
    private Instant occurredAt;

    private String tokenIdentifier;
    private String failureReason;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_id")
    private Attendance attendance;

    @Column(nullable = false)
    private Instant createdAt;

    protected CheckIn() {}

    public CheckIn(Tenant tenant, TrainingSession trainingSession, Student student, CheckInStatus status, Instant occurredAt, String tokenIdentifier, String failureReason) {
        this.id = UUID.randomUUID();
        this.tenant = tenant;
        this.trainingSession = trainingSession;
        this.student = student;
        this.method = CheckInMethod.QR;
        this.status = status;
        this.occurredAt = occurredAt;
        this.tokenIdentifier = tokenIdentifier;
        this.failureReason = failureReason;
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (id == null) id = UUID.randomUUID();
        if (method == null) method = CheckInMethod.QR;
        if (occurredAt == null) occurredAt = now;
        createdAt = now;
    }

    public void setAttendance(Attendance attendance) { this.attendance = attendance; }

    public UUID getId() { return id; }
    public Tenant getTenant() { return tenant; }
    public TrainingSession getTrainingSession() { return trainingSession; }
    public Student getStudent() { return student; }
    public CheckInMethod getMethod() { return method; }
    public CheckInStatus getStatus() { return status; }
    public Instant getOccurredAt() { return occurredAt; }
    public String getTokenIdentifier() { return tokenIdentifier; }
    public String getFailureReason() { return failureReason; }
    public Attendance getAttendance() { return attendance; }
    public Instant getCreatedAt() { return createdAt; }
}

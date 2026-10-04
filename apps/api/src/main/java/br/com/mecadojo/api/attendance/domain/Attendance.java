package br.com.mecadojo.api.attendance.domain;

import br.com.mecadojo.api.identity.domain.Tenant;
import br.com.mecadojo.api.identity.domain.User;
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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "attendances")
public class Attendance {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_session_id", nullable = false)
    private TrainingSession trainingSession;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceSource source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;

    @Column(nullable = false)
    private Instant checkedInAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by_user_id")
    private User recordedByUser;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "check_in_id")
    private CheckIn checkIn;

    private Instant voidedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voided_by_user_id")
    private User voidedByUser;

    private String voidReason;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Attendance() {}

    public Attendance(Tenant tenant, TrainingSession trainingSession, Student student, AttendanceSource source, Instant checkedInAt, User recordedByUser, CheckIn checkIn) {
        this.id = UUID.randomUUID();
        this.tenant = tenant;
        this.trainingSession = trainingSession;
        this.student = student;
        this.source = source;
        this.status = AttendanceStatus.ACTIVE;
        this.checkedInAt = checkedInAt;
        this.recordedByUser = recordedByUser;
        this.checkIn = checkIn;
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (id == null) id = UUID.randomUUID();
        if (status == null) status = AttendanceStatus.ACTIVE;
        if (checkedInAt == null) checkedInAt = now;
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() { updatedAt = Instant.now(); }

    public void voidAttendance(User voidedByUser, String reason) {
        this.status = AttendanceStatus.VOIDED;
        this.voidedAt = Instant.now();
        this.voidedByUser = voidedByUser;
        this.voidReason = reason;
    }

    public UUID getId() { return id; }
    public Tenant getTenant() { return tenant; }
    public TrainingSession getTrainingSession() { return trainingSession; }
    public Student getStudent() { return student; }
    public AttendanceSource getSource() { return source; }
    public AttendanceStatus getStatus() { return status; }
    public Instant getCheckedInAt() { return checkedInAt; }
    public User getRecordedByUser() { return recordedByUser; }
    public CheckIn getCheckIn() { return checkIn; }
    public void setCheckIn(CheckIn checkIn) { this.checkIn = checkIn; }
    public Instant getVoidedAt() { return voidedAt; }
    public User getVoidedByUser() { return voidedByUser; }
    public String getVoidReason() { return voidReason; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}

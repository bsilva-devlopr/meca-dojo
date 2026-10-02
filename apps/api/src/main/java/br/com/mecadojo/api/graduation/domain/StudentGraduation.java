package br.com.mecadojo.api.graduation.domain;

import br.com.mecadojo.api.identity.domain.Tenant;
import br.com.mecadojo.api.identity.domain.User;
import br.com.mecadojo.api.people.domain.StaffProfile;
import br.com.mecadojo.api.people.domain.Student;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "student_graduations")
public class StudentGraduation {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "belt_id", nullable = false)
    private Belt belt;

    @Column(nullable = false)
    private int degree;

    private LocalDate graduatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "awarded_by_staff_id")
    private StaffProfile awardedByStaff;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GraduationType type;

    private String notes;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corrects_graduation_id")
    private StudentGraduation correctsGraduation;

    private String correctionReason;

    @Column(nullable = false)
    private Instant createdAt;

    protected StudentGraduation() {
    }

    public StudentGraduation(Tenant tenant, Student student, Belt belt, int degree, LocalDate graduatedAt, StaffProfile awardedByStaff, GraduationType type, String notes, User createdByUser) {
        this.id = UUID.randomUUID();
        this.tenant = tenant;
        this.student = student;
        this.belt = belt;
        this.degree = degree;
        this.graduatedAt = graduatedAt;
        this.awardedByStaff = awardedByStaff;
        this.type = type;
        this.notes = notes;
        this.createdByUser = createdByUser;
    }

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public Tenant getTenant() { return tenant; }
    public Student getStudent() { return student; }
    public Belt getBelt() { return belt; }
    public int getDegree() { return degree; }
    public LocalDate getGraduatedAt() { return graduatedAt; }
    public StaffProfile getAwardedByStaff() { return awardedByStaff; }
    public GraduationType getType() { return type; }
    public String getNotes() { return notes; }
    public User getCreatedByUser() { return createdByUser; }
    public StudentGraduation getCorrectsGraduation() { return correctsGraduation; }
    public String getCorrectionReason() { return correctionReason; }
    public Instant getCreatedAt() { return createdAt; }

    public void markCorrection(StudentGraduation correctsGraduation, String correctionReason) {
        this.correctsGraduation = correctsGraduation;
        this.correctionReason = correctionReason;
    }
}

package br.com.mecadojo.api.people.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "guardian_students")
public class GuardianStudent {
    @EmbeddedId
    private GuardianStudentId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("guardianId")
    @JoinColumn(name = "guardian_id", nullable = false)
    private Guardian guardian;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("studentId")
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GuardianRelationship relationship;

    @Column(name = "is_primary", nullable = false)
    private boolean primary;

    @Column(nullable = false)
    private Instant createdAt;

    protected GuardianStudent() {
    }

    public GuardianStudent(Guardian guardian, Student student, GuardianRelationship relationship, boolean primary) {
        this.id = new GuardianStudentId(guardian.getId(), student.getId());
        this.guardian = guardian;
        this.student = student;
        this.relationship = relationship;
        this.primary = primary;
    }

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
    }

    public GuardianStudentId getId() { return id; }
    public Guardian getGuardian() { return guardian; }
    public Student getStudent() { return student; }
    public GuardianRelationship getRelationship() { return relationship; }
    public void setRelationship(GuardianRelationship relationship) { this.relationship = relationship; }
    public boolean isPrimary() { return primary; }
    public void setPrimary(boolean primary) { this.primary = primary; }
}

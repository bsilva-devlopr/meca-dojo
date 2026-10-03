package br.com.mecadojo.api.training.domain;

import br.com.mecadojo.api.people.domain.StaffProfile;
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
@Table(name = "training_session_staff")
public class TrainingSessionStaff {
    @EmbeddedId
    private TrainingSessionStaffId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("trainingSessionId")
    @JoinColumn(name = "training_session_id", nullable = false)
    private TrainingSession trainingSession;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("staffProfileId")
    @JoinColumn(name = "staff_profile_id", nullable = false)
    private StaffProfile staffProfile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrainingSessionStaffRole role;

    @Column(nullable = false)
    private Instant createdAt;

    protected TrainingSessionStaff() {}

    public TrainingSessionStaff(TrainingSession trainingSession, StaffProfile staffProfile, TrainingSessionStaffRole role) {
        this.trainingSession = trainingSession;
        this.staffProfile = staffProfile;
        this.role = role;
        this.id = new TrainingSessionStaffId(trainingSession.getId(), staffProfile.getId());
    }

    @PrePersist
    void prePersist() { if (createdAt == null) createdAt = Instant.now(); }

    public TrainingSessionStaffId getId() { return id; }
    public TrainingSession getTrainingSession() { return trainingSession; }
    public StaffProfile getStaffProfile() { return staffProfile; }
    public TrainingSessionStaffRole getRole() { return role; }
    public Instant getCreatedAt() { return createdAt; }
}

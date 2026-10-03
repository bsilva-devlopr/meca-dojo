package br.com.mecadojo.api.training.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class TrainingSessionStaffId implements Serializable {
    @Column(name = "training_session_id")
    private UUID trainingSessionId;

    @Column(name = "staff_profile_id")
    private UUID staffProfileId;

    protected TrainingSessionStaffId() {}

    public TrainingSessionStaffId(UUID trainingSessionId, UUID staffProfileId) {
        this.trainingSessionId = trainingSessionId;
        this.staffProfileId = staffProfileId;
    }

    public UUID getTrainingSessionId() { return trainingSessionId; }
    public UUID getStaffProfileId() { return staffProfileId; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TrainingSessionStaffId that)) return false;
        return Objects.equals(trainingSessionId, that.trainingSessionId) && Objects.equals(staffProfileId, that.staffProfileId);
    }

    @Override public int hashCode() { return Objects.hash(trainingSessionId, staffProfileId); }
}

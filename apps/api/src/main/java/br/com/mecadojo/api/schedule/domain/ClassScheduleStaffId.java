package br.com.mecadojo.api.schedule.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class ClassScheduleStaffId implements Serializable {
    @Column(name = "schedule_id")
    private UUID scheduleId;

    @Column(name = "staff_profile_id")
    private UUID staffProfileId;

    protected ClassScheduleStaffId() {}

    public ClassScheduleStaffId(UUID scheduleId, UUID staffProfileId) {
        this.scheduleId = scheduleId;
        this.staffProfileId = staffProfileId;
    }

    public UUID getScheduleId() { return scheduleId; }
    public UUID getStaffProfileId() { return staffProfileId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClassScheduleStaffId that)) return false;
        return Objects.equals(scheduleId, that.scheduleId) && Objects.equals(staffProfileId, that.staffProfileId);
    }

    @Override
    public int hashCode() { return Objects.hash(scheduleId, staffProfileId); }
}

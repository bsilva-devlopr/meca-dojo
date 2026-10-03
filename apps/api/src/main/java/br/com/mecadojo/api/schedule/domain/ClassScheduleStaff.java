package br.com.mecadojo.api.schedule.domain;

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
@Table(name = "class_schedule_staff")
public class ClassScheduleStaff {
    @EmbeddedId
    private ClassScheduleStaffId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("scheduleId")
    @JoinColumn(name = "schedule_id", nullable = false)
    private ClassSchedule schedule;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("staffProfileId")
    @JoinColumn(name = "staff_profile_id", nullable = false)
    private StaffProfile staffProfile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScheduleStaffRole role;

    @Column(nullable = false)
    private Instant createdAt;

    protected ClassScheduleStaff() {}

    public ClassScheduleStaff(ClassSchedule schedule, StaffProfile staffProfile, ScheduleStaffRole role) {
        this.schedule = schedule;
        this.staffProfile = staffProfile;
        this.role = role;
        this.id = new ClassScheduleStaffId(schedule.getId(), staffProfile.getId());
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public ClassScheduleStaffId getId() { return id; }
    public ClassSchedule getSchedule() { return schedule; }
    public StaffProfile getStaffProfile() { return staffProfile; }
    public ScheduleStaffRole getRole() { return role; }
    public Instant getCreatedAt() { return createdAt; }
}

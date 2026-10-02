package br.com.mecadojo.api.people.domain;

import br.com.mecadojo.api.identity.domain.Tenant;
import br.com.mecadojo.api.identity.domain.Unit;
import br.com.mecadojo.api.identity.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "staff_profiles")
public class StaffProfile {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StaffType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StaffStatus status;

    @Column(nullable = false)
    private String displayName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_unit_id")
    private Unit primaryUnit;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected StaffProfile() {
    }

    public StaffProfile(Tenant tenant, User user, StaffType type, String displayName) {
        this.id = UUID.randomUUID();
        this.tenant = tenant;
        this.user = user;
        this.type = type;
        this.displayName = displayName;
        this.status = StaffStatus.ACTIVE;
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (id == null) id = UUID.randomUUID();
        if (status == null) status = StaffStatus.ACTIVE;
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public Tenant getTenant() { return tenant; }
    public User getUser() { return user; }
    public StaffType getType() { return type; }
    public void setType(StaffType type) { this.type = type; }
    public StaffStatus getStatus() { return status; }
    public void setStatus(StaffStatus status) { this.status = status; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public Unit getPrimaryUnit() { return primaryUnit; }
    public void setPrimaryUnit(Unit primaryUnit) { this.primaryUnit = primaryUnit; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}

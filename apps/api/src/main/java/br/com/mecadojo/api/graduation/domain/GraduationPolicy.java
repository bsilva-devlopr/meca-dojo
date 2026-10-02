package br.com.mecadojo.api.graduation.domain;

import br.com.mecadojo.api.identity.domain.Tenant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "graduation_policies")
public class GraduationPolicy {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "belt_id", nullable = false)
    private Belt belt;

    private Integer minimumTrainingCount;
    private Integer minimumDaysInGrade;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected GraduationPolicy() {
    }

    public GraduationPolicy(Tenant tenant, Belt belt) {
        this.id = UUID.randomUUID();
        this.tenant = tenant;
        this.belt = belt;
        this.active = true;
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (id == null) id = UUID.randomUUID();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public Tenant getTenant() { return tenant; }
    public Belt getBelt() { return belt; }
    public Integer getMinimumTrainingCount() { return minimumTrainingCount; }
    public void setMinimumTrainingCount(Integer minimumTrainingCount) { this.minimumTrainingCount = minimumTrainingCount; }
    public Integer getMinimumDaysInGrade() { return minimumDaysInGrade; }
    public void setMinimumDaysInGrade(Integer minimumDaysInGrade) { this.minimumDaysInGrade = minimumDaysInGrade; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}

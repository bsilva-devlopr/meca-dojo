package br.com.mecadojo.api.schedule.domain;

import br.com.mecadojo.api.identity.domain.Tenant;
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
@Table(name = "dojo_classes")
public class DojoClass {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClassModality modality;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClassAudience audience;

    @Enumerated(EnumType.STRING)
    @Column(name = "class_type", nullable = false)
    private ClassType classType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClassLevel level;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClassStatus status;

    private Integer defaultCapacity;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected DojoClass() {}

    public DojoClass(Tenant tenant, String name, ClassModality modality, ClassAudience audience, ClassType classType, ClassLevel level) {
        this.id = UUID.randomUUID();
        this.tenant = tenant;
        this.name = name;
        this.modality = modality;
        this.audience = audience;
        this.classType = classType;
        this.level = level;
        this.status = ClassStatus.ACTIVE;
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (id == null) id = UUID.randomUUID();
        if (status == null) status = ClassStatus.ACTIVE;
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() { updatedAt = Instant.now(); }

    public UUID getId() { return id; }
    public Tenant getTenant() { return tenant; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public ClassModality getModality() { return modality; }
    public void setModality(ClassModality modality) { this.modality = modality; }
    public ClassAudience getAudience() { return audience; }
    public void setAudience(ClassAudience audience) { this.audience = audience; }
    public ClassType getClassType() { return classType; }
    public void setClassType(ClassType classType) { this.classType = classType; }
    public ClassLevel getLevel() { return level; }
    public void setLevel(ClassLevel level) { this.level = level; }
    public ClassStatus getStatus() { return status; }
    public void setStatus(ClassStatus status) { this.status = status; }
    public Integer getDefaultCapacity() { return defaultCapacity; }
    public void setDefaultCapacity(Integer defaultCapacity) { this.defaultCapacity = defaultCapacity; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}

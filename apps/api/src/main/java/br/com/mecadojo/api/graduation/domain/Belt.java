package br.com.mecadojo.api.graduation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "belts")
public class Belt {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BeltSystem system;

    @Column(nullable = false)
    private int sequence;

    @Column(nullable = false)
    private int maxDegree;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private Instant createdAt;

    protected Belt() {
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public String getDisplayName() { return displayName; }
    public BeltSystem getSystem() { return system; }
    public int getSequence() { return sequence; }
    public int getMaxDegree() { return maxDegree; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
}

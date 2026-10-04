package br.com.mecadojo.api.attendance.domain;

import br.com.mecadojo.api.identity.domain.Tenant;
import br.com.mecadojo.api.identity.domain.User;
import br.com.mecadojo.api.training.domain.TrainingSession;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "check_in_tokens")
public class CheckInToken {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_session_id", nullable = false)
    private TrainingSession trainingSession;

    @Column(nullable = false)
    private String tokenHash;

    @Column(nullable = false)
    private Instant expiresAt;

    private Instant revokedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdByUser;

    @Column(nullable = false)
    private Instant createdAt;

    protected CheckInToken() {}

    public CheckInToken(Tenant tenant, TrainingSession trainingSession, String tokenHash, Instant expiresAt, User createdByUser) {
        this.id = UUID.randomUUID();
        this.tenant = tenant;
        this.trainingSession = trainingSession;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.createdByUser = createdByUser;
    }

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        createdAt = Instant.now();
    }

    public boolean isExpired(Instant now) { return !expiresAt.isAfter(now); }
    public boolean isRevoked() { return revokedAt != null; }

    public UUID getId() { return id; }
    public Tenant getTenant() { return tenant; }
    public TrainingSession getTrainingSession() { return trainingSession; }
    public String getTokenHash() { return tokenHash; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getRevokedAt() { return revokedAt; }
    public User getCreatedByUser() { return createdByUser; }
    public Instant getCreatedAt() { return createdAt; }
}

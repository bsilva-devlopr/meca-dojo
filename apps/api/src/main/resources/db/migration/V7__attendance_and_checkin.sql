CREATE TABLE attendances (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    training_session_id UUID NOT NULL REFERENCES training_sessions(id),
    student_id UUID NOT NULL REFERENCES students(id),
    source VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    checked_in_at TIMESTAMPTZ NOT NULL,
    recorded_by_user_id UUID REFERENCES users(id),
    check_in_id UUID,
    voided_at TIMESTAMPTZ,
    voided_by_user_id UUID REFERENCES users(id),
    void_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT chk_attendances_source CHECK (source IN ('QR', 'MANUAL', 'RECEPTION')),
    CONSTRAINT chk_attendances_status CHECK (status IN ('ACTIVE', 'VOIDED')),
    CONSTRAINT chk_attendances_void_metadata CHECK (
        (status = 'ACTIVE' AND voided_at IS NULL AND voided_by_user_id IS NULL AND void_reason IS NULL)
        OR
        (status = 'VOIDED' AND voided_at IS NOT NULL AND voided_by_user_id IS NOT NULL AND void_reason IS NOT NULL AND length(trim(void_reason)) > 0)
    )
);

CREATE UNIQUE INDEX uk_attendances_active_session_student
    ON attendances(training_session_id, student_id)
    WHERE status = 'ACTIVE';
CREATE INDEX idx_attendances_tenant_session_status ON attendances(tenant_id, training_session_id, status);
CREATE INDEX idx_attendances_tenant_student_checked_at ON attendances(tenant_id, student_id, checked_in_at DESC);
CREATE INDEX idx_attendances_tenant_status ON attendances(tenant_id, status);

CREATE TABLE check_in_tokens (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    training_session_id UUID NOT NULL REFERENCES training_sessions(id),
    token_hash VARCHAR(128) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    created_by_user_id UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_check_in_tokens_tenant_session ON check_in_tokens(tenant_id, training_session_id);
CREATE INDEX idx_check_in_tokens_expires_at ON check_in_tokens(expires_at);

CREATE TABLE check_ins (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    training_session_id UUID REFERENCES training_sessions(id),
    student_id UUID REFERENCES students(id),
    method VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    token_identifier VARCHAR(128),
    failure_reason TEXT,
    attendance_id UUID REFERENCES attendances(id),
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT chk_check_ins_method CHECK (method IN ('QR')),
    CONSTRAINT chk_check_ins_status CHECK (status IN ('ACCEPTED', 'DUPLICATE', 'EXPIRED', 'INVALID', 'SESSION_CLOSED', 'STUDENT_INVALID', 'UNLINKED_USER'))
);

ALTER TABLE attendances
    ADD CONSTRAINT fk_attendances_check_in FOREIGN KEY (check_in_id) REFERENCES check_ins(id);

CREATE INDEX idx_check_ins_tenant_session ON check_ins(tenant_id, training_session_id);
CREATE INDEX idx_check_ins_tenant_student ON check_ins(tenant_id, student_id);
CREATE INDEX idx_check_ins_tenant_status ON check_ins(tenant_id, status);
CREATE INDEX idx_check_ins_occurred_at ON check_ins(occurred_at DESC);

CREATE TABLE training_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    dojo_class_id UUID NOT NULL REFERENCES dojo_classes(id),
    class_schedule_id UUID REFERENCES class_schedules(id),
    unit_id UUID NOT NULL REFERENCES units(id),
    session_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status VARCHAR(20) NOT NULL,
    capacity INTEGER,
    notes TEXT,
    created_by_user_id UUID NOT NULL REFERENCES users(id),
    cancelled_at TIMESTAMPTZ,
    cancelled_by_user_id UUID REFERENCES users(id),
    cancellation_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_training_sessions_status CHECK (status IN ('SCHEDULED', 'OPEN', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT ck_training_sessions_time CHECK (end_time > start_time),
    CONSTRAINT ck_training_sessions_capacity CHECK (capacity IS NULL OR capacity > 0),
    CONSTRAINT ck_training_sessions_cancellation CHECK (
        (status = 'CANCELLED' AND cancelled_at IS NOT NULL AND cancelled_by_user_id IS NOT NULL AND cancellation_reason IS NOT NULL AND length(trim(cancellation_reason)) > 0)
        OR (status <> 'CANCELLED' AND cancelled_at IS NULL AND cancelled_by_user_id IS NULL AND cancellation_reason IS NULL)
    )
);

CREATE TABLE training_session_staff (
    training_session_id UUID NOT NULL REFERENCES training_sessions(id) ON DELETE CASCADE,
    staff_profile_id UUID NOT NULL REFERENCES staff_profiles(id),
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (training_session_id, staff_profile_id),
    CONSTRAINT ck_training_session_staff_role CHECK (role IN ('LEAD', 'ASSISTANT'))
);

CREATE UNIQUE INDEX uk_training_sessions_schedule_date ON training_sessions(class_schedule_id, session_date) WHERE class_schedule_id IS NOT NULL;
CREATE UNIQUE INDEX uk_training_session_staff_one_lead ON training_session_staff(training_session_id) WHERE role = 'LEAD';
CREATE INDEX ix_training_sessions_tenant_date_time ON training_sessions(tenant_id, session_date, start_time);
CREATE INDEX ix_training_sessions_tenant_status ON training_sessions(tenant_id, status);
CREATE INDEX ix_training_sessions_tenant_unit_date ON training_sessions(tenant_id, unit_id, session_date);
CREATE INDEX ix_training_sessions_tenant_class_date ON training_sessions(tenant_id, dojo_class_id, session_date);
CREATE INDEX ix_training_sessions_tenant_schedule ON training_sessions(tenant_id, class_schedule_id);
CREATE INDEX ix_training_session_staff_staff ON training_session_staff(staff_profile_id);

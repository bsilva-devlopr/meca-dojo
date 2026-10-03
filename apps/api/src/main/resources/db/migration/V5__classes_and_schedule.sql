CREATE TABLE dojo_classes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    name VARCHAR(160) NOT NULL,
    description TEXT,
    modality VARCHAR(20) NOT NULL,
    audience VARCHAR(20) NOT NULL,
    class_type VARCHAR(30) NOT NULL,
    level VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,
    default_capacity INTEGER,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_dojo_classes_modality CHECK (modality IN ('GI', 'NO_GI', 'MIXED', 'OTHER')),
    CONSTRAINT ck_dojo_classes_audience CHECK (audience IN ('ADULT', 'KIDS', 'TEENS', 'ALL')),
    CONSTRAINT ck_dojo_classes_type CHECK (class_type IN ('REGULAR', 'FUNDAMENTALS', 'COMPETITION', 'OPEN_MAT', 'PRIVATE', 'OTHER')),
    CONSTRAINT ck_dojo_classes_level CHECK (level IN ('ALL_LEVELS', 'BEGINNER', 'INTERMEDIATE', 'ADVANCED')),
    CONSTRAINT ck_dojo_classes_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT ck_dojo_classes_name_not_blank CHECK (length(trim(name)) > 0),
    CONSTRAINT ck_dojo_classes_default_capacity CHECK (default_capacity IS NULL OR default_capacity > 0)
);

CREATE TABLE class_schedules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    dojo_class_id UUID NOT NULL REFERENCES dojo_classes(id),
    unit_id UUID NOT NULL REFERENCES units(id),
    day_of_week VARCHAR(20) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    capacity_override INTEGER,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_class_schedules_day CHECK (day_of_week IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY')),
    CONSTRAINT ck_class_schedules_time CHECK (end_time > start_time),
    CONSTRAINT ck_class_schedules_capacity_override CHECK (capacity_override IS NULL OR capacity_override > 0),
    CONSTRAINT ck_class_schedules_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE TABLE class_schedule_staff (
    schedule_id UUID NOT NULL REFERENCES class_schedules(id) ON DELETE CASCADE,
    staff_profile_id UUID NOT NULL REFERENCES staff_profiles(id),
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (schedule_id, staff_profile_id),
    CONSTRAINT ck_class_schedule_staff_role CHECK (role IN ('LEAD', 'ASSISTANT'))
);

CREATE UNIQUE INDEX uk_class_schedule_staff_one_lead ON class_schedule_staff(schedule_id) WHERE role = 'LEAD';
CREATE INDEX ix_dojo_classes_tenant_name ON dojo_classes(tenant_id, name);
CREATE INDEX ix_dojo_classes_tenant_status ON dojo_classes(tenant_id, status);
CREATE INDEX ix_dojo_classes_tenant_modality ON dojo_classes(tenant_id, modality);
CREATE INDEX ix_class_schedules_tenant_day_time ON class_schedules(tenant_id, day_of_week, start_time);
CREATE INDEX ix_class_schedules_tenant_unit ON class_schedules(tenant_id, unit_id);
CREATE INDEX ix_class_schedules_tenant_class ON class_schedules(tenant_id, dojo_class_id);
CREATE INDEX ix_class_schedules_tenant_status ON class_schedules(tenant_id, status);
CREATE INDEX ix_class_schedule_staff_staff ON class_schedule_staff(staff_profile_id);

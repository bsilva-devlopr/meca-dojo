CREATE TABLE students (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    user_id UUID REFERENCES users(id),
    primary_unit_id UUID NOT NULL REFERENCES units(id),
    full_name VARCHAR(180) NOT NULL,
    preferred_name VARCHAR(120),
    birth_date DATE,
    email VARCHAR(254),
    phone VARCHAR(40),
    status VARCHAR(20) NOT NULL,
    join_date DATE NOT NULL,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_students_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'TRIAL')),
    CONSTRAINT ck_students_full_name_not_blank CHECK (length(trim(full_name)) > 0)
);

CREATE TABLE student_emergency_contacts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    name VARCHAR(160) NOT NULL,
    relationship VARCHAR(80) NOT NULL,
    phone VARCHAR(40) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_student_emergency_contact_student UNIQUE (student_id),
    CONSTRAINT ck_student_emergency_contact_name_not_blank CHECK (length(trim(name)) > 0),
    CONSTRAINT ck_student_emergency_contact_relationship_not_blank CHECK (length(trim(relationship)) > 0),
    CONSTRAINT ck_student_emergency_contact_phone_not_blank CHECK (length(trim(phone)) > 0)
);

CREATE TABLE guardians (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    user_id UUID REFERENCES users(id),
    full_name VARCHAR(180) NOT NULL,
    email VARCHAR(254),
    phone VARCHAR(40) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_guardians_full_name_not_blank CHECK (length(trim(full_name)) > 0),
    CONSTRAINT ck_guardians_phone_not_blank CHECK (length(trim(phone)) > 0)
);

CREATE TABLE guardian_students (
    guardian_id UUID NOT NULL REFERENCES guardians(id) ON DELETE CASCADE,
    student_id UUID NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    relationship VARCHAR(40) NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (guardian_id, student_id),
    CONSTRAINT ck_guardian_students_relationship CHECK (relationship IN ('FATHER', 'MOTHER', 'LEGAL_GUARDIAN', 'GRANDPARENT', 'OTHER'))
);

CREATE UNIQUE INDEX uk_guardian_students_one_primary ON guardian_students(student_id) WHERE is_primary;

CREATE TABLE staff_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    user_id UUID NOT NULL REFERENCES users(id),
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    display_name VARCHAR(160) NOT NULL,
    primary_unit_id UUID REFERENCES units(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_staff_profiles_type CHECK (type IN ('PROFESSOR', 'INSTRUCTOR')),
    CONSTRAINT ck_staff_profiles_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT ck_staff_profiles_display_name_not_blank CHECK (length(trim(display_name)) > 0),
    CONSTRAINT uk_staff_profiles_user_type UNIQUE (tenant_id, user_id, type)
);

CREATE INDEX ix_students_tenant_name ON students(tenant_id, full_name);
CREATE INDEX ix_students_tenant_status ON students(tenant_id, status);
CREATE INDEX ix_students_tenant_unit ON students(tenant_id, primary_unit_id);
CREATE INDEX ix_students_tenant_user ON students(tenant_id, user_id);
CREATE INDEX ix_guardians_tenant_name ON guardians(tenant_id, full_name);
CREATE INDEX ix_guardians_tenant_user ON guardians(tenant_id, user_id);
CREATE INDEX ix_guardian_students_student ON guardian_students(student_id);
CREATE INDEX ix_staff_profiles_tenant_type ON staff_profiles(tenant_id, type);
CREATE INDEX ix_staff_profiles_tenant_status ON staff_profiles(tenant_id, status);
CREATE INDEX ix_staff_profiles_tenant_unit ON staff_profiles(tenant_id, primary_unit_id);

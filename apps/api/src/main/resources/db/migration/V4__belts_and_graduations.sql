CREATE TABLE belts (
    id UUID PRIMARY KEY,
    code VARCHAR(40) NOT NULL,
    display_name VARCHAR(80) NOT NULL,
    system VARCHAR(20) NOT NULL,
    sequence INTEGER NOT NULL,
    max_degree INTEGER NOT NULL DEFAULT 4,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_belts_system_code UNIQUE (system, code),
    CONSTRAINT uk_belts_system_sequence UNIQUE (system, sequence),
    CONSTRAINT ck_belts_system CHECK (system IN ('GENERAL', 'KIDS')),
    CONSTRAINT ck_belts_sequence_positive CHECK (sequence > 0),
    CONSTRAINT ck_belts_max_degree CHECK (max_degree >= 0),
    CONSTRAINT ck_belts_code_not_blank CHECK (length(trim(code)) > 0),
    CONSTRAINT ck_belts_display_name_not_blank CHECK (length(trim(display_name)) > 0)
);

INSERT INTO belts (id, code, display_name, system, sequence, max_degree, active) VALUES
('00000000-0000-4000-8000-000000000101', 'WHITE', 'Faixa Branca', 'GENERAL', 1, 4, true),
('00000000-0000-4000-8000-000000000102', 'BLUE', 'Faixa Azul', 'GENERAL', 2, 4, true),
('00000000-0000-4000-8000-000000000103', 'PURPLE', 'Faixa Roxa', 'GENERAL', 3, 4, true),
('00000000-0000-4000-8000-000000000104', 'BROWN', 'Faixa Marrom', 'GENERAL', 4, 4, true),
('00000000-0000-4000-8000-000000000105', 'BLACK', 'Faixa Preta', 'GENERAL', 5, 6, true),
('00000000-0000-4000-8000-000000000201', 'WHITE', 'Faixa Branca Infantil', 'KIDS', 1, 4, true),
('00000000-0000-4000-8000-000000000202', 'GREY_WHITE', 'Faixa Cinza/Branca', 'KIDS', 2, 4, true),
('00000000-0000-4000-8000-000000000203', 'GREY', 'Faixa Cinza', 'KIDS', 3, 4, true),
('00000000-0000-4000-8000-000000000204', 'GREY_BLACK', 'Faixa Cinza/Preta', 'KIDS', 4, 4, true),
('00000000-0000-4000-8000-000000000205', 'YELLOW_WHITE', 'Faixa Amarela/Branca', 'KIDS', 5, 4, true),
('00000000-0000-4000-8000-000000000206', 'YELLOW', 'Faixa Amarela', 'KIDS', 6, 4, true),
('00000000-0000-4000-8000-000000000207', 'YELLOW_BLACK', 'Faixa Amarela/Preta', 'KIDS', 7, 4, true),
('00000000-0000-4000-8000-000000000208', 'ORANGE_WHITE', 'Faixa Laranja/Branca', 'KIDS', 8, 4, true),
('00000000-0000-4000-8000-000000000209', 'ORANGE', 'Faixa Laranja', 'KIDS', 9, 4, true),
('00000000-0000-4000-8000-000000000210', 'ORANGE_BLACK', 'Faixa Laranja/Preta', 'KIDS', 10, 4, true),
('00000000-0000-4000-8000-000000000211', 'GREEN_WHITE', 'Faixa Verde/Branca', 'KIDS', 11, 4, true),
('00000000-0000-4000-8000-000000000212', 'GREEN', 'Faixa Verde', 'KIDS', 12, 4, true),
('00000000-0000-4000-8000-000000000213', 'GREEN_BLACK', 'Faixa Verde/Preta', 'KIDS', 13, 4, true)
ON CONFLICT (system, code) DO UPDATE SET
    display_name = EXCLUDED.display_name,
    system = EXCLUDED.system,
    sequence = EXCLUDED.sequence,
    max_degree = EXCLUDED.max_degree,
    active = EXCLUDED.active;

CREATE TABLE student_graduations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    student_id UUID NOT NULL REFERENCES students(id),
    belt_id UUID NOT NULL REFERENCES belts(id),
    degree INTEGER NOT NULL,
    graduated_at DATE,
    awarded_by_staff_id UUID REFERENCES staff_profiles(id),
    type VARCHAR(30) NOT NULL,
    notes TEXT,
    created_by_user_id UUID NOT NULL REFERENCES users(id),
    corrects_graduation_id UUID REFERENCES student_graduations(id),
    correction_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_student_graduations_type CHECK (type IN ('INITIAL', 'BELT_PROMOTION', 'DEGREE_PROMOTION', 'CORRECTION')),
    CONSTRAINT ck_student_graduations_degree CHECK (degree >= 0),
    CONSTRAINT ck_student_graduations_correction_reason CHECK (
        (type = 'CORRECTION' AND correction_reason IS NOT NULL AND length(trim(correction_reason)) > 0)
        OR (type <> 'CORRECTION' AND correction_reason IS NULL)
    )
);

CREATE UNIQUE INDEX uk_student_graduations_one_initial ON student_graduations(student_id) WHERE type = 'INITIAL';
CREATE INDEX ix_student_graduations_tenant_student_created ON student_graduations(tenant_id, student_id, created_at DESC);
CREATE INDEX ix_student_graduations_tenant_staff ON student_graduations(tenant_id, awarded_by_staff_id);
CREATE INDEX ix_student_graduations_corrects ON student_graduations(corrects_graduation_id);

CREATE TABLE graduation_policies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    belt_id UUID NOT NULL REFERENCES belts(id),
    minimum_training_count INTEGER,
    minimum_days_in_grade INTEGER,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_graduation_policies_tenant_belt UNIQUE (tenant_id, belt_id),
    CONSTRAINT ck_graduation_policies_training_count CHECK (minimum_training_count IS NULL OR minimum_training_count >= 0),
    CONSTRAINT ck_graduation_policies_days CHECK (minimum_days_in_grade IS NULL OR minimum_days_in_grade >= 0)
);

CREATE INDEX ix_graduation_policies_tenant ON graduation_policies(tenant_id);

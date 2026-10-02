CREATE TABLE foundation_schema_marker (
    id smallint PRIMARY KEY,
    applied_at timestamptz NOT NULL DEFAULT now(),
    description varchar(120) NOT NULL
);

INSERT INTO foundation_schema_marker (id, description)
VALUES (1, 'Cycle 00 foundation migration');

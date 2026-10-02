# Data Model - planejado

Core futuro: tenants, units, users, roles, permissions, user_roles, students, professors, guardians, guardian_students, belts, student_belt_history, classes, class_schedules, class_professors, training_sessions, attendances, announcements, audit_logs.

A implementacao fisica sera criada incrementalmente por migrations durante os ciclos.

No Cycle 00, Flyway cria apenas `foundation_schema_marker`, uma tabela tecnica minima para validar conexao com PostgreSQL e execucao de migrations. Nenhuma tabela de negocio foi antecipada.

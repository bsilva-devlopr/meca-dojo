# ADR-009 - Classes and Weekly Schedule

## Status

Accepted

## Context

O Meca Dojo precisa representar aulas oferecidas pela academia e sua grade semanal recorrente sem antecipar ocorrencias concretas, presenca ou check-in. Java ja possui `java.lang.Class`, entao uma entidade chamada apenas `Class` geraria ambiguidade tecnica.

## Decision

A entidade de dominio foi chamada `DojoClass`. Na API e UI ela aparece como Aula. `DojoClass` e tenant-scoped e representa a definicao do programa: nome, descricao, modalidade, publico, tipo, nivel, status e capacidade padrao opcional.

`ClassSchedule` representa a recorrencia semanal planejada: DojoClass, Unit, dia da semana, inicio, fim, capacidade especifica opcional e status. A Unit fica no schedule, nao na DojoClass, para permitir a mesma aula em varias unidades.

Professores/instrutores sao associados por `class_schedule_staff`, relacionamento N:N com papel `LEAD` ou `ASSISTANT`. O MVP permite horario sem professor para montar a grade antes de escalar equipe, mas nao permite staff duplicado nem mais de um LEAD no mesmo horario. Staff precisa pertencer ao tenant autenticado, estar ACTIVE e ser PROFESSOR ou INSTRUCTOR.

A recorrencia do Cycle 04 e somente semanal. Nao ha RRULE, feriados, excecoes, cancelamentos, substituicao por data ou calendario mensal. `TrainingSession` continua reservado para uma ocorrencia concreta em data especifica e sera tratado em ciclo futuro.

Conflitos de horario na mesma Unit nao sao bloqueados porque ainda nao existe conceito de sala/tatame. Academias podem ter aulas paralelas.

## Consequences

A agenda semanal pode ser renderizada com um unico endpoint (`GET /api/v1/schedules/weekly`). A capacidade efetiva e calculada como `capacityOverride ?? dojoClass.defaultCapacity` e nao e persistida.

A autorizacao usa permissions `CLASS_*` e `SCHEDULE_*`. OWNER e ADMIN administram; PROFESSOR, INSTRUCTOR e RECEPTION leem a grade.

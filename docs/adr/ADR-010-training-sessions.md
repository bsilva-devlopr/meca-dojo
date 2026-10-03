# ADR-010 - Training Sessions

## Status

Accepted

## Context

ClassSchedule representa a recorrencia semanal planejada da academia. O produto agora precisa operar treinos reais em datas especificas, com professor efetivo, unidade, horario, status e excecoes sem alterar retroativamente a grade.

## Decision

Criamos `TrainingSession` como ocorrencia concreta tenant-scoped. Ela guarda `dojoClassId`, `classScheduleId` opcional, `unitId`, `sessionDate`, `startTime`, `endTime`, `status`, `capacity`, `notes`, `createdByUserId`, timestamps e metadados de cancelamento.

`classScheduleId` e nullable para suportar treinos manuais, como aulao, seminario ou treino extra. `dojoClassId` e `unitId` continuam obrigatorios.

TrainingSession usa snapshot operacional. Ao criar a partir da grade, copiamos aula, unidade, data, horario, capacidade efetiva e staff do schedule. Alteracoes posteriores em DojoClass/ClassSchedule nao mudam sessoes ja criadas. `sessionDate`, `startTime` e `endTime` representam horario local operacional da Unit; timezone por Unit fica para ciclo futuro.

Professores da sessao ficam em `training_session_staff`, com roles `LEAD` e `ASSISTANT`, zero ou um `LEAD` e N assistentes. Substituicao altera apenas a sessao.

Estados permitidos:

- `SCHEDULED -> OPEN`
- `OPEN -> COMPLETED`
- `SCHEDULED -> CANCELLED`
- `OPEN -> CANCELLED`

Nao ha `reopen` neste ciclo. Cancelamento preserva a sessao e registra `cancelledAt`, `cancelledByUserId` e `cancellationReason` obrigatorio.

A geracao da grade e explicita por `POST /api/v1/training-sessions/generate`, limitada a 31 dias. Ela le schedules ACTIVE, calcula datas por DayOfWeek, copia snapshot e staff, e nao duplica sessoes existentes. A idempotencia e reforcada por constraint parcial `UNIQUE(class_schedule_id, session_date) WHERE class_schedule_id IS NOT NULL`.

## Consequences

`/app/schedule` permanece como grade recorrente. `/app/training` passa a ser a tela operacional por data, com abrir, encerrar, cancelar, editar, gerar da grade e criar treino manual.

Attendance, check-in, QR Code, frequencia, reservas e waitlist continuam fora deste ciclo.

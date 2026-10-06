# ADR-011 - Attendance and Check-in

## Status

Accepted

## Context

TrainingSession passou a representar treinos reais. O produto agora precisa registrar presenca oficial de alunos, permitir lancamento manual no tatame e oferecer check-in por QR sem permitir que o navegador escolha arbitrariamente outro aluno.

## Decision

`Attendance` e a fonte de verdade de frequencia. Ela liga `Student` a `TrainingSession`, guarda `source`, `checkedInAt`, usuario registrador opcional, `checkInId` opcional e status `ACTIVE` ou `VOIDED`.

`CheckIn` registra a tentativa operacional. Um QR aceito cria Attendance ACTIVE. Tentativas duplicadas, expiradas, invalidas, com sessao fechada, aluno invalido ou usuario sem Student vinculado ficam auditadas em CheckIn sem contar frequencia.

Uma presenca ACTIVE por Student e TrainingSession e garantida no banco por indice unico parcial. `VOIDED` nao conta em frequencia e libera novo registro ACTIVE para correcao operacional.

TrainingSession precisa estar OPEN para aceitar Attendance. Students ACTIVE e TRIAL podem treinar; INACTIVE e SUSPENDED sao rejeitados.

QR usa token opaco aleatorio criptograficamente forte. O backend persiste apenas SHA-256 do token, vinculado a tenant e TrainingSession, com expiracao configuravel por `app.check-in.qr-ttl-seconds` e default de 60 segundos. O mesmo token pode ser usado por varios alunos durante a janela de validade; ele nao e consumido no primeiro uso.

Self check-in recebe apenas o token. O backend resolve `User -> Student` pelo usuario autenticado. Aluno sem login vinculado usa registro manual por professor ou recepcao.

Attendance ACTIVE passa a alimentar resumo de frequencia do aluno e `GraduationEligibility.minimumTrainingCount`. Attendance VOIDED e CheckIn rejeitado nao contam.

## Consequences

A UI do professor no detalhe do treino OPEN mostra Presencas, registro manual por busca, QR grande com validade e correcao auditavel. `/check-in` trata os estados do aluno com mensagens simples.

Nao foi criado scheduler de limpeza de tokens expirados neste ciclo; eles permanecem para auditoria e limpeza futura.

Nao foram implementados reservas, waitlist, push, gamificacao, geolocalizacao, biometria, NFC ou app nativo.

## Cycle 07 amendment

ADR-013 supersedes immediate official counting: ACTIVE attendance is operational while the session is OPEN. Official frequency/history and minimumTrainingCount require the session to be COMPLETED. CANCELLED attendance remains auditable and does not count. Duplicate QR checks remain independent of official history.

# ADR-008 - Graduation Domain

## Status

Accepted

## Context

O Meca Dojo precisa representar graduacao de Jiu-Jitsu sem perder historico. Faixa e grau atuais nao podem ser apenas campos mutaveis em `students`, porque promocoes, correcoes e importacoes historicas precisam ser auditaveis.

Tambem precisamos suportar sistemas adulto/geral e infantil desde o inicio, sem codificar uma federacao unica como regra permanente.

## Decision

O catalogo de faixas e global e definido pelo sistema em `belts`. Ele possui `system` (`GENERAL`, `KIDS`), `code`, `display_name`, `sequence`, `max_degree` e `active`. O par `(system, code)` e unico, permitindo `WHITE` em GENERAL e KIDS.

O historico em `student_graduations` e a fonte de verdade da graduacao do aluno. A graduacao atual e derivada do evento mais recente criado para o aluno no tenant. Nao foi criada projecao `student_current_graduations` neste MVP para evitar duplicidade de estado; os fluxos de promocao usam transacao e lock simples ao ler o historico antes de calcular o proximo evento.

Eventos suportados:

- `INITIAL`: graduacao inicial ou historico anterior a entrada na academia.
- `DEGREE_PROMOTION`: incremento normal de grau, calculado pelo backend.
- `BELT_PROMOTION`: promocao normal para a proxima faixa do mesmo sistema, com grau inicial 0.
- `CORRECTION`: correcao administrativa com motivo obrigatorio e referencia ao evento corrigido quando aplicavel.

`graduated_at` e `DATE` porque graduacao e data civil. `created_at` e timestamp porque registra quando o evento foi lancado.

`awarded_by_staff_id` aponta para StaffProfile ativo do mesmo tenant, professor ou instrutor. Pode ser nulo para historico importado. `created_by_user_id` registra quem lancou no sistema.

Politicas de elegibilidade sao tenant-scoped em `graduation_policies`. O ciclo atual usa `minimum_days_in_grade`. `minimum_training_count` fica modelado, mas gera warning enquanto Attendance/CheckIn nao existir.

## Consequences

O sistema nunca promove automaticamente. Elegibilidade significa apenas que o aluno pode ser avaliado.

O historico nao e apagado nem reescrito silenciosamente. Erros sao tratados por evento `CORRECTION`.

Regras infantis avancadas, idade minima, transicao infantil para adulto e contagem real de treinos ficam para ciclos futuros.

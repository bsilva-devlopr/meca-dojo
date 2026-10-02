# ADR-007 - People Domain Model

Status: Accepted.

Decisao: separar identidade de perfis de negocio.

`User` representa login, email de autenticacao, senha, roles, tenant e seguranca. `Student` representa a ficha administrativa do aluno. `StaffProfile` representa a pessoa exercendo funcao de professor ou instrutor dentro da academia. `Guardian` representa responsavel por um ou mais alunos.

Regras principais:

- Student possui `user_id` opcional para permitir cadastro antes do acesso ao sistema.
- Guardian possui `user_id` opcional pelo mesmo motivo.
- StaffProfile exige `user_id` neste MVP porque professor/instrutor usa o sistema.
- O mesmo User pode ter Student e StaffProfile.
- Role PROFESSOR/STUDENT controla autorizacao; StaffProfile type PROFESSOR e Student representam dominio.
- Student possui `primary_unit_id`, mas isso nao limita treinos multiunidade futuros.
- `Student.notes` e campo administrativo e nao substitui prontuario medico.

Nao foram implementados neste ciclo: faixa, graduacao, turmas, treinos, check-in, financeiro, competicoes, prontuario medico, loja ou CRM.

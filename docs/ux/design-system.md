# Meca Dojo Design System

## Principios visuais

A interface do Meca Dojo deve parecer moderna, premium, esportiva, minimalista, rapida e mobile-first. Ela deve apoiar a rotina real de uma academia de Jiu-Jitsu sem parecer um ERP antigo ou painel CRUD generico.

A identidade usa preto/grafite, branco/off-white e roxo como accent principal. O roxo destaca a marca e acoes primarias, mas nao domina todas as superficies. As cores das faixas aparecem no contexto esportivo, especialmente em graduacao.

Evitar neon excessivo, sombras pesadas, gradientes exagerados, glassmorphism em excesso, emojis como icones estruturais e informacoes fake.

## Tokens

Os tokens ficam centralizados em `apps/web/app/globals.css` como CSS variables e expostos no Tailwind por `apps/web/tailwind.config.ts`.

Tokens principais:

- `background`
- `surface`
- `surfaceElevated`
- `foreground`
- `mutedForeground`
- `border`
- `primary`
- `primaryForeground`
- `success`
- `warning`
- `danger`
- `info`
- `radius`
- `shadow-soft`
- `shadow-card`

O design esta preparado para dark mode por classe `.dark`, mas o toggle visual ainda nao faz parte do produto.

## Tipografia

Hierarquia atual:

- Display: chamadas institucionais e login.
- Page Title: titulo principal de pagina.
- Section Title: rotulos curtos em uppercase.
- Card Title: titulo interno de cards.
- Body: texto comum em 14px com boa altura de linha.
- Label: campos e controles.
- Caption: metadados e contexto.

Usar poucas escalas e priorizar leitura em celular.

## Espacamento, radius e sombra

Cards usam radius amplo (`rounded-2xl`) e sombra suave. Superficies devem respirar com padding consistente, normalmente `p-4`, `p-5` ou `p-6`.

Sombras devem indicar elevacao leve, nunca peso visual.

## Componentes

Componentes base ficam em `apps/web/src/components/ui`:

- `Button` e `ButtonLink`
- `Card` e `CardHeader`
- `Badge` e `StatusBadge`
- `Input`, `Select`, `Textarea`, `FormField`
- `PageHeader`
- `EmptyState`
- `LoadingState`
- `ErrorState`

Novas telas devem reutilizar esses componentes antes de criar estilos locais.

## App shell

Desktop usa sidebar fixa, compacta e escura, com icones consistentes por `lucide-react`. Mobile usa bottom navigation para os modulos principais atuais.

O header superior fica limpo e preparado para breadcrumb, busca, notificacoes ou acoes futuras, sem criar botoes fake.

## Status colors

- `success`: estados ativos ou confirmados.
- `warning`: atencao, suspensao ou correcoes.
- `danger`: erro ou risco.
- `info`: estados informativos como trial.
- `primary`: marca e acoes principais.

Cor nunca deve ser o unico indicador: sempre usar texto junto.

## Belt representation

`BeltBadge` representa visualmente a faixa inspirada na faixa de Jiu-Jitsu, com trilho de graus e texto sempre visivel. Deve suportar faixas adultas e infantis combinadas.

Nao usar apenas `[PURPLE]` ou cor isolada. Sempre mostrar nome, grau e sistema.

## Responsividade

Mobile-first. Validar mentalmente e, quando possivel, visualmente:

- 375px
- 768px
- 1024px
- 1440px

Tabelas devem virar cards em celular. Formularios nao devem comprimir muitos campos lado a lado em telas pequenas.

## Acessibilidade

Requisitos minimos:

- contraste adequado;
- foco visivel;
- labels ou `aria-label` em controles;
- botoes semanticamente corretos;
- texto alem de cor para status;
- icones com `aria-hidden` quando decorativos;
- empty/loading/error states compreensiveis.

## Feedback

Operacoes devem informar resultado por feedback inline ou componente dedicado. Nao usar `alert()` do navegador.

## Loading e empty states

Preferir skeletons simples e mensagens claras. Empty states devem orientar proxima acao real, sem dizer apenas “Nenhum registro encontrado”.


## Schedule experience

A agenda semanal recorrente deve ser a experiencia principal para aulas. No desktop, usar uma grade por dias da semana com cards compactos e legiveis. No mobile, nao comprimir sete colunas: usar seletor horizontal de dias e lista cronologica vertical.

Com TrainingSession no Cycle 05, `/app/schedule` permanece como grade semanal recorrente e `/app/training` concentra as ocorrencias reais por data. A experiencia de Treinos deve priorizar mobile: data atual, ontem/hoje/amanha, horario, aula, professor, unidade, status textual e acao operacional principal.

Nao usar linguagem tecnica como "materializar recorrencia" na UI. O fluxo administrativo deve dizer "Gerar treinos da grade".

Modality badges devem usar texto sempre visivel: Gi, No-Gi, Mista ou Outra. A cor e suporte visual, nunca o unico indicador.

A navegacao mobile deve priorizar poucos destinos. Com o crescimento de modulos, manter itens principais como Inicio, Alunos, Agenda e Mais, em vez de lotar a barra inferior.

# Relatório — Sistema de Gestão de Produtos

**Disciplina:** Programação Orientada a Objetos III — **Avaliação:** P1
**Professor:** Daniel Corrêa da Silva
**Aluno(a):**Cauã Dionis 
**Curso:** ESN4

## 1. Funcionalidades implementadas

**Operações básicas (interface JavaFX)**
- Cadastrar produto com todos os dados (código, nome, descrição, datas de fabricação e validade, preços de compra e venda, estoque e categoria).
- Excluir produto selecionado na tabela, com confirmação.
- Consultar produto por código.
- Listar todos os produtos em uma tabela.

**Persistência:** os dados são gravados em `data/produtos.csv`, separado por ponto-e-vírgula. O arquivo é carregado ao iniciar o programa e regravado após cada cadastro ou exclusão.

**Validação:** código com 8 caracteres alfanuméricos e único; nome com no mínimo 3 caracteres; fabricação não posterior a hoje; validade não anterior à fabricação; preços positivos com venda maior que compra; estoque não negativo; categoria obrigatória. Todos os erros são mostrados juntos ao usuário.

**Relatórios com Stream API** (os quatro sugeridos no enunciado, mais um):
1. Produtos próximos ao vencimento (60 dias) — `filter` + `sorted`.
2. Produtos com estoque baixo (menos de 10 unidades) — `filter` + `sorted`.
3. Margem de lucro média por categoria — `groupingBy` + `collectingAndThen` + `map` + `reduce`.
4. Produtos agrupados por setor da categoria — `sorted` + `groupingBy`.
5. Valor total do estoque — `map` + `reduce`.

## 2. Atendimento aos critérios de avaliação

| Critério | Onde está |
|----------|-----------|
| Interface JavaFX e controlador com tratamento de eventos (formulário, tabela, relatório) — 6,0 | `main.fxml` e `ui/MainController` (`onSalvar`, `onConsultar`, `onListarTodos`, `onExcluir`, `onAtualizarRelatorio`) |
| Classes do modelo de negócio — 2,0 | `model/Categoria` e `model/Produto` (composição: um produto tem uma categoria) |
| Relatório com Streams — 2,0 | `report/RelatorioService` |

## 3. Decisões de design

**Separação em camadas.** Modelo (`model`), regras de validação (`validation`), persistência (`repository`), regras de negócio (`service`), relatórios (`report`) e interface (`ui`). O controlador JavaFX não conhece o CSV nem faz cálculos: só lê o formulário, chama o serviço e exibe o resultado. Isso deixa toda a lógica testável sem abrir janela.

**Injeção de dependências.** `App` cria o repositório e o serviço e os entrega ao controlador por meio de um `controllerFactory` do `FXMLLoader`, em vez de o controlador criar suas próprias dependências.

**Tratamento de exceções.** Três exceções próprias e verificadas: `ValidacaoException` (guarda a lista completa de erros), `ProdutoNaoEncontradoException` e `PersistenciaException`. A interface captura cada uma e mostra uma mensagem clara; se a gravação falhar, o serviço desfaz a alteração em memória para que a tela e o arquivo não fiquem divergentes.

**Persistência robusta.** A gravação é feita em arquivo temporário e depois substitui o original, evitando CSV corrompido se algo falhar no meio. Textos digitados com `;` ou quebra de linha são sanitizados para não quebrar o formato. A leitura aceita arquivos salvos pelo Excel (BOM UTF-8, codificação ANSI, preço com vírgula) e informa o número da linha em caso de erro.

**Dinheiro e datas.** `BigDecimal` para preços (sem erros de arredondamento de `double`) e `LocalDate` para datas.

**Código único sem diferenciar maiúsculas/minúsculas.** `prd00001` e `PRD00001` são o mesmo produto; o serviço usa um mapa com o código em maiúsculas como chave.

**Margem de lucro** definida como `(venda − compra) / venda × 100`, e a média por categoria dá o mesmo peso a cada produto.

**"Próximo ao vencimento"** considera produtos que vencem de hoje até hoje + 60 dias (inclusive); produtos já vencidos não entram nesse relatório.

**Relógio injetável.** Serviço e relatórios recebem um `Clock`, o que permite testar regras que dependem da data de hoje com uma data fixa.

**Ordenação em português.** Relatórios usam `Collator` pt-BR para que nomes com acento ("Água") fiquem na ordem alfabética correta.

## 4. Testes

Testes automatizados com JUnit 5 (`mvn test`) cobrindo validação, leitura/gravação do CSV, serviço (cadastro, duplicidade, exclusão, persistência entre execuções) e os cinco relatórios, com data fixa de 23/10/2026.

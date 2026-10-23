# Gestão de Produtos — P1 de Programação Orientada a Objetos III (UNIFAN)

Sistema em Java com interface gráfica **JavaFX** para gerenciar o cadastro de produtos de uma loja ou supermercado.
Os dados ficam em um arquivo **CSV separado por ponto-e-vírgula** e os relatórios usam a **API Stream** do Java.

**Aluno: Cauã Dionis**                               **Curso: ESN4**

---

## 1. O que você precisa instalar

| Item | Versão | Como conferir |
|------|--------|---------------|
| JDK (Java) | **17 ou superior** | `java -version` |
| Maven | 3.8 ou superior | `mvn -version` |

- **JDK:** pode ser o [Temurin](https://adoptium.net) (Windows: baixe o instalador `.msi` e marque a opção que adiciona ao PATH).
- **Maven:** baixe em <https://maven.apache.org/download.cgi>, descompacte e adicione a pasta `bin` ao PATH.
  Se preferir não instalar o Maven, use o **IntelliJ IDEA** (ele já traz o Maven embutido) — veja a seção 3.
- **JavaFX não precisa ser instalado**: o Maven baixa sozinho (é uma dependência do `pom.xml`).

## 2. Como executar (terminal)

1. Extraia o ZIP e abra um terminal **dentro da pasta do projeto** (a que contém o arquivo `pom.xml`).
2. Rode os testes automatizados (opcional, mas recomendado):
   ```
   mvn test
   ```
3. Abra o programa:
   ```
   mvn javafx:run
   ```

Na primeira execução o Maven baixa as dependências (JavaFX, JUnit etc.), então é preciso estar com internet. A janela do sistema abre com 12 produtos de exemplo.

> **Importante:** execute sempre a partir da pasta do projeto. O arquivo de dados é `data/produtos.csv`, caminho relativo a essa pasta.

## 3. Como executar pela IDE

**IntelliJ IDEA** (mais simples)
1. `File > Open...` e escolha o arquivo `pom.xml` → **Open as Project**. Aguarde o Maven importar (barra de progresso no canto inferior direito).
2. Abra `src/main/java/br/edu/unifan/produtos/Launcher.java`, clique com o botão direito → **Run 'Launcher.main()'**.
   (Use `Launcher` e não `App`: rodar direto uma classe que estende `Application` costuma dar o erro *"JavaFX runtime components are missing"*.)
3. Para rodar os testes: botão direito na pasta `src/test/java` → **Run 'All Tests'**.

**Eclipse:** `File > Import > Maven > Existing Maven Projects`, selecione a pasta e rode `Launcher` como *Java Application*.

**VS Code:** instale o *Extension Pack for Java*, abra a pasta e use o terminal integrado com `mvn javafx:run`.

## 4. Como usar o sistema

**Aba Produtos**
- **Cadastrar:** preencha o formulário à esquerda e clique em **Salvar**. Campos com `*` são obrigatórios. Se algo estiver inválido, uma janela lista *todos* os problemas de uma vez.
- **Consultar por código:** digite o código (ex.: `PRD00001`) e clique em **Consultar** (ou pressione Enter). A tabela passa a mostrar só aquele produto.
- **Listar todos:** botão **Listar todos** volta a mostrar a lista completa.
- **Excluir:** selecione uma linha da tabela e clique em **Excluir selecionado** (pede confirmação).
- Cada operação grava o CSV imediatamente.

**Aba Relatórios** — escolha o relatório na lista (o texto é gerado na hora; **Atualizar** recalcula):
1. Produtos próximos ao vencimento (próximos 60 dias, não inclui os já vencidos)
2. Produtos com estoque baixo (menos de 10 unidades)
3. Margem de lucro média por categoria
4. Produtos agrupados por setor
5. Valor total do estoque (preço de compra)

## 5. Regras de validação

| Campo | Regra |
|-------|-------|
| Código | exatamente 8 caracteres alfanuméricos (ex.: `PRD00013`); único, sem diferenciar maiúsculas de minúsculas |
| Nome | não vazio, mínimo de 3 caracteres |
| Data de fabricação | obrigatória e não pode ser posterior à data atual |
| Data de validade | obrigatória e não pode ser anterior à fabricação |
| Preços | positivos; o preço de venda deve ser **maior** que o de compra |
| Estoque | inteiro, não negativo |
| Categoria | obrigatória |

Preços aceitam vírgula ou ponto (`12,50` ou `12.50`). Datas no formulário: `dd/mm/aaaa`.

## 6. Arquivo CSV

Local: `data/produtos.csv` (criado automaticamente, com cabeçalho, se não existir). Uma linha por produto, campos separados por `;`:

```
codigo;nome;descricao;dataFabricacao;dataValidade;precoCompra;precoVenda;quantidadeEstoque;categoriaId;categoriaNome;categoriaDescricao;categoriaSetor
PRD00001;Leite Integral 1L;Leite UHT integral caixa 1 litro;2026-09-20;2026-11-15;4.20;5.99;120;1;Laticínios;Leite, queijos e iogurtes;Frios e Laticínios
```

- Datas no formato `aaaa-mm-dd` e preços com ponto decimal.
- Se for editar no Excel: **feche o arquivo antes de usar o sistema** (aberto no Excel, o Windows impede a gravação) e mantenha as datas no formato `aaaa-mm-dd`.
- Os produtos de exemplo têm datas próximas de outubro de 2026; os relatórios de vencimento dependem da data de hoje do seu computador, então o resultado muda com o tempo.
- Para começar do zero, apague `data/produtos.csv` (ou deixe só a linha de cabeçalho).

## 7. Estrutura do projeto

```
gestao-produtos/
├── pom.xml
├── data/produtos.csv                      dados (persistência)
└── src/
    ├── main/java/br/edu/unifan/produtos/
    │   ├── App.java, Launcher.java        ponto de entrada JavaFX
    │   ├── model/                         Categoria, Produto
    │   ├── validation/ProdutoValidador    regras de validação
    │   ├── exception/                     ValidacaoException, PersistenciaException, ProdutoNaoEncontradoException
    │   ├── repository/CsvProdutoRepository  leitura/gravação do CSV
    │   ├── service/                       ProdutoService (regras), CategoriaCatalogo
    │   ├── report/                        RelatorioService (Stream API), RelatorioFormatter
    │   └── ui/MainController              controlador da tela (eventos)
    ├── main/resources/.../ui/main.fxml    layout da tela
    └── test/java/...                      testes JUnit 5
```

## 8. Problemas comuns

| Mensagem / sintoma | Solução |
|--------------------|---------|
| `release version 17 not supported` / `invalid target release` | Seu JDK é anterior ao 17. Instale o JDK 17+ e confira com `java -version`. |
| `JavaFX runtime components are missing` | Rode por `mvn javafx:run` ou pela classe `Launcher` (não por `App`). |
| `mvn` não é reconhecido | O Maven não está no PATH. Instale-o (seção 1) ou use o IntelliJ. |
| Janela de erro "Não foi possível carregar os produtos" | Alguma linha do CSV está fora do formato; a mensagem informa o número da linha. |
| "Não foi possível gravar o arquivo" | O CSV está aberto em outro programa (Excel). Feche e tente de novo. |
| Tabela abre vazia | Você executou fora da pasta do projeto, então o programa criou outro `data/produtos.csv` vazio. |


package br.edu.unifan.produtos.ui;

import br.edu.unifan.produtos.exception.PersistenciaException;
import br.edu.unifan.produtos.exception.ProdutoNaoEncontradoException;
import br.edu.unifan.produtos.exception.ValidacaoException;
import br.edu.unifan.produtos.model.Categoria;
import br.edu.unifan.produtos.model.Produto;
import br.edu.unifan.produtos.report.RelatorioFormatter;
import br.edu.unifan.produtos.report.RelatorioService;
import br.edu.unifan.produtos.service.ProdutoService;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Controlador da tela principal: trata os eventos do formulário, da tabela de listagem e da exibição de relatórios.
 */
public class MainController {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private enum Relatorio {
        PROXIMOS_VENCIMENTO("Produtos próximos ao vencimento (" + RelatorioService.DIAS_PARA_VENCER + " dias)"),
        ESTOQUE_BAIXO("Produtos com estoque baixo (< " + RelatorioService.LIMITE_ESTOQUE_BAIXO + " unidades)"),
        MARGEM_CATEGORIA("Margem de lucro média por categoria"),
        POR_SETOR("Produtos agrupados por setor"),
        VALOR_ESTOQUE("Valor total do estoque (preço de compra)");

        private final String rotulo;

        Relatorio(String rotulo) {
            this.rotulo = rotulo;
        }

        @Override
        public String toString() {
            return rotulo;
        }
    }

    private final ProdutoService service;
    private final RelatorioService relatorios;
    private final ObservableList<Produto> itens = FXCollections.observableArrayList();

    // Formulário
    @FXML private TextField txtCodigo;
    @FXML private TextField txtNome;
    @FXML private TextField txtDescricao;
    @FXML private DatePicker dpFabricacao;
    @FXML private DatePicker dpValidade;
    @FXML private TextField txtPrecoCompra;
    @FXML private TextField txtPrecoVenda;
    @FXML private TextField txtEstoque;
    @FXML private ComboBox<Categoria> cbCategoria;

    // Consulta e tabela
    @FXML private TextField txtBusca;
    @FXML private TableView<Produto> tabela;
    @FXML private TableColumn<Produto, String> colCodigo;
    @FXML private TableColumn<Produto, String> colNome;
    @FXML private TableColumn<Produto, String> colCategoria;
    @FXML private TableColumn<Produto, String> colSetor;
    @FXML private TableColumn<Produto, String> colFabricacao;
    @FXML private TableColumn<Produto, String> colValidade;
    @FXML private TableColumn<Produto, String> colPrecoCompra;
    @FXML private TableColumn<Produto, String> colPrecoVenda;
    @FXML private TableColumn<Produto, String> colEstoque;

    // Relatórios
    @FXML private ComboBox<Relatorio> cbRelatorio;
    @FXML private TextArea txtRelatorio;

    @FXML private Label lblStatus;

    public MainController(ProdutoService service, RelatorioService relatorios) {
        this.service = service;
        this.relatorios = relatorios;
    }

    @FXML
    private void initialize() {
        configurarColunas();
        tabela.setItems(itens);
        tabela.setPlaceholder(new Label("Nenhum produto para exibir."));

        configurarDatePicker(dpFabricacao);
        configurarDatePicker(dpValidade);
        cbCategoria.setItems(FXCollections.observableArrayList(service.categorias()));

        txtBusca.setOnAction(e -> onConsultar());

        cbRelatorio.setItems(FXCollections.observableArrayList(Relatorio.values()));
        cbRelatorio.getSelectionModel().selectFirst();
        cbRelatorio.valueProperty().addListener((obs, antigo, novo) -> gerarRelatorio());

        atualizarTabela();
        gerarRelatorio();
        status("Pronto. " + itens.size() + " produto(s) carregado(s).");
    }

    // ------------------------------------------------------------------ eventos

    @FXML
    private void onSalvar() {
        try {
            Produto produto = lerFormulario();
            service.cadastrar(produto);
            atualizarTabela();
            gerarRelatorio();
            limparFormulario();
            status("Produto " + produto.getCodigo() + " cadastrado com sucesso.");
        } catch (ValidacaoException e) {
            mostrarErro("Dados inválidos", e.getErros().stream()
                    .map(erro -> "• " + erro)
                    .collect(Collectors.joining("\n")));
        } catch (PersistenciaException e) {
            mostrarErro("Erro ao gravar o arquivo", e.getMessage());
        }
    }

    @FXML
    private void onLimpar() {
        limparFormulario();
        status("Formulário limpo.");
    }

    @FXML
    private void onConsultar() {
        String codigo = txtBusca.getText() == null ? "" : txtBusca.getText().trim();
        if (codigo.isEmpty()) {
            status("Informe um código para consultar.");
            return;
        }
        Optional<Produto> encontrado = service.buscarPorCodigo(codigo);
        if (encontrado.isPresent()) {
            itens.setAll(encontrado.get());
            tabela.getSelectionModel().selectFirst();
            status("Produto " + encontrado.get().getCodigo() + " encontrado. Clique em \"Listar todos\" para ver a lista completa.");
        } else {
            itens.clear();
            status("Nenhum produto com o código " + codigo + ".");
        }
    }

    @FXML
    private void onListarTodos() {
        txtBusca.clear();
        atualizarTabela();
        status(itens.size() + " produto(s) listado(s).");
    }

    @FXML
    private void onExcluir() {
        Produto selecionado = tabela.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            status("Selecione um produto na tabela para excluir.");
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION,
                "Excluir o produto " + selecionado.getCodigo() + " - " + selecionado.getNome() + "?",
                ButtonType.YES, ButtonType.NO);
        confirmacao.setTitle("Confirmar exclusão");
        confirmacao.setHeaderText(null);
        if (confirmacao.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
            return;
        }

        try {
            service.excluir(selecionado.getCodigo());
            atualizarTabela();
            gerarRelatorio();
            status("Produto " + selecionado.getCodigo() + " excluído.");
        } catch (ProdutoNaoEncontradoException e) {
            mostrarErro("Produto não encontrado", e.getMessage());
        } catch (PersistenciaException e) {
            mostrarErro("Erro ao gravar o arquivo", e.getMessage());
        }
    }

    @FXML
    private void onAtualizarRelatorio() {
        gerarRelatorio();
        status("Relatório atualizado.");
    }

    // ------------------------------------------------------------------ relatórios

    private void gerarRelatorio() {
        Relatorio escolhido = cbRelatorio.getValue();
        if (escolhido == null) {
            return;
        }
        List<Produto> todos = service.listar();
        String texto = switch (escolhido) {
            case PROXIMOS_VENCIMENTO -> RelatorioFormatter.proximosAoVencimento(
                    relatorios.proximosAoVencimento(todos, RelatorioService.DIAS_PARA_VENCER),
                    relatorios.hoje(), RelatorioService.DIAS_PARA_VENCER);
            case ESTOQUE_BAIXO -> RelatorioFormatter.estoqueBaixo(
                    relatorios.estoqueBaixo(todos, RelatorioService.LIMITE_ESTOQUE_BAIXO),
                    RelatorioService.LIMITE_ESTOQUE_BAIXO);
            case MARGEM_CATEGORIA -> RelatorioFormatter.margemPorCategoria(
                    relatorios.margemMediaPorCategoria(todos));
            case POR_SETOR -> RelatorioFormatter.porSetor(relatorios.agruparPorSetor(todos));
            case VALOR_ESTOQUE -> RelatorioFormatter.valorTotalEstoque(
                    relatorios.valorTotalEstoque(todos), todos.size());
        };
        txtRelatorio.setText(texto);
    }

    // ------------------------------------------------------------------ formulário

    private Produto lerFormulario() throws ValidacaoException {
        List<String> erros = new ArrayList<>();
        BigDecimal precoCompra = lerDecimal(txtPrecoCompra.getText(), "Preço de compra", erros);
        BigDecimal precoVenda = lerDecimal(txtPrecoVenda.getText(), "Preço de venda", erros);
        int estoque = lerInteiro(txtEstoque.getText(), "Quantidade em estoque", erros);
        if (!erros.isEmpty()) {
            throw new ValidacaoException(erros);
        }
        return new Produto(
                textoOuVazio(txtCodigo),
                textoOuVazio(txtNome),
                textoOuVazio(txtDescricao),
                dpFabricacao.getValue(),
                dpValidade.getValue(),
                precoCompra,
                precoVenda,
                estoque,
                cbCategoria.getValue());
    }

    private static BigDecimal lerDecimal(String texto, String campo, List<String> erros) {
        if (texto == null || texto.isBlank()) {
            erros.add(campo + " é obrigatório.");
            return null;
        }
        try {
            // Aceita vírgula ou ponto como separador decimal (ex.: 12,50 ou 12.50).
            return new BigDecimal(texto.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            erros.add(campo + " inválido: digite apenas números (ex.: 12,50).");
            return null;
        }
    }

    private static int lerInteiro(String texto, String campo, List<String> erros) {
        if (texto == null || texto.isBlank()) {
            erros.add(campo + " é obrigatória.");
            return 0;
        }
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            erros.add(campo + " inválida: digite um número inteiro (ex.: 25).");
            return 0;
        }
    }

    private static String textoOuVazio(TextField campo) {
        return campo.getText() == null ? "" : campo.getText().trim();
    }

    private void limparFormulario() {
        txtCodigo.clear();
        txtNome.clear();
        txtDescricao.clear();
        dpFabricacao.setValue(null);
        dpFabricacao.getEditor().clear();
        dpValidade.setValue(null);
        dpValidade.getEditor().clear();
        txtPrecoCompra.clear();
        txtPrecoVenda.clear();
        txtEstoque.clear();
        cbCategoria.getSelectionModel().clearSelection();
        txtCodigo.requestFocus();
    }

    // ------------------------------------------------------------------ tabela

    private void configurarColunas() {
        NumberFormat moeda = NumberFormat.getCurrencyInstance(PT_BR);
        coluna(colCodigo, Produto::getCodigo);
        coluna(colNome, Produto::getNome);
        coluna(colCategoria, p -> p.getCategoria().getNome());
        coluna(colSetor, p -> p.getCategoria().getSetor());
        coluna(colFabricacao, p -> p.getDataFabricacao().format(FORMATO_DATA));
        coluna(colValidade, p -> p.getDataValidade().format(FORMATO_DATA));
        coluna(colPrecoCompra, p -> moeda.format(p.getPrecoCompra()));
        coluna(colPrecoVenda, p -> moeda.format(p.getPrecoVenda()));
        coluna(colEstoque, p -> String.valueOf(p.getQuantidadeEstoque()));
    }

    private static void coluna(TableColumn<Produto, String> coluna, Function<Produto, String> extrator) {
        coluna.setCellValueFactory(celula -> new ReadOnlyStringWrapper(extrator.apply(celula.getValue())));
    }

    private void atualizarTabela() {
        itens.setAll(service.listar());
    }

    private static void configurarDatePicker(DatePicker picker) {
        picker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate data) {
                return data == null ? "" : data.format(FORMATO_DATA);
            }

            @Override
            public LocalDate fromString(String texto) {
                if (texto == null || texto.isBlank()) {
                    return null;
                }
                try {
                    return LocalDate.parse(texto.trim(), FORMATO_DATA);
                } catch (DateTimeParseException e) {
                    return null;
                }
            }
        });
    }

    // ------------------------------------------------------------------ mensagens

    private void status(String mensagem) {
        lblStatus.setText(mensagem);
    }

    private void mostrarErro(String titulo, String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.ERROR, mensagem, ButtonType.OK);
        alerta.setTitle("Erro");
        alerta.setHeaderText(titulo);
        alerta.showAndWait();
        status(titulo + ".");
    }
}

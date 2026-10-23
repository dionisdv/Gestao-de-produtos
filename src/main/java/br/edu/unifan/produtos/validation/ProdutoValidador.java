package br.edu.unifan.produtos.validation;

import br.edu.unifan.produtos.exception.ValidacaoException;
import br.edu.unifan.produtos.model.Produto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Regras de validação de um {@link Produto}. Coleta todos os erros encontrados
 * e os lança juntos em uma {@link ValidacaoException}.
 */
public final class ProdutoValidador {

    /** Código: exatamente 8 caracteres alfanuméricos (ex.: PRD00001). */
    private static final Pattern CODIGO = Pattern.compile("^[A-Za-z0-9]{8}$");
    private static final int NOME_TAMANHO_MINIMO = 3;

    private ProdutoValidador() {
    }

    public static void validar(Produto produto, LocalDate hoje) throws ValidacaoException {
        if (produto == null) {
            throw new ValidacaoException("Produto não informado.");
        }

        List<String> erros = new ArrayList<>();

        String codigo = produto.getCodigo();
        if (codigo == null || !CODIGO.matcher(codigo).matches()) {
            erros.add("Código deve ter exatamente 8 caracteres alfanuméricos (letras e números, sem espaços).");
        }

        String nome = produto.getNome();
        if (nome == null || nome.isBlank()) {
            erros.add("Nome é obrigatório.");
        } else if (nome.trim().length() < NOME_TAMANHO_MINIMO) {
            erros.add("Nome deve ter pelo menos " + NOME_TAMANHO_MINIMO + " caracteres.");
        }

        LocalDate fabricacao = produto.getDataFabricacao();
        LocalDate validade = produto.getDataValidade();
        if (fabricacao == null) {
            erros.add("Data de fabricação é obrigatória.");
        } else if (fabricacao.isAfter(hoje)) {
            erros.add("Data de fabricação não pode ser posterior à data atual.");
        }
        if (validade == null) {
            erros.add("Data de validade é obrigatória.");
        } else if (fabricacao != null && validade.isBefore(fabricacao)) {
            erros.add("Data de validade não pode ser anterior à data de fabricação.");
        }

        BigDecimal compra = produto.getPrecoCompra();
        BigDecimal venda = produto.getPrecoVenda();
        boolean compraValida = compra != null && compra.signum() > 0;
        boolean vendaValida = venda != null && venda.signum() > 0;
        if (!compraValida) {
            erros.add("Preço de compra deve ser um valor positivo.");
        }
        if (!vendaValida) {
            erros.add("Preço de venda deve ser um valor positivo.");
        }
        if (compraValida && vendaValida && venda.compareTo(compra) <= 0) {
            erros.add("Preço de venda deve ser maior que o preço de compra.");
        }

        if (produto.getQuantidadeEstoque() < 0) {
            erros.add("Quantidade em estoque não pode ser negativa.");
        }

        if (produto.getCategoria() == null) {
            erros.add("Categoria é obrigatória.");
        }

        if (!erros.isEmpty()) {
            throw new ValidacaoException(erros);
        }
    }
}

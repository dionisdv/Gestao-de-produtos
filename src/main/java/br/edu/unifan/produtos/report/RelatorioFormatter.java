package br.edu.unifan.produtos.report;

import br.edu.unifan.produtos.model.Produto;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/** Transforma os resultados dos relatórios em texto para exibição na interface. */
public final class RelatorioFormatter {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private RelatorioFormatter() {
    }

    public static String proximosAoVencimento(List<Produto> produtos, LocalDate hoje, int dias) {
        StringBuilder sb = new StringBuilder();
        sb.append("PRODUTOS PRÓXIMOS AO VENCIMENTO (próximos ").append(dias).append(" dias)\n");
        sb.append("Data de referência: ").append(hoje.format(DATA)).append("\n\n");
        if (produtos.isEmpty()) {
            return sb.append("Nenhum produto vence nesse período.").toString();
        }
        String linhas = produtos.stream()
                .map(p -> {
                    long restantes = ChronoUnit.DAYS.between(hoje, p.getDataValidade());
                    String quando = restantes == 0 ? "vence hoje" : "em " + restantes + " dia(s)";
                    return String.format("%-8s  %-28s  validade %s  (%s)  estoque: %d",
                            p.getCodigo(), cortar(p.getNome(), 28), p.getDataValidade().format(DATA),
                            quando, p.getQuantidadeEstoque());
                })
                .collect(Collectors.joining("\n"));
        return sb.append(linhas).append("\n\nTotal: ").append(produtos.size()).append(" produto(s).").toString();
    }

    public static String estoqueBaixo(List<Produto> produtos, int limite) {
        StringBuilder sb = new StringBuilder();
        sb.append("PRODUTOS COM ESTOQUE BAIXO (menos de ").append(limite).append(" unidades)\n\n");
        if (produtos.isEmpty()) {
            return sb.append("Nenhum produto com estoque baixo.").toString();
        }
        String linhas = produtos.stream()
                .map(p -> String.format("%-8s  %-28s  %-16s  estoque: %d",
                        p.getCodigo(), cortar(p.getNome(), 28), cortar(p.getCategoria().getNome(), 16),
                        p.getQuantidadeEstoque()))
                .collect(Collectors.joining("\n"));
        return sb.append(linhas).append("\n\nTotal: ").append(produtos.size()).append(" produto(s).").toString();
    }

    public static String margemPorCategoria(Map<String, BigDecimal> margens) {
        StringBuilder sb = new StringBuilder();
        sb.append("MARGEM DE LUCRO MÉDIA POR CATEGORIA\n");
        sb.append("Margem = (preço de venda - preço de compra) / preço de venda\n\n");
        if (margens.isEmpty()) {
            return sb.append("Não há produtos cadastrados.").toString();
        }
        NumberFormat numero = NumberFormat.getNumberInstance(PT_BR);
        numero.setMinimumFractionDigits(2);
        numero.setMaximumFractionDigits(2);
        String linhas = margens.entrySet().stream()
                .map(e -> String.format("%-20s  %8s %%", e.getKey(), numero.format(e.getValue())))
                .collect(Collectors.joining("\n"));
        return sb.append(linhas).toString();
    }

    public static String porSetor(Map<String, List<Produto>> porSetor) {
        StringBuilder sb = new StringBuilder();
        sb.append("PRODUTOS AGRUPADOS POR SETOR\n\n");
        if (porSetor.isEmpty()) {
            return sb.append("Não há produtos cadastrados.").toString();
        }
        String blocos = porSetor.entrySet().stream()
                .map(e -> "[" + e.getKey().toUpperCase() + "] - " + e.getValue().size() + " produto(s)\n"
                        + e.getValue().stream()
                        .map(p -> String.format("   %-8s  %-28s  %-16s  estoque: %d",
                                p.getCodigo(), cortar(p.getNome(), 28),
                                cortar(p.getCategoria().getNome(), 16), p.getQuantidadeEstoque()))
                        .collect(Collectors.joining("\n")))
                .collect(Collectors.joining("\n\n"));
        return sb.append(blocos).toString();
    }

    public static String valorTotalEstoque(BigDecimal total, int quantidadeProdutos) {
        NumberFormat moeda = NumberFormat.getCurrencyInstance(PT_BR);
        return "VALOR TOTAL DO ESTOQUE (preço de compra)\n\n"
                + "Produtos cadastrados: " + quantidadeProdutos + "\n"
                + "Valor total: " + moeda.format(total);
    }

    private static String cortar(String texto, int tamanho) {
        if (texto == null) {
            return "";
        }
        return texto.length() <= tamanho ? texto : texto.substring(0, tamanho - 1) + "…";
    }
}

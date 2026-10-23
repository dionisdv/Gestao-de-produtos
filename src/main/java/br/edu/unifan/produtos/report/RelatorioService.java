package br.edu.unifan.produtos.report;

import br.edu.unifan.produtos.model.Produto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Collator;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Relatórios do sistema, todos implementados com a API Stream (filter, map, reduce, sorted, groupingBy).
 * Os métodos recebem a coleção de produtos e não alteram nenhum dado.
 */
public class RelatorioService {

    public static final int DIAS_PARA_VENCER = 60;
    public static final int LIMITE_ESTOQUE_BAIXO = 10;

    /** Ordenação alfabética do português: "Água" vem antes de "Bebida", não depois do "Z". */
    private static final Collator ORDEM_ALFABETICA = Collator.getInstance(Locale.forLanguageTag("pt-BR"));

    private final Clock clock;

    public RelatorioService() {
        this(Clock.systemDefaultZone());
    }

    public RelatorioService(Clock clock) {
        this.clock = clock;
    }

    public LocalDate hoje() {
        return LocalDate.now(clock);
    }

    /**
     * Produtos que vencem entre hoje e hoje + {@code dias} (inclusive), do que vence primeiro para o último.
     * Produtos que já venceram não entram aqui.
     */
    public List<Produto> proximosAoVencimento(Collection<Produto> produtos, int dias) {
        LocalDate hoje = hoje();
        LocalDate limite = hoje.plusDays(dias);
        return produtos.stream()
                .filter(p -> !p.getDataValidade().isBefore(hoje) && !p.getDataValidade().isAfter(limite))
                .sorted(Comparator.comparing(Produto::getDataValidade).thenComparing(Produto::getNome))
                .toList();
    }

    /** Produtos com estoque menor que {@code limite}, do menor estoque para o maior. */
    public List<Produto> estoqueBaixo(Collection<Produto> produtos, int limite) {
        return produtos.stream()
                .filter(p -> p.getQuantidadeEstoque() < limite)
                .sorted(Comparator.comparingInt(Produto::getQuantidadeEstoque).thenComparing(Produto::getNome))
                .toList();
    }

    /**
     * Margem de lucro média (%) por nome de categoria, em ordem alfabética.
     * Cada produto tem peso igual na média da sua categoria.
     */
    public Map<String, BigDecimal> margemMediaPorCategoria(Collection<Produto> produtos) {
        return produtos.stream().collect(Collectors.groupingBy(
                p -> p.getCategoria().getNome(),
                () -> new TreeMap<String, BigDecimal>(ORDEM_ALFABETICA),
                Collectors.collectingAndThen(Collectors.toList(), lista -> lista.stream()
                        .map(Produto::getMargemLucroPercentual)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(lista.size()), 2, RoundingMode.HALF_UP))));
    }

    /** Produtos agrupados pelo setor da categoria; setores e produtos em ordem alfabética. */
    public Map<String, List<Produto>> agruparPorSetor(Collection<Produto> produtos) {
        return produtos.stream()
                .sorted(Comparator.comparing(Produto::getNome, ORDEM_ALFABETICA))
                .collect(Collectors.groupingBy(
                        p -> p.getCategoria().getSetor(),
                        () -> new TreeMap<String, List<Produto>>(ORDEM_ALFABETICA),
                        Collectors.toList()));
    }

    /** Valor total do estoque ao preço de compra: soma de (preço de compra x quantidade). */
    public BigDecimal valorTotalEstoque(Collection<Produto> produtos) {
        return produtos.stream()
                .map(p -> p.getPrecoCompra().multiply(BigDecimal.valueOf(p.getQuantidadeEstoque())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

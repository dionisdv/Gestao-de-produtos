package br.edu.unifan.produtos.report;

import br.edu.unifan.produtos.model.Produto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static br.edu.unifan.produtos.ProdutoFixture.BEBIDAS;
import static br.edu.unifan.produtos.ProdutoFixture.LATICINIOS;
import static br.edu.unifan.produtos.ProdutoFixture.MERCEARIA;
import static br.edu.unifan.produtos.ProdutoFixture.RELOGIO;
import static br.edu.unifan.produtos.ProdutoFixture.produto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** "Hoje" é 23/10/2026 (relógio fixo), portanto o limite de 60 dias é 22/12/2026. */
class RelatorioServiceTest {

    private final RelatorioService relatorios = new RelatorioService(RELOGIO);

    // margens sobre o preço de venda: p1 = 20%, p2 = 50%, p3 = 37,5%, p4 = 50%, p5 = 50%
    private final Produto p1 = produto("PRD00001", LATICINIOS, LocalDate.of(2026, 11, 10), "4.00", "5.00", 5);
    private final Produto p2 = produto("PRD00002", LATICINIOS, LocalDate.of(2027, 6, 1), "10.00", "20.00", 50);
    private final Produto p3 = produto("PRD00003", BEBIDAS, LocalDate.of(2026, 12, 22), "5.00", "8.00", 9);
    private final Produto p4 = produto("PRD00004", MERCEARIA, LocalDate.of(2026, 12, 23), "2.00", "4.00", 10);
    private final Produto p5 = produto("PRD00005", MERCEARIA, LocalDate.of(2026, 10, 1), "1.00", "2.00", 0);
    private final List<Produto> todos = List.of(p1, p2, p3, p4, p5);

    private static List<String> codigos(List<Produto> produtos) {
        return produtos.stream().map(Produto::getCodigo).toList();
    }

    private static void assertValor(String esperado, BigDecimal real) {
        assertEquals(0, new BigDecimal(esperado).compareTo(real), "esperado " + esperado + " mas foi " + real);
    }

    @Test
    void proximosAoVencimentoInclui60DiasExcluiVencidosEOrdenaPorValidade() {
        List<Produto> resultado = relatorios.proximosAoVencimento(todos, 60);

        // p3 vence exatamente em 60 dias (entra); p4 em 61 (fora); p5 já venceu (fora); p2 está longe
        assertEquals(List.of("PRD00001", "PRD00003"), codigos(resultado));
    }

    @Test
    void produtoQueVenceHojeEntraNoRelatorio() {
        Produto venceHoje = produto("PRD00006", LATICINIOS, LocalDate.of(2026, 10, 23), "1.00", "2.00", 3);

        assertEquals(List.of("PRD00006"), codigos(relatorios.proximosAoVencimento(List.of(venceHoje), 60)));
    }

    @Test
    void estoqueBaixoUsaMenorQueOLimiteEOrdenaPorQuantidade() {
        List<Produto> resultado = relatorios.estoqueBaixo(todos, 10);

        // p4 tem exatamente 10 (fora)
        assertEquals(List.of("PRD00005", "PRD00001", "PRD00003"), codigos(resultado));
    }

    @Test
    void margemMediaPorCategoria() {
        Map<String, BigDecimal> margens = relatorios.margemMediaPorCategoria(todos);

        assertEquals(List.of("Bebidas", "Laticínios", "Mercearia"), List.copyOf(margens.keySet()));
        assertValor("37.50", margens.get("Bebidas"));
        assertValor("35.00", margens.get("Laticínios"));   // (20 + 50) / 2
        assertValor("50.00", margens.get("Mercearia"));    // (50 + 50) / 2
    }

    @Test
    void agrupaProdutosPorSetorEmOrdemAlfabetica() {
        Map<String, List<Produto>> grupos = relatorios.agruparPorSetor(todos);

        assertEquals(List.of("Frios e Laticínios", "Mercearia"), List.copyOf(grupos.keySet()));
        assertEquals(List.of("PRD00001", "PRD00002"), codigos(grupos.get("Frios e Laticínios")));
        assertEquals(List.of("PRD00003", "PRD00004", "PRD00005"), codigos(grupos.get("Mercearia")));
    }

    @Test
    void valorTotalDoEstoqueSomaCompraVezesQuantidade() {
        // 4*5 + 10*50 + 5*9 + 2*10 + 1*0 = 20 + 500 + 45 + 20 + 0
        assertValor("585.00", relatorios.valorTotalEstoque(todos));
    }

    @Test
    void relatoriosComListaVaziaNaoQuebram() {
        assertTrue(relatorios.proximosAoVencimento(List.of(), 60).isEmpty());
        assertTrue(relatorios.estoqueBaixo(List.of(), 10).isEmpty());
        assertTrue(relatorios.margemMediaPorCategoria(List.of()).isEmpty());
        assertTrue(relatorios.agruparPorSetor(List.of()).isEmpty());
        assertValor("0", relatorios.valorTotalEstoque(List.of()));
    }

    @Test
    void margemDeLucroDoProdutoEhSobreOPrecoDeVenda() {
        assertValor("37.50", p3.getMargemLucroPercentual());
    }
}

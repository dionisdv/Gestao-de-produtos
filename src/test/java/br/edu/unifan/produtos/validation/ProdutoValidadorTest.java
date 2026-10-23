package br.edu.unifan.produtos.validation;

import br.edu.unifan.produtos.exception.ValidacaoException;
import br.edu.unifan.produtos.model.Produto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static br.edu.unifan.produtos.ProdutoFixture.HOJE;
import static br.edu.unifan.produtos.ProdutoFixture.produtoValido;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProdutoValidadorTest {

    private static List<String> errosDe(Produto produto) {
        ValidacaoException ex = assertThrows(ValidacaoException.class,
                () -> ProdutoValidador.validar(produto, HOJE));
        return ex.getErros();
    }

    private static void assertErroContem(Produto produto, String trecho) {
        assertTrue(errosDe(produto).stream().anyMatch(e -> e.contains(trecho)),
                "Esperava um erro contendo \"" + trecho + "\" mas veio: " + errosDe(produto));
    }

    @Test
    void aceitaProdutoValido() {
        assertDoesNotThrow(() -> ProdutoValidador.validar(produtoValido(), HOJE));
    }

    @Test
    void rejeitaProdutoNulo() {
        assertThrows(ValidacaoException.class, () -> ProdutoValidador.validar(null, HOJE));
    }

    @Test
    void rejeitaCodigoComTamanhoErrado() {
        Produto p = produtoValido();
        p.setCodigo("ABC123");
        assertErroContem(p, "Código");
    }

    @Test
    void rejeitaCodigoComCaracteresEspeciais() {
        Produto p = produtoValido();
        p.setCodigo("PRD-0001");
        assertErroContem(p, "Código");
    }

    @Test
    void rejeitaNomeVazio() {
        Produto p = produtoValido();
        p.setNome("   ");
        assertErroContem(p, "Nome");
    }

    @Test
    void rejeitaNomeMuitoCurto() {
        Produto p = produtoValido();
        p.setNome("ab");
        assertErroContem(p, "Nome");
    }

    @Test
    void rejeitaFabricacaoNoFuturo() {
        Produto p = produtoValido();
        p.setDataFabricacao(HOJE.plusDays(1));
        p.setDataValidade(HOJE.plusYears(1));
        assertErroContem(p, "fabricação não pode ser posterior");
    }

    @Test
    void aceitaFabricacaoHoje() {
        Produto p = produtoValido();
        p.setDataFabricacao(HOJE);
        assertDoesNotThrow(() -> ProdutoValidador.validar(p, HOJE));
    }

    @Test
    void rejeitaValidadeAnteriorAFabricacao() {
        Produto p = produtoValido();
        p.setDataFabricacao(LocalDate.of(2026, 5, 10));
        p.setDataValidade(LocalDate.of(2026, 5, 9));
        assertErroContem(p, "validade não pode ser anterior");
    }

    @Test
    void rejeitaDatasAusentes() {
        Produto p = produtoValido();
        p.setDataFabricacao(null);
        p.setDataValidade(null);
        List<String> erros = errosDe(p);
        assertTrue(erros.stream().anyMatch(e -> e.contains("fabricação é obrigatória")));
        assertTrue(erros.stream().anyMatch(e -> e.contains("validade é obrigatória")));
    }

    @Test
    void rejeitaPrecoDeCompraZeroOuNegativo() {
        Produto p = produtoValido();
        p.setPrecoCompra(BigDecimal.ZERO);
        assertErroContem(p, "Preço de compra");
        p.setPrecoCompra(new BigDecimal("-1"));
        assertErroContem(p, "Preço de compra");
    }

    @Test
    void rejeitaPrecoDeVendaNaoMaiorQueOPrecoDeCompra() {
        Produto p = produtoValido();
        p.setPrecoCompra(new BigDecimal("5.00"));
        p.setPrecoVenda(new BigDecimal("5.00"));
        assertErroContem(p, "maior que o preço de compra");
        p.setPrecoVenda(new BigDecimal("4.99"));
        assertErroContem(p, "maior que o preço de compra");
    }

    @Test
    void rejeitaEstoqueNegativoMasAceitaZero() {
        Produto p = produtoValido();
        p.setQuantidadeEstoque(-1);
        assertErroContem(p, "estoque");
        p.setQuantidadeEstoque(0);
        assertDoesNotThrow(() -> ProdutoValidador.validar(p, HOJE));
    }

    @Test
    void rejeitaProdutoSemCategoria() {
        Produto p = produtoValido();
        p.setCategoria(null);
        assertErroContem(p, "Categoria");
    }

    @Test
    void informaTodosOsErrosDeUmaVez() {
        Produto p = produtoValido();
        p.setCodigo("X");
        p.setNome("");
        p.setQuantidadeEstoque(-5);
        assertTrue(errosDe(p).size() >= 3);
    }
}

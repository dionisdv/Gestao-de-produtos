package br.edu.unifan.produtos;

import br.edu.unifan.produtos.model.Categoria;
import br.edu.unifan.produtos.model.Produto;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/** Dados de apoio compartilhados pelos testes. */
public final class ProdutoFixture {

    /** Relógio fixo: "hoje" nos testes é sempre 23/10/2026. */
    public static final Clock RELOGIO = Clock.fixed(
            Instant.parse("2026-10-23T15:00:00Z"), ZoneId.of("America/Sao_Paulo"));
    public static final LocalDate HOJE = LocalDate.of(2026, 10, 23);

    public static final Categoria LATICINIOS = new Categoria(1, "Laticínios", "Leite e queijos", "Frios e Laticínios");
    public static final Categoria BEBIDAS = new Categoria(2, "Bebidas", "Sucos e refrigerantes", "Mercearia");
    public static final Categoria MERCEARIA = new Categoria(6, "Mercearia", "Arroz e feijão", "Mercearia");

    private ProdutoFixture() {
    }

    public static Produto produtoValido() {
        return produto("PRD00001", LATICINIOS, LocalDate.of(2027, 1, 1), "4.00", "5.00", 20);
    }

    public static Produto produto(String codigo, Categoria categoria, LocalDate validade,
                                  String compra, String venda, int estoque) {
        return new Produto(codigo, "Produto " + codigo, "Descrição de " + codigo,
                LocalDate.of(2026, 1, 1), validade,
                new BigDecimal(compra), new BigDecimal(venda), estoque, categoria);
    }
}

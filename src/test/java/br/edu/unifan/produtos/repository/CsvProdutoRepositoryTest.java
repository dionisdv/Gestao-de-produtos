package br.edu.unifan.produtos.repository;

import br.edu.unifan.produtos.exception.PersistenciaException;
import br.edu.unifan.produtos.model.Produto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static br.edu.unifan.produtos.ProdutoFixture.BEBIDAS;
import static br.edu.unifan.produtos.ProdutoFixture.LATICINIOS;
import static br.edu.unifan.produtos.ProdutoFixture.produto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvProdutoRepositoryTest {

    @TempDir
    Path pasta;

    @Test
    void arquivoInexistenteResultaEmListaVazia() throws PersistenciaException {
        CsvProdutoRepository repo = new CsvProdutoRepository(pasta.resolve("nao-existe.csv"));
        assertTrue(repo.carregar().isEmpty());
    }

    @Test
    void salvaECarregaOsMesmosDados() throws PersistenciaException {
        CsvProdutoRepository repo = new CsvProdutoRepository(pasta.resolve("dados").resolve("produtos.csv"));
        Produto a = produto("PRD00001", LATICINIOS, LocalDate.of(2026, 12, 1), "4.20", "5.99", 12);
        Produto b = produto("PRD00002", BEBIDAS, LocalDate.of(2027, 3, 1), "5.50", "8.99", 0);

        repo.salvar(List.of(a, b));
        List<Produto> lidos = repo.carregar();

        assertEquals(2, lidos.size());
        Produto lido = lidos.get(0);
        assertEquals("PRD00001", lido.getCodigo());
        assertEquals(a.getNome(), lido.getNome());
        assertEquals(a.getDescricao(), lido.getDescricao());
        assertEquals(a.getDataFabricacao(), lido.getDataFabricacao());
        assertEquals(a.getDataValidade(), lido.getDataValidade());
        assertEquals(0, a.getPrecoCompra().compareTo(lido.getPrecoCompra()));
        assertEquals(0, a.getPrecoVenda().compareTo(lido.getPrecoVenda()));
        assertEquals(12, lido.getQuantidadeEstoque());
        assertEquals(1, lido.getCategoria().getId());
        assertEquals("Laticínios", lido.getCategoria().getNome());
        assertEquals("Frios e Laticínios", lido.getCategoria().getSetor());
        assertEquals("PRD00002", lidos.get(1).getCodigo());
    }

    @Test
    void arquivoUsaPontoEVirgulaEPossuiCabecalho() throws Exception {
        Path arquivo = pasta.resolve("produtos.csv");
        new CsvProdutoRepository(arquivo).salvar(List.of(
                produto("PRD00001", LATICINIOS, LocalDate.of(2026, 12, 1), "4.20", "5.99", 12)));

        List<String> linhas = Files.readAllLines(arquivo, StandardCharsets.UTF_8);
        assertEquals(2, linhas.size());
        assertTrue(linhas.get(0).startsWith("codigo;nome;descricao;"));
        assertEquals(12, linhas.get(1).split(";", -1).length);
    }

    @Test
    void ponto_e_virgula_digitado_no_texto_nao_quebra_o_formato() throws PersistenciaException {
        CsvProdutoRepository repo = new CsvProdutoRepository(pasta.resolve("produtos.csv"));
        Produto p = produto("PRD00001", LATICINIOS, LocalDate.of(2026, 12, 1), "4.20", "5.99", 12);
        p.setNome("Leite; integral");

        repo.salvar(List.of(p));

        List<Produto> lidos = repo.carregar();
        assertEquals(1, lidos.size());
        assertEquals("Leite, integral", lidos.get(0).getNome());
    }

    @Test
    void linhaInvalidaGeraPersistenciaExceptionIndicandoALinha() throws IOException {
        Path arquivo = pasta.resolve("produtos.csv");
        Files.writeString(arquivo, CsvProdutoRepository.CABECALHO + "\nlinha;quebrada\n", StandardCharsets.UTF_8);

        PersistenciaException ex = assertThrows(PersistenciaException.class,
                () -> new CsvProdutoRepository(arquivo).carregar());
        assertTrue(ex.getMessage().contains("Linha 2"), ex.getMessage());
    }

    @Test
    void dataInvalidaGeraPersistenciaException() throws IOException {
        Path arquivo = pasta.resolve("produtos.csv");
        Files.writeString(arquivo, CsvProdutoRepository.CABECALHO
                + "\nPRD00001;Leite;desc;31/12/2026;2027-01-01;4.20;5.99;10;1;Laticínios;d;Frios\n",
                StandardCharsets.UTF_8);

        assertThrows(PersistenciaException.class, () -> new CsvProdutoRepository(arquivo).carregar());
    }

    @Test
    void aceitaArquivoComBomEPrecoComVirgula() throws Exception {
        Path arquivo = pasta.resolve("produtos.csv");
        Files.writeString(arquivo, "﻿" + CsvProdutoRepository.CABECALHO
                + "\nPRD00001;Leite;desc;2026-09-01;2027-01-01;4,20;5,99;10;1;Laticínios;d;Frios\n\n",
                StandardCharsets.UTF_8);

        List<Produto> lidos = new CsvProdutoRepository(arquivo).carregar();

        assertEquals(1, lidos.size());
        assertEquals(0, new java.math.BigDecimal("4.20").compareTo(lidos.get(0).getPrecoCompra()));
    }

    @Test
    void leArquivoSalvoEmWindows1252() throws Exception {
        Path arquivo = pasta.resolve("produtos.csv");
        String conteudo = CsvProdutoRepository.CABECALHO
                + "\nPRD00001;Feijão;desc;2026-09-01;2027-01-01;4.20;5.99;10;1;Laticínios;d;Frios\n";
        Files.write(arquivo, conteudo.getBytes(StandardCharsets.ISO_8859_1));

        List<Produto> lidos = new CsvProdutoRepository(arquivo).carregar();

        assertEquals("Feijão", lidos.get(0).getNome());
    }
}

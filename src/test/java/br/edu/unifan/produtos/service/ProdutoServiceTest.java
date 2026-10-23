package br.edu.unifan.produtos.service;

import br.edu.unifan.produtos.exception.PersistenciaException;
import br.edu.unifan.produtos.exception.ProdutoNaoEncontradoException;
import br.edu.unifan.produtos.exception.ValidacaoException;
import br.edu.unifan.produtos.model.Produto;
import br.edu.unifan.produtos.repository.CsvProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static br.edu.unifan.produtos.ProdutoFixture.LATICINIOS;
import static br.edu.unifan.produtos.ProdutoFixture.RELOGIO;
import static br.edu.unifan.produtos.ProdutoFixture.produto;
import static br.edu.unifan.produtos.ProdutoFixture.produtoValido;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProdutoServiceTest {

    @TempDir
    Path pasta;

    private Path arquivo;
    private ProdutoService service;

    @BeforeEach
    void preparar() throws PersistenciaException {
        arquivo = pasta.resolve("produtos.csv");
        service = novoService();
    }

    private ProdutoService novoService() throws PersistenciaException {
        return new ProdutoService(new CsvProdutoRepository(arquivo), RELOGIO);
    }

    @Test
    void cadastraEPersisteNoArquivo() throws Exception {
        service.cadastrar(produtoValido());

        assertEquals(1, service.listar().size());
        ProdutoService recarregado = novoService(); // simula reiniciar o programa
        assertEquals(1, recarregado.listar().size());
        assertTrue(recarregado.buscarPorCodigo("PRD00001").isPresent());
    }

    @Test
    void naoPermiteCodigoDuplicadoIgnorandoMaiusculas() throws Exception {
        service.cadastrar(produtoValido());
        Produto repetido = produtoValido();
        repetido.setCodigo("prd00001");

        ValidacaoException ex = assertThrows(ValidacaoException.class, () -> service.cadastrar(repetido));
        assertTrue(ex.getMessage().contains("Já existe"));
        assertEquals(1, service.listar().size());
    }

    @Test
    void produtoInvalidoNaoEhCadastradoNemGravado() {
        Produto invalido = produtoValido();
        invalido.setNome("");

        assertThrows(ValidacaoException.class, () -> service.cadastrar(invalido));
        assertTrue(service.listar().isEmpty());
        assertFalse(Files.exists(arquivo));
    }

    @Test
    void validaFabricacaoUsandoORelogioInjetado() {
        Produto futuro = produtoValido();
        futuro.setDataFabricacao(LocalDate.of(2026, 10, 24)); // um dia depois do "hoje" fixo
        futuro.setDataValidade(LocalDate.of(2027, 10, 24));

        assertThrows(ValidacaoException.class, () -> service.cadastrar(futuro));
    }

    @Test
    void buscaPorCodigoIgnoraMaiusculasEEspacos() throws Exception {
        service.cadastrar(produtoValido());

        assertTrue(service.buscarPorCodigo(" prd00001 ").isPresent());
        assertFalse(service.buscarPorCodigo("PRD99999").isPresent());
        assertFalse(service.buscarPorCodigo(null).isPresent());
    }

    @Test
    void excluiEPersisteNoArquivo() throws Exception {
        service.cadastrar(produtoValido());
        service.cadastrar(produto("PRD00002", LATICINIOS, LocalDate.of(2027, 1, 1), "1.00", "2.00", 5));

        service.excluir("PRD00001");

        assertEquals(1, service.listar().size());
        ProdutoService recarregado = novoService();
        assertEquals(1, recarregado.listar().size());
        assertFalse(recarregado.buscarPorCodigo("PRD00001").isPresent());
    }

    @Test
    void excluirCodigoInexistenteLancaExcecao() {
        assertThrows(ProdutoNaoEncontradoException.class, () -> service.excluir("PRD99999"));
    }

    @Test
    void listarDevolveCopiaImutavel() throws Exception {
        service.cadastrar(produtoValido());

        assertThrows(UnsupportedOperationException.class, () -> service.listar().clear());
    }

    @Test
    void categoriasIncluemCatalogoPadraoSemRepetirIds() throws Exception {
        service.cadastrar(produtoValido());

        long distintas = service.categorias().stream().map(c -> c.getId()).distinct().count();
        assertEquals(service.categorias().size(), distintas);
        assertTrue(service.categorias().size() >= CategoriaCatalogo.padrao().size());
    }
}

package br.edu.unifan.produtos.service;

import br.edu.unifan.produtos.exception.PersistenciaException;
import br.edu.unifan.produtos.exception.ProdutoNaoEncontradoException;
import br.edu.unifan.produtos.exception.ValidacaoException;
import br.edu.unifan.produtos.model.Categoria;
import br.edu.unifan.produtos.model.Produto;
import br.edu.unifan.produtos.repository.CsvProdutoRepository;
import br.edu.unifan.produtos.validation.ProdutoValidador;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Regras de negócio do cadastro: cadastrar, excluir, consultar e listar.
 * Mantém os produtos em memória e grava o CSV após cada operação que altera os dados.
 */
public class ProdutoService {

    private final CsvProdutoRepository repository;
    private final Clock clock;
    /** Chave: código em maiúsculas (a unicidade ignora maiúsculas/minúsculas). */
    private final Map<String, Produto> produtos = new LinkedHashMap<>();

    public ProdutoService(CsvProdutoRepository repository) throws PersistenciaException {
        this(repository, Clock.systemDefaultZone());
    }

    public ProdutoService(CsvProdutoRepository repository, Clock clock) throws PersistenciaException {
        this.repository = repository;
        this.clock = clock;
        for (Produto p : repository.carregar()) {
            produtos.put(chave(p.getCodigo()), p);
        }
    }

    public void cadastrar(Produto produto) throws ValidacaoException, PersistenciaException {
        ProdutoValidador.validar(produto, LocalDate.now(clock));

        String chave = chave(produto.getCodigo());
        if (produtos.containsKey(chave)) {
            throw new ValidacaoException("Já existe um produto cadastrado com o código " + produto.getCodigo() + ".");
        }

        Map<String, Produto> antes = new LinkedHashMap<>(produtos);
        produtos.put(chave, produto);
        persistirOuReverter(antes);
    }

    public void excluir(String codigo) throws ProdutoNaoEncontradoException, PersistenciaException {
        String chave = chave(codigo);
        if (!produtos.containsKey(chave)) {
            throw new ProdutoNaoEncontradoException(codigo);
        }

        Map<String, Produto> antes = new LinkedHashMap<>(produtos);
        produtos.remove(chave);
        persistirOuReverter(antes);
    }

    public Optional<Produto> buscarPorCodigo(String codigo) {
        return Optional.ofNullable(produtos.get(chave(codigo)));
    }

    /** Cópia da lista atual: alterá-la não afeta o cadastro. */
    public List<Produto> listar() {
        return List.copyOf(produtos.values());
    }

    /** Categorias do catálogo padrão somadas às que já aparecem nos produtos carregados, por nome. */
    public List<Categoria> categorias() {
        Map<Integer, Categoria> porId = new LinkedHashMap<>();
        CategoriaCatalogo.padrao().forEach(c -> porId.put(c.getId(), c));
        produtos.values().stream()
                .map(Produto::getCategoria)
                .forEach(c -> porId.putIfAbsent(c.getId(), c));
        return porId.values().stream()
                .sorted(Comparator.comparing(Categoria::getNome, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private void persistirOuReverter(Map<String, Produto> estadoAnterior) throws PersistenciaException {
        try {
            repository.salvar(produtos.values());
        } catch (PersistenciaException e) {
            produtos.clear();
            produtos.putAll(estadoAnterior);
            throw e;
        }
    }

    private static String chave(String codigo) {
        return codigo == null ? "" : codigo.trim().toUpperCase();
    }
}

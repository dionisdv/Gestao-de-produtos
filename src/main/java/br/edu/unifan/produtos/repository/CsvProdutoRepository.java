package br.edu.unifan.produtos.repository;

import br.edu.unifan.produtos.exception.PersistenciaException;
import br.edu.unifan.produtos.model.Categoria;
import br.edu.unifan.produtos.model.Produto;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Lê e grava produtos em um arquivo CSV separado por ponto-e-vírgula (;).
 *
 * <p>Formato (uma linha por produto, com cabeçalho na primeira linha):
 * <pre>
 * codigo;nome;descricao;dataFabricacao;dataValidade;precoCompra;precoVenda;quantidadeEstoque;
 * categoriaId;categoriaNome;categoriaDescricao;categoriaSetor
 * </pre>
 * Datas no formato ISO (aaaa-mm-dd) e preços com ponto decimal.
 */
public class CsvProdutoRepository {

    static final String SEPARADOR = ";";
    static final String CABECALHO = String.join(SEPARADOR,
            "codigo", "nome", "descricao", "dataFabricacao", "dataValidade",
            "precoCompra", "precoVenda", "quantidadeEstoque",
            "categoriaId", "categoriaNome", "categoriaDescricao", "categoriaSetor");
    private static final int TOTAL_CAMPOS = 12;

    private final Path arquivo;

    public CsvProdutoRepository(Path arquivo) {
        this.arquivo = arquivo;
    }

    public Path getArquivo() {
        return arquivo;
    }

    /** Carrega todos os produtos do arquivo. Se o arquivo ainda não existe, devolve lista vazia. */
    public List<Produto> carregar() throws PersistenciaException {
        if (!Files.exists(arquivo)) {
            return new ArrayList<>();
        }

        List<String> linhas = lerLinhas();
        List<Produto> produtos = new ArrayList<>();
        for (int i = 0; i < linhas.size(); i++) {
            String linha = linhas.get(i);
            if (i == 0) {
                linha = removerBom(linha);
            }
            if (linha.isBlank()) {
                continue;
            }
            if (i == 0 && linha.startsWith("codigo" + SEPARADOR)) {
                continue; // cabeçalho
            }
            try {
                produtos.add(paraProduto(linha));
            } catch (RuntimeException e) {
                throw new PersistenciaException("Linha " + (i + 1) + " inválida no arquivo "
                        + arquivo.getFileName() + ": " + e.getMessage(), e);
            }
        }
        return produtos;
    }

    /** Regrava o arquivo inteiro com os produtos informados (escreve em arquivo temporário e substitui). */
    public void salvar(Collection<Produto> produtos) throws PersistenciaException {
        List<String> linhas = new ArrayList<>();
        linhas.add(CABECALHO);
        produtos.stream().map(this::paraLinha).forEach(linhas::add);

        try {
            Path absoluto = arquivo.toAbsolutePath();
            if (absoluto.getParent() != null) {
                Files.createDirectories(absoluto.getParent());
            }
            Path temporario = absoluto.resolveSibling(absoluto.getFileName() + ".tmp");
            Files.write(temporario, linhas, StandardCharsets.UTF_8);
            Files.move(temporario, absoluto, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new PersistenciaException("Não foi possível gravar o arquivo " + arquivo.getFileName()
                    + ". Se ele estiver aberto em outro programa (como o Excel), feche-o e tente de novo. "
                    + "Detalhe: " + e.getMessage(), e);
        }
    }

    private List<String> lerLinhas() throws PersistenciaException {
        try {
            return Files.readAllLines(arquivo, StandardCharsets.UTF_8);
        } catch (CharacterCodingException e) {
            // Arquivo salvo pelo Excel em ANSI (Windows-1252): tenta ISO-8859-1.
            try {
                return Files.readAllLines(arquivo, StandardCharsets.ISO_8859_1);
            } catch (IOException e2) {
                throw new PersistenciaException("Não foi possível ler o arquivo " + arquivo.getFileName()
                        + ": " + e2.getMessage(), e2);
            }
        } catch (IOException e) {
            throw new PersistenciaException("Não foi possível ler o arquivo " + arquivo.getFileName()
                    + ": " + e.getMessage(), e);
        }
    }

    private static String removerBom(String linha) {
        return linha.startsWith("﻿") ? linha.substring(1) : linha;
    }

    private Produto paraProduto(String linha) {
        String[] c = linha.split(SEPARADOR, -1);
        if (c.length != TOTAL_CAMPOS) {
            throw new IllegalArgumentException("esperados " + TOTAL_CAMPOS + " campos separados por ';' mas há " + c.length);
        }
        Categoria categoria = new Categoria(
                Integer.parseInt(c[8].trim()), c[9].trim(), c[10].trim(), c[11].trim());
        return new Produto(
                c[0].trim(),
                c[1].trim(),
                c[2].trim(),
                LocalDate.parse(c[3].trim()),
                LocalDate.parse(c[4].trim()),
                decimal(c[5]),
                decimal(c[6]),
                Integer.parseInt(c[7].trim()),
                categoria);
    }

    private static BigDecimal decimal(String texto) {
        return new BigDecimal(texto.trim().replace(',', '.'));
    }

    private String paraLinha(Produto p) {
        Categoria c = p.getCategoria();
        return String.join(SEPARADOR,
                limpar(p.getCodigo()),
                limpar(p.getNome()),
                limpar(p.getDescricao()),
                String.valueOf(p.getDataFabricacao()),
                String.valueOf(p.getDataValidade()),
                p.getPrecoCompra().toPlainString(),
                p.getPrecoVenda().toPlainString(),
                String.valueOf(p.getQuantidadeEstoque()),
                String.valueOf(c.getId()),
                limpar(c.getNome()),
                limpar(c.getDescricao()),
                limpar(c.getSetor()));
    }

    /** Impede que textos digitados pelo usuário quebrem o formato (separador e quebras de linha). */
    private static String limpar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace(SEPARADOR, ",").replaceAll("[\\r\\n]+", " ").trim();
    }
}

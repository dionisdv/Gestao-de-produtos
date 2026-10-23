package br.edu.unifan.produtos.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Produto da loja/supermercado. O código é o identificador único e a categoria é uma composição.
 */
public class Produto {

    private String codigo;
    private String nome;
    private String descricao;
    private LocalDate dataFabricacao;
    private LocalDate dataValidade;
    private BigDecimal precoCompra;
    private BigDecimal precoVenda;
    private int quantidadeEstoque;
    private Categoria categoria;

    public Produto(String codigo, String nome, String descricao, LocalDate dataFabricacao,
                   LocalDate dataValidade, BigDecimal precoCompra, BigDecimal precoVenda,
                   int quantidadeEstoque, Categoria categoria) {
        this.codigo = codigo;
        this.nome = nome;
        this.descricao = descricao;
        this.dataFabricacao = dataFabricacao;
        this.dataValidade = dataValidade;
        this.precoCompra = precoCompra;
        this.precoVenda = precoVenda;
        this.quantidadeEstoque = quantidadeEstoque;
        this.categoria = categoria;
    }

    /**
     * Margem de lucro sobre o preço de venda, em percentual: (venda - compra) / venda * 100.
     * Retorna zero quando os preços ainda não são válidos.
     */
    public BigDecimal getMargemLucroPercentual() {
        if (precoCompra == null || precoVenda == null || precoVenda.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        return precoVenda.subtract(precoCompra)
                .multiply(BigDecimal.valueOf(100))
                .divide(precoVenda, 2, RoundingMode.HALF_UP);
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDate getDataFabricacao() {
        return dataFabricacao;
    }

    public void setDataFabricacao(LocalDate dataFabricacao) {
        this.dataFabricacao = dataFabricacao;
    }

    public LocalDate getDataValidade() {
        return dataValidade;
    }

    public void setDataValidade(LocalDate dataValidade) {
        this.dataValidade = dataValidade;
    }

    public BigDecimal getPrecoCompra() {
        return precoCompra;
    }

    public void setPrecoCompra(BigDecimal precoCompra) {
        this.precoCompra = precoCompra;
    }

    public BigDecimal getPrecoVenda() {
        return precoVenda;
    }

    public void setPrecoVenda(BigDecimal precoVenda) {
        this.precoVenda = precoVenda;
    }

    public int getQuantidadeEstoque() {
        return quantidadeEstoque;
    }

    public void setQuantidadeEstoque(int quantidadeEstoque) {
        this.quantidadeEstoque = quantidadeEstoque;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    /** Dois produtos são iguais quando possuem o mesmo código (ignorando maiúsculas/minúsculas). */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Produto outro)) {
            return false;
        }
        return codigo != null && codigo.equalsIgnoreCase(outro.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo == null ? null : codigo.toUpperCase());
    }

    @Override
    public String toString() {
        return codigo + " - " + nome;
    }
}

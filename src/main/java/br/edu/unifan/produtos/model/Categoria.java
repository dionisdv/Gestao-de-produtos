package br.edu.unifan.produtos.model;

import java.util.Objects;

/**
 * Categoria de produtos (ex.: Laticínios, Bebidas). Cada categoria pertence a um setor da loja.
 */
public class Categoria {

    private int id;
    private String nome;
    private String descricao;
    private String setor;

    public Categoria(int id, String nome, String descricao, String setor) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.setor = setor;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getSetor() {
        return setor;
    }

    public void setSetor(String setor) {
        this.setor = setor;
    }

    /** Duas categorias são iguais quando possuem o mesmo id. */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Categoria outra)) {
            return false;
        }
        return id == outra.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /** Usado pelo ComboBox da interface para exibir a categoria. */
    @Override
    public String toString() {
        return nome;
    }
}

package br.edu.unifan.produtos.service;

import br.edu.unifan.produtos.model.Categoria;

import java.util.List;

/** Categorias padrão oferecidas no formulário de cadastro. */
public final class CategoriaCatalogo {

    private CategoriaCatalogo() {
    }

    public static List<Categoria> padrao() {
        return List.of(
                new Categoria(1, "Laticínios", "Leite, queijos e iogurtes", "Frios e Laticínios"),
                new Categoria(2, "Bebidas", "Refrigerantes, sucos e águas", "Mercearia"),
                new Categoria(3, "Limpeza", "Produtos de limpeza doméstica", "Casa"),
                new Categoria(4, "Hortifruti", "Frutas, legumes e verduras", "Perecíveis"),
                new Categoria(5, "Higiene Pessoal", "Sabonetes, shampoos e cremes dentais", "Casa"),
                new Categoria(6, "Mercearia", "Arroz, feijão, massas e grãos", "Mercearia"));
    }
}

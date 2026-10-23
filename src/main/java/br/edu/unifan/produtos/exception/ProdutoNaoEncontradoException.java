package br.edu.unifan.produtos.exception;

/** Lançada ao tentar operar sobre um código de produto que não existe. */
public class ProdutoNaoEncontradoException extends Exception {

    private static final long serialVersionUID = 1L;

    public ProdutoNaoEncontradoException(String codigo) {
        super("Não existe produto cadastrado com o código " + codigo + ".");
    }
}

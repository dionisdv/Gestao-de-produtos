package br.edu.unifan.produtos.exception;

import java.util.List;

/**
 * Lançada quando os dados informados não respeitam as regras de negócio.
 * Guarda todas as mensagens de erro de uma só vez, para que o usuário corrija tudo em uma tentativa.
 */
@SuppressWarnings("serial")
public class ValidacaoException extends Exception {

    private static final long serialVersionUID = 1L;

    private final List<String> erros;

    public ValidacaoException(String erro) {
        this(List.of(erro));
    }

    public ValidacaoException(List<String> erros) {
        super(String.join("\n", erros));
        this.erros = List.copyOf(erros);
    }

    public List<String> getErros() {
        return erros;
    }
}

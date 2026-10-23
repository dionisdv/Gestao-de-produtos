package br.edu.unifan.produtos.exception;

/** Lançada quando há falha ao ler ou gravar o arquivo CSV. */
public class PersistenciaException extends Exception {

    private static final long serialVersionUID = 1L;

    public PersistenciaException(String mensagem) {
        super(mensagem);
    }

    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}

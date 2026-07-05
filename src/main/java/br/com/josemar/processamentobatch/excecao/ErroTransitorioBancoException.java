package br.com.josemar.processamentobatch.excecao;

/**
 * Excecao usada para demonstrar retry em falhas temporarias no faturamento.
 */
public class ErroTransitorioBancoException extends RuntimeException {

    public ErroTransitorioBancoException(String mensagem) {
        super(mensagem);
    }
}

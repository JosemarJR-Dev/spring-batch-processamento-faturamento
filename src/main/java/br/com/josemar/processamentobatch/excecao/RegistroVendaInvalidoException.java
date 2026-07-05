package br.com.josemar.processamentobatch.excecao;

import br.com.josemar.processamentobatch.dto.RegistroVendaCsv;

/**
 * Excecao usada para sinalizar uma venda invalida que deve ser pulada pelo Spring Batch.
 */
public class RegistroVendaInvalidoException extends RuntimeException {

    private final RegistroVendaCsv registro;

    public RegistroVendaInvalidoException(RegistroVendaCsv registro, String mensagem) {
        super(mensagem);
        this.registro = registro;
    }

    public RegistroVendaCsv getRegistro() {
        return registro;
    }
}

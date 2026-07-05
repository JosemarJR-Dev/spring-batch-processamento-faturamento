package br.com.josemar.processamentobatch.excecao;

/**
 * Excecao usada quando uma venda referencia produto inexistente no cadastro.
 */
public class ProdutoNaoEncontradoException extends RegistroVendaInvalidoException {

    public ProdutoNaoEncontradoException(br.com.josemar.processamentobatch.dto.RegistroVendaCsv registro, String mensagem) {
        super(registro, mensagem);
    }
}

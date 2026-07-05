package br.com.josemar.processamentobatch.processador;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import br.com.josemar.processamentobatch.dto.RegistroVendaCsv;
import br.com.josemar.processamentobatch.excecao.RegistroVendaInvalidoException;
import br.com.josemar.processamentobatch.repositorio.ProdutoRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.test.MetaDataInstanceFactory;

/**
 * Testa as regras unitarias do processador de validacao de vendas.
 */
@ExtendWith(MockitoExtension.class)
class ProcessadorValidacaoVendaTest {

    @Mock
    private ProdutoRepositorio produtoRepositorio;

    private ProcessadorValidacaoVenda processador;

    @BeforeEach
    void configurar() {
        processador = new ProcessadorValidacaoVenda(produtoRepositorio);
        processador.beforeStep(MetaDataInstanceFactory.createStepExecution());
    }

    @Test
    void deveProcessarRegistroValido() {
        when(produtoRepositorio.existePorCodigo("PROD-001")).thenReturn(true);

        var venda = processador.process(registroValido());

        assertThat(venda.getIdVenda()).isEqualTo("1001");
        assertThat(venda.getCodigoCliente()).isEqualTo("CLI-001");
        assertThat(venda.getValorTotal()).isEqualByComparingTo("199.80");
    }

    @Test
    void deveRejeitarClienteVazio() {
        RegistroVendaCsv registro = registroValido();
        registro.setCodigoCliente("");

        assertThatThrownBy(() -> processador.process(registro))
                .isInstanceOf(RegistroVendaInvalidoException.class)
                .hasMessageContaining("codigo_cliente");
    }

    @Test
    void deveRejeitarQuantidadeInvalida() {
        RegistroVendaCsv registro = registroValido();
        registro.setQuantidade("-2");

        assertThatThrownBy(() -> processador.process(registro))
                .isInstanceOf(RegistroVendaInvalidoException.class)
                .hasMessageContaining("quantidade");
    }

    private RegistroVendaCsv registroValido() {
        return new RegistroVendaCsv(
                2,
                "1001,CLI-001,PROD-001,2,99.90,2026-07-05,CARTAO",
                "1001",
                "CLI-001",
                "PROD-001",
                "2",
                "99.90",
                "2026-07-05",
                "CARTAO");
    }
}

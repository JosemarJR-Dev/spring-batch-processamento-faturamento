package br.com.josemar.processamentobatch.processador;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import br.com.josemar.processamentobatch.dominio.FormaPagamento;
import br.com.josemar.processamentobatch.dominio.StatusVenda;
import br.com.josemar.processamentobatch.dominio.VendaStaging;
import br.com.josemar.processamentobatch.dto.RegistroVendaCsv;
import br.com.josemar.processamentobatch.excecao.ProdutoNaoEncontradoException;
import br.com.josemar.processamentobatch.excecao.RegistroVendaInvalidoException;
import br.com.josemar.processamentobatch.repositorio.ProdutoRepositorio;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Valida uma linha do CSV e converte registros aceitos em vendas de staging.
 */
@Component
public class ProcessadorValidacaoVenda implements ItemProcessor<RegistroVendaCsv, VendaStaging>, StepExecutionListener {

    private final ProdutoRepositorio produtoRepositorio;
    private Long jobExecutionId;

    public ProcessadorValidacaoVenda(ProdutoRepositorio produtoRepositorio) {
        this.produtoRepositorio = produtoRepositorio;
    }

    /**
     * Captura o identificador da execucao para rastrear a carga importada.
     */
    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.jobExecutionId = stepExecution.getJobExecutionId();
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return stepExecution.getExitStatus();
    }

    /**
     * Aplica as regras de validacao e monta a entidade usada pelos writers JDBC.
     */
    @Override
    public VendaStaging process(RegistroVendaCsv item) {
        validarObrigatorio(item.getIdVenda(), item, "id_venda e obrigatorio");
        validarObrigatorio(item.getCodigoCliente(), item, "codigo_cliente e obrigatorio");
        validarObrigatorio(item.getCodigoProduto(), item, "codigo_produto e obrigatorio");

        Integer quantidade = converterQuantidade(item);
        BigDecimal valorUnitario = converterValorUnitario(item);
        LocalDate dataVenda = converterDataVenda(item);
        FormaPagamento formaPagamento = converterFormaPagamento(item);

        if (!produtoRepositorio.existePorCodigo(item.getCodigoProduto())) {
            throw new ProdutoNaoEncontradoException(item, "codigo_produto nao encontrado: " + item.getCodigoProduto());
        }

        return VendaStaging.builder()
                .idVenda(item.getIdVenda())
                .codigoCliente(item.getCodigoCliente())
                .codigoProduto(item.getCodigoProduto())
                .quantidade(quantidade)
                .valorUnitario(valorUnitario)
                .valorTotal(valorUnitario.multiply(BigDecimal.valueOf(quantidade)))
                .dataVenda(dataVenda)
                .formaPagamento(formaPagamento)
                .status(StatusVenda.IMPORTADA)
                .dataImportacao(LocalDateTime.now())
                .jobExecutionId(jobExecutionId)
                .build();
    }

    private void validarObrigatorio(String valor, RegistroVendaCsv item, String mensagem) {
        if (!StringUtils.hasText(valor)) {
            throw new RegistroVendaInvalidoException(item, mensagem);
        }
    }

    private Integer converterQuantidade(RegistroVendaCsv item) {
        try {
            Integer quantidade = Integer.valueOf(item.getQuantidade());
            if (quantidade <= 0) {
                throw new RegistroVendaInvalidoException(item, "quantidade deve ser maior que zero");
            }
            return quantidade;
        } catch (NumberFormatException ex) {
            throw new RegistroVendaInvalidoException(item, "quantidade invalida");
        }
    }

    private BigDecimal converterValorUnitario(RegistroVendaCsv item) {
        try {
            BigDecimal valor = new BigDecimal(item.getValorUnitario());
            if (valor.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RegistroVendaInvalidoException(item, "valor_unitario deve ser maior que zero");
            }
            return valor;
        } catch (NumberFormatException ex) {
            throw new RegistroVendaInvalidoException(item, "valor_unitario invalido");
        }
    }

    private LocalDate converterDataVenda(RegistroVendaCsv item) {
        try {
            return LocalDate.parse(item.getDataVenda());
        } catch (RuntimeException ex) {
            throw new RegistroVendaInvalidoException(item, "data_venda invalida");
        }
    }

    private FormaPagamento converterFormaPagamento(RegistroVendaCsv item) {
        try {
            return FormaPagamento.valueOf(item.getFormaPagamento());
        } catch (RuntimeException ex) {
            throw new RegistroVendaInvalidoException(item, "forma_pagamento invalida");
        }
    }
}

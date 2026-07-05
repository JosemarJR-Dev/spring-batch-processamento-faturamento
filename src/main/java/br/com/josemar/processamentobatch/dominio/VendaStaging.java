package br.com.josemar.processamentobatch.dominio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representa uma venda validada e pronta para faturamento.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendaStaging {

    private Long id;
    private String idVenda;
    private String codigoCliente;
    private String codigoProduto;
    private Integer quantidade;
    private BigDecimal valorUnitario;
    private BigDecimal valorTotal;
    private LocalDate dataVenda;
    private FormaPagamento formaPagamento;
    private StatusVenda status;
    private LocalDateTime dataImportacao;
    private Long jobExecutionId;

    public String getFormaPagamentoNome() {
        return formaPagamento.name();
    }

    public String getStatusNome() {
        return status.name();
    }
}

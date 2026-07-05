package br.com.josemar.processamentobatch.dominio;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representa a fatura gerada a partir de uma venda valida.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Fatura {

    private Long id;
    private Long vendaStagingId;
    private String idVenda;
    private String codigoCliente;
    private String codigoProduto;
    private BigDecimal valorTotal;
    private LocalDate dataEmissao;
    private LocalDate dataVencimento;
    private Long jobExecutionId;
}

package br.com.josemar.processamentobatch.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representa uma linha bruta do CSV de vendas antes da validacao de negocio.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistroVendaCsv {

    private Integer numeroLinha;
    private String conteudoOriginal;
    private String idVenda;
    private String codigoCliente;
    private String codigoProduto;
    private String quantidade;
    private String valorUnitario;
    private String dataVenda;
    private String formaPagamento;
}

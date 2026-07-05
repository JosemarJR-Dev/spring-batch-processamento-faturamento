package br.com.josemar.processamentobatch.dominio;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representa um produto cadastrado e usado para validar as vendas importadas.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Produto {

    private String codigo;
    private String nome;
    private BigDecimal valorPadrao;
}

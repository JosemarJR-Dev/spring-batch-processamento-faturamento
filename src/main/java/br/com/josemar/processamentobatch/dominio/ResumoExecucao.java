package br.com.josemar.processamentobatch.dominio;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representa o resumo consolidado de uma execucao do job de faturamento.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResumoExecucao {

    private Long id;
    private Long jobExecutionId;
    private String nomeJob;
    private String arquivoEntrada;
    private LocalDate dataProcessamento;
    private Long totalLidos;
    private Long totalGravados;
    private Long totalRejeitados;
    private Long totalFaturas;
    private String statusFinal;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
}

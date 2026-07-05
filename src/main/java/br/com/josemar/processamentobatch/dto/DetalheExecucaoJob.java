package br.com.josemar.processamentobatch.dto;

import java.util.List;

import br.com.josemar.processamentobatch.dominio.Fatura;
import br.com.josemar.processamentobatch.dominio.RegistroRejeitado;
import br.com.josemar.processamentobatch.dominio.ResumoExecucao;

/**
 * Representa os detalhes de uma execucao para consulta manual.
 */
public record DetalheExecucaoJob(
        ResumoExecucao resumo,
        List<RegistroRejeitado> rejeicoes,
        List<Fatura> faturas) {
}

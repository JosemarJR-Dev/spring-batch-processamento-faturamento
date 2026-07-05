package br.com.josemar.processamentobatch.dto;

import java.util.Map;

/**
 * Representa a resposta basica do disparo manual do job.
 */
public record RespostaExecucaoJob(
        Long jobExecutionId,
        String status,
        String nomeJob,
        Map<String, Object> parametros) {
}

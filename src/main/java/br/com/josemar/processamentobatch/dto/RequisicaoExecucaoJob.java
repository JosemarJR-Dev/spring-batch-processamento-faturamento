package br.com.josemar.processamentobatch.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Representa os parametros enviados para disparar o job manualmente.
 */
public record RequisicaoExecucaoJob(
        @NotBlank String arquivoEntrada,
        @NotBlank String dataProcessamento) {
}

package br.com.josemar.processamentobatch.tasklet;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

/**
 * Valida existencia, conteudo e cabecalho do arquivo CSV recebido pelo job.
 */
@Component
public class ValidarArquivoEntradaTasklet implements Tasklet {

    private static final String HEADER_ESPERADO =
            "id_venda,codigo_cliente,codigo_produto,quantidade,valor_unitario,data_venda,forma_pagamento";

    /**
     * Valida os parametros principais e impede processamento de arquivo inadequado.
     */
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        String arquivoEntrada = (String) chunkContext.getStepContext().getJobParameters().get("arquivoEntrada");
        String dataProcessamento = (String) chunkContext.getStepContext().getJobParameters().get("dataProcessamento");

        if (arquivoEntrada == null || arquivoEntrada.isBlank()) {
            throw new IllegalArgumentException("JobParameter arquivoEntrada e obrigatorio");
        }
        if (dataProcessamento == null || dataProcessamento.isBlank()) {
            throw new IllegalArgumentException("JobParameter dataProcessamento e obrigatorio");
        }

        Path caminho = Path.of(arquivoEntrada);
        if (!Files.exists(caminho)) {
            throw new IllegalArgumentException("Arquivo de entrada nao encontrado: " + arquivoEntrada);
        }
        if (Files.size(caminho) == 0) {
            throw new IllegalArgumentException("Arquivo de entrada esta vazio: " + arquivoEntrada);
        }

        List<String> linhas = Files.readAllLines(caminho);
        if (linhas.isEmpty() || !HEADER_ESPERADO.equals(linhas.get(0))) {
            throw new IllegalArgumentException("Header do arquivo de entrada esta invalido");
        }

        return RepeatStatus.FINISHED;
    }
}

package br.com.josemar.processamentobatch.tasklet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.test.MetaDataInstanceFactory;

/**
 * Testa as validacoes de arquivo feitas antes da importacao.
 */
class ValidarArquivoEntradaTaskletTest {

    private final ValidarArquivoEntradaTasklet tasklet = new ValidarArquivoEntradaTasklet();

    @TempDir
    private Path tempDir;

    @Test
    void deveAceitarArquivoValido() throws Exception {
        Path arquivo = tempDir.resolve("vendas.csv");
        Files.writeString(arquivo,
                "id_venda,codigo_cliente,codigo_produto,quantidade,valor_unitario,data_venda,forma_pagamento%n"
                        .formatted()
                        + "9001,CLI-900,PROD-001,1,99.90,2026-07-05,PIX%n".formatted());

        RepeatStatus status = tasklet.execute(contribuicao(arquivo), contexto(arquivo));

        assertThat(status).isEqualTo(RepeatStatus.FINISHED);
    }

    @Test
    void deveFalharQuandoArquivoNaoExiste() {
        Path arquivo = tempDir.resolve("inexistente.csv");

        assertThatThrownBy(() -> tasklet.execute(contribuicao(arquivo), contexto(arquivo)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nao encontrado");
    }

    private StepContribution contribuicao(Path arquivo) {
        return new StepContribution(stepExecution(arquivo));
    }

    private ChunkContext contexto(Path arquivo) {
        return new ChunkContext(new StepContext(stepExecution(arquivo)));
    }

    private org.springframework.batch.core.StepExecution stepExecution(Path arquivo) {
        org.springframework.batch.core.JobParameters parametros = new JobParametersBuilder()
                .addString("arquivoEntrada", arquivo.toString())
                .addString("dataProcessamento", "2026-07-05")
                .toJobParameters();
        return MetaDataInstanceFactory.createStepExecution(parametros);
    }
}

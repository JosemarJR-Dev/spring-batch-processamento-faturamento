package br.com.josemar.processamentobatch.job;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.josemar.processamentobatch.SpringBatchProcessamentoFaturamentoApplication;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

/**
 * Testa o job completo com arquivos pequenos de entrada.
 */
@SpringBatchTest
@SpringBootTest(classes = SpringBatchProcessamentoFaturamentoApplication.class)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class JobProcessamentoFaturamentoTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void deveExecutarJobCompletoComArquivoValido() throws Exception {
        String arquivo = new ClassPathResource("arquivos/vendas-teste-valido.csv").getFile().getPath();
        var parametros = new JobParametersBuilder()
                .addString("arquivoEntrada", arquivo)
                .addString("dataProcessamento", "2026-07-06")
                .toJobParameters();

        var execucao = jobLauncherTestUtils.launchJob(parametros);

        assertThat(execucao.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(contarPorExecucao("VENDA_STAGING", execucao.getId())).isEqualTo(2);
        assertThat(contarPorExecucao("FATURA", execucao.getId())).isEqualTo(2);
        assertThat(contarPorExecucao("REGISTRO_REJEITADO", execucao.getId())).isZero();
    }

    @Test
    void deveRejeitarInvalidosEContinuarJob() throws Exception {
        String arquivo = new ClassPathResource("arquivos/vendas-teste-rejeicoes.csv").getFile().getPath();
        var parametros = new JobParametersBuilder()
                .addString("arquivoEntrada", arquivo)
                .addString("dataProcessamento", "2026-07-07")
                .toJobParameters();

        var execucao = jobLauncherTestUtils.launchJob(parametros);

        assertThat(execucao.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(contarPorExecucao("VENDA_STAGING", execucao.getId())).isEqualTo(1);
        assertThat(contarPorExecucao("FATURA", execucao.getId())).isEqualTo(1);
        assertThat(contarPorExecucao("REGISTRO_REJEITADO", execucao.getId())).isEqualTo(2);
    }

    private Integer contarPorExecucao(String tabela, Long jobExecutionId) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM " + tabela + " WHERE job_execution_id = ?",
                Integer.class,
                jobExecutionId);
    }
}

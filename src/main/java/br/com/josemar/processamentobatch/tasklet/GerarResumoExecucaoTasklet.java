package br.com.josemar.processamentobatch.tasklet;

import java.sql.Timestamp;
import java.time.LocalDate;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Consolida metricas da execucao em uma tabela de negocio para consulta simples.
 */
@Component
public class GerarResumoExecucaoTasklet implements Tasklet {

    private final JdbcTemplate jdbcTemplate;

    public GerarResumoExecucaoTasklet(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Soma contadores dos steps e registra o resumo inicial da execucao.
     */
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        JobExecution jobExecution = contribution.getStepExecution().getJobExecution();
        String arquivoEntrada = (String) chunkContext.getStepContext().getJobParameters().get("arquivoEntrada");
        String dataProcessamento = (String) chunkContext.getStepContext().getJobParameters().get("dataProcessamento");
        long totalLidos = somar(jobExecution, "importarVendasStep", "read");
        long totalGravados = somar(jobExecution, "importarVendasStep", "write");
        long totalRejeitados = contar("SELECT COUNT(1) FROM REGISTRO_REJEITADO WHERE job_execution_id = ?", jobExecution.getId());
        long totalFaturas = contar("SELECT COUNT(1) FROM FATURA WHERE job_execution_id = ?", jobExecution.getId());

        jdbcTemplate.update("""
                MERGE INTO RESUMO_EXECUCAO (
                    job_execution_id, nome_job, arquivo_entrada, data_processamento,
                    total_lidos, total_gravados, total_rejeitados, total_faturas,
                    status_final, data_inicio, data_fim
                ) KEY (job_execution_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                jobExecution.getId(),
                jobExecution.getJobInstance().getJobName(),
                arquivoEntrada,
                LocalDate.parse(dataProcessamento),
                totalLidos,
                totalGravados,
                totalRejeitados,
                totalFaturas,
                jobExecution.getStatus().name(),
                jobExecution.getStartTime() == null ? null : Timestamp.valueOf(jobExecution.getStartTime()),
                null);
        return RepeatStatus.FINISHED;
    }

    private long somar(JobExecution jobExecution, String nomeStep, String tipoContador) {
        return jobExecution.getStepExecutions().stream()
                .filter(step -> nomeStep.equals(step.getStepName()))
                .mapToLong(step -> "read".equals(tipoContador) ? step.getReadCount() : step.getWriteCount())
                .sum();
    }

    private long contar(String sql, Long jobExecutionId) {
        Long total = jdbcTemplate.queryForObject(sql, Long.class, jobExecutionId);
        return total == null ? 0L : total;
    }
}

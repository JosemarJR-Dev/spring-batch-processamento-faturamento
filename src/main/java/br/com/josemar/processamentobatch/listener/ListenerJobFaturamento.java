package br.com.josemar.processamentobatch.listener;

import java.sql.Timestamp;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Finaliza informacoes de auditoria apos o termino do job de faturamento.
 */
@Component
public class ListenerJobFaturamento implements JobExecutionListener {

    private final JdbcTemplate jdbcTemplate;

    public ListenerJobFaturamento(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Atualiza status final e data de fim no resumo de execucao.
     */
    @Override
    public void afterJob(JobExecution jobExecution) {
        jdbcTemplate.update("""
                UPDATE RESUMO_EXECUCAO
                SET status_final = ?, data_fim = ?
                WHERE job_execution_id = ?
                """,
                jobExecution.getStatus().name(),
                jobExecution.getEndTime() == null ? null : Timestamp.valueOf(jobExecution.getEndTime()),
                jobExecution.getId());
    }
}

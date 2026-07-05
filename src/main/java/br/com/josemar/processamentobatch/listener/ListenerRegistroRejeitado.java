package br.com.josemar.processamentobatch.listener;

import java.time.LocalDateTime;

import br.com.josemar.processamentobatch.dominio.VendaStaging;
import br.com.josemar.processamentobatch.dto.RegistroVendaCsv;
import br.com.josemar.processamentobatch.excecao.RegistroVendaInvalidoException;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Persiste no banco os registros rejeitados durante o processamento do CSV.
 */
@Component
public class ListenerRegistroRejeitado implements SkipListener<RegistroVendaCsv, VendaStaging>, StepExecutionListener {

    private final JdbcTemplate jdbcTemplate;
    private StepExecution stepExecution;

    public ListenerRegistroRejeitado(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Guarda o contexto do step para rastrear a rejeicao no banco.
     */
    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return stepExecution.getExitStatus();
    }

    /**
     * Registra rejeicoes causadas por regra de negocio no processor.
     */
    @Override
    public void onSkipInProcess(RegistroVendaCsv item, Throwable t) {
        RegistroVendaCsv registro = item;
        if (t instanceof RegistroVendaInvalidoException excecao && excecao.getRegistro() != null) {
            registro = excecao.getRegistro();
        }
        gravarRejeicao(registro, t.getMessage());
    }

    private void gravarRejeicao(RegistroVendaCsv registro, String motivo) {
        jdbcTemplate.update("""
                INSERT INTO REGISTRO_REJEITADO (
                    job_execution_id, step_execution_id, numero_linha, id_venda,
                    motivo_rejeicao, conteudo_original, data_rejeicao
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                stepExecution.getJobExecutionId(),
                stepExecution.getId(),
                registro == null ? null : registro.getNumeroLinha(),
                registro == null ? null : registro.getIdVenda(),
                motivo,
                registro == null ? null : registro.getConteudoOriginal(),
                LocalDateTime.now());
    }
}

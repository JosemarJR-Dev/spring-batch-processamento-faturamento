package br.com.josemar.processamentobatch.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

/**
 * Registra eventos basicos de inicio e fim dos steps para facilitar estudos do fluxo.
 */
@Component
public class ListenerStepFaturamento implements StepExecutionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListenerStepFaturamento.class);

    /**
     * Emite log com o nome do step iniciado.
     */
    @Override
    public void beforeStep(StepExecution stepExecution) {
        LOGGER.info("Iniciando step {}", stepExecution.getStepName());
    }

    /**
     * Emite log com contadores principais ao fim do step.
     */
    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        LOGGER.info("Finalizando step {} com status {}, lidos {}, gravados {}, skips {}",
                stepExecution.getStepName(),
                stepExecution.getStatus(),
                stepExecution.getReadCount(),
                stepExecution.getWriteCount(),
                stepExecution.getSkipCount());
        return stepExecution.getExitStatus();
    }
}

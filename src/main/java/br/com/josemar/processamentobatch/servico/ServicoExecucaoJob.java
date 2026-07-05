package br.com.josemar.processamentobatch.servico;

import java.util.LinkedHashMap;
import java.util.Map;

import br.com.josemar.processamentobatch.dto.RespostaExecucaoJob;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Dispara o job de faturamento com parametros estaveis para permitir restart.
 */
@Service
public class ServicoExecucaoJob {

    private final JobLauncher jobLauncher;
    private final Job jobProcessamentoFaturamento;

    public ServicoExecucaoJob(JobLauncher jobLauncher, Job jobProcessamentoFaturamento) {
        this.jobLauncher = jobLauncher;
        this.jobProcessamentoFaturamento = jobProcessamentoFaturamento;
    }

    /**
     * Executa o job usando arquivoEntrada e dataProcessamento como identidade do JobInstance.
     */
    public RespostaExecucaoJob executar(String arquivoEntrada, String dataProcessamento) {
        try {
            JobParameters parametros = new JobParametersBuilder()
                    .addString("arquivoEntrada", arquivoEntrada)
                    .addString("dataProcessamento", dataProcessamento)
                    .toJobParameters();
            JobExecution execution = jobLauncher.run(jobProcessamentoFaturamento, parametros);
            return new RespostaExecucaoJob(
                    execution.getId(),
                    execution.getStatus().name(),
                    execution.getJobInstance().getJobName(),
                    parametrosComoMapa(execution.getJobParameters()));
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
        }
    }

    private Map<String, Object> parametrosComoMapa(JobParameters parametros) {
        Map<String, Object> mapa = new LinkedHashMap<>();
        for (Map.Entry<String, JobParameter<?>> entrada : parametros.getParameters().entrySet()) {
            mapa.put(entrada.getKey(), entrada.getValue().getValue());
        }
        return mapa;
    }
}

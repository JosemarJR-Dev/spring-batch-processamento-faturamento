package br.com.josemar.processamentobatch.configuracao;

import br.com.josemar.processamentobatch.listener.ListenerJobFaturamento;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Monta o job principal de processamento batch de faturamento.
 */
@Configuration
public class ConfiguracaoJobFaturamento {

    /**
     * Encadeia validacao, importacao, faturamento, arquivo de rejeicoes e resumo.
     */
    @Bean
    public Job jobProcessamentoFaturamento(
            JobRepository jobRepository,
            Step validarArquivoEntradaStep,
            Step importarVendasStep,
            Step gerarFaturasStep,
            Step gerarArquivoRejeicoesStep,
            Step gerarResumoExecucaoStep,
            ListenerJobFaturamento listenerJobFaturamento) {
        return new JobBuilder("jobProcessamentoFaturamento", jobRepository)
                .listener(listenerJobFaturamento)
                .start(validarArquivoEntradaStep)
                .next(importarVendasStep)
                .next(gerarFaturasStep)
                .next(gerarArquivoRejeicoesStep)
                .next(gerarResumoExecucaoStep)
                .build();
    }
}

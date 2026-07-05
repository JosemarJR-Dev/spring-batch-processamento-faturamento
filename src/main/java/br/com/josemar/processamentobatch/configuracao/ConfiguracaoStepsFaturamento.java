package br.com.josemar.processamentobatch.configuracao;

import br.com.josemar.processamentobatch.dominio.Fatura;
import br.com.josemar.processamentobatch.dominio.VendaStaging;
import br.com.josemar.processamentobatch.dto.RegistroVendaCsv;
import br.com.josemar.processamentobatch.escritor.EscritorFatura;
import br.com.josemar.processamentobatch.excecao.ErroTransitorioBancoException;
import br.com.josemar.processamentobatch.excecao.RegistroVendaInvalidoException;
import br.com.josemar.processamentobatch.listener.ListenerRegistroRejeitado;
import br.com.josemar.processamentobatch.listener.ListenerStepFaturamento;
import br.com.josemar.processamentobatch.processador.ProcessadorGeracaoFatura;
import br.com.josemar.processamentobatch.processador.ProcessadorValidacaoVenda;
import br.com.josemar.processamentobatch.tasklet.GerarArquivoRejeicoesTasklet;
import br.com.josemar.processamentobatch.tasklet.GerarResumoExecucaoTasklet;
import br.com.josemar.processamentobatch.tasklet.ValidarArquivoEntradaTasklet;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Declara os steps que compoem a rotina batch de faturamento.
 */
@Configuration
public class ConfiguracaoStepsFaturamento {

    /**
     * Valida a existencia e o cabecalho do arquivo informado.
     */
    @Bean
    public Step validarArquivoEntradaStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ValidarArquivoEntradaTasklet tasklet,
            ListenerStepFaturamento listenerStepFaturamento) {
        return new StepBuilder("validarArquivoEntradaStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .listener(listenerStepFaturamento)
                .build();
    }

    /**
     * Importa vendas validas e pula registros recusados por regras de negocio.
     */
    @Bean
    public Step importarVendasStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            FlatFileItemReader<RegistroVendaCsv> leitorRegistroVendaCsv,
            ProcessadorValidacaoVenda processadorValidacaoVenda,
            JdbcBatchItemWriter<VendaStaging> escritorVendaStaging,
            ListenerRegistroRejeitado listenerRegistroRejeitado,
            ListenerStepFaturamento listenerStepFaturamento) {
        return new StepBuilder("importarVendasStep", jobRepository)
                .<RegistroVendaCsv, VendaStaging>chunk(100, transactionManager)
                .reader(leitorRegistroVendaCsv)
                .processor(processadorValidacaoVenda)
                .writer(escritorVendaStaging)
                .faultTolerant()
                .skip(RegistroVendaInvalidoException.class)
                .skipLimit(100)
                .listener((SkipListener<RegistroVendaCsv, VendaStaging>) listenerRegistroRejeitado)
                .listener((StepExecutionListener) processadorValidacaoVenda)
                .listener((StepExecutionListener) listenerRegistroRejeitado)
                .listener(listenerStepFaturamento)
                .build();
    }

    /**
     * Gera faturas para vendas importadas e demonstra retry de erro transitorio.
     */
    @Bean
    public Step gerarFaturasStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            JdbcCursorItemReader<VendaStaging> leitorVendasNaoFaturadas,
            ProcessadorGeracaoFatura processadorGeracaoFatura,
            EscritorFatura escritorFatura,
            ListenerStepFaturamento listenerStepFaturamento) {
        return new StepBuilder("gerarFaturasStep", jobRepository)
                .<VendaStaging, Fatura>chunk(100, transactionManager)
                .reader(leitorVendasNaoFaturadas)
                .processor(processadorGeracaoFatura)
                .writer(escritorFatura)
                .faultTolerant()
                .retry(ErroTransitorioBancoException.class)
                .retryLimit(3)
                .listener(listenerStepFaturamento)
                .build();
    }

    /**
     * Gera arquivo CSV com os registros rejeitados na importacao.
     */
    @Bean
    public Step gerarArquivoRejeicoesStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            GerarArquivoRejeicoesTasklet tasklet,
            ListenerStepFaturamento listenerStepFaturamento) {
        return new StepBuilder("gerarArquivoRejeicoesStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .listener(listenerStepFaturamento)
                .build();
    }

    /**
     * Consolida os contadores da execucao em uma tabela de resumo.
     */
    @Bean
    public Step gerarResumoExecucaoStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            GerarResumoExecucaoTasklet tasklet,
            ListenerStepFaturamento listenerStepFaturamento) {
        return new StepBuilder("gerarResumoExecucaoStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .listener(listenerStepFaturamento)
                .build();
    }
}

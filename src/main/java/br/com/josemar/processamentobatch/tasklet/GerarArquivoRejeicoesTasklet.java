package br.com.josemar.processamentobatch.tasklet;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Exporta as rejeicoes registradas no banco para um CSV de saida.
 */
@Component
public class GerarArquivoRejeicoesTasklet implements Tasklet {

    private final JdbcTemplate jdbcTemplate;

    public GerarArquivoRejeicoesTasklet(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Gera o arquivo output/rejeicoes-{dataProcessamento}.csv para auditoria da carga.
     */
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        Long jobExecutionId = contribution.getStepExecution().getJobExecutionId();
        String dataProcessamento = (String) chunkContext.getStepContext().getJobParameters().get("dataProcessamento");
        Path diretorio = Path.of("output");
        Files.createDirectories(diretorio);

        List<String> linhas = jdbcTemplate.query("""
                SELECT numero_linha, id_venda, motivo_rejeicao, conteudo_original
                FROM REGISTRO_REJEITADO
                WHERE job_execution_id = ?
                ORDER BY numero_linha
                """, (rs, rowNum) -> String.join(",",
                valorCsv(rs.getString("numero_linha")),
                valorCsv(rs.getString("id_venda")),
                valorCsv(rs.getString("motivo_rejeicao")),
                valorCsv(rs.getString("conteudo_original"))), jobExecutionId);

        linhas.add(0, "numero_linha,id_venda,motivo_rejeicao,conteudo_original");
        Files.write(diretorio.resolve("rejeicoes-" + dataProcessamento + ".csv"), linhas);
        return RepeatStatus.FINISHED;
    }

    private String valorCsv(String valor) {
        if (valor == null) {
            return "";
        }
        return "\"" + valor.replace("\"", "\"\"") + "\"";
    }
}

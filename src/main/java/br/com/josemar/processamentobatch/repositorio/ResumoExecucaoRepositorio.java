package br.com.josemar.processamentobatch.repositorio;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import br.com.josemar.processamentobatch.dominio.Fatura;
import br.com.josemar.processamentobatch.dominio.RegistroRejeitado;
import br.com.josemar.processamentobatch.dominio.ResumoExecucao;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Consulta informacoes consolidadas de execucoes do job de faturamento.
 */
@Repository
public class ResumoExecucaoRepositorio {

    private final JdbcTemplate jdbcTemplate;

    public ResumoExecucaoRepositorio(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Lista as execucoes mais recentes para consulta via endpoint.
     */
    public List<ResumoExecucao> listarUltimasExecucoes() {
        return jdbcTemplate.query("""
                SELECT * FROM RESUMO_EXECUCAO
                ORDER BY job_execution_id DESC
                LIMIT 20
                """, this::mapearResumo);
    }

    /**
     * Busca um resumo pelo identificador tecnico da execucao do job.
     */
    public Optional<ResumoExecucao> buscarPorJobExecutionId(Long id) {
        List<ResumoExecucao> resumos = jdbcTemplate.query(
                "SELECT * FROM RESUMO_EXECUCAO WHERE job_execution_id = ?",
                this::mapearResumo,
                id);
        return resumos.stream().findFirst();
    }

    /**
     * Lista as rejeicoes geradas por uma execucao especifica.
     */
    public List<RegistroRejeitado> listarRejeicoes(Long jobExecutionId) {
        return jdbcTemplate.query("""
                SELECT * FROM REGISTRO_REJEITADO
                WHERE job_execution_id = ?
                ORDER BY numero_linha
                """, this::mapearRejeicao, jobExecutionId);
    }

    /**
     * Lista as faturas geradas por uma execucao especifica.
     */
    public List<Fatura> listarFaturas(Long jobExecutionId) {
        return jdbcTemplate.query("""
                SELECT * FROM FATURA
                WHERE job_execution_id = ?
                ORDER BY id
                """, this::mapearFatura, jobExecutionId);
    }

    private ResumoExecucao mapearResumo(ResultSet rs, int rowNum) throws SQLException {
        return new ResumoExecucao(
                rs.getLong("id"),
                rs.getLong("job_execution_id"),
                rs.getString("nome_job"),
                rs.getString("arquivo_entrada"),
                rs.getDate("data_processamento").toLocalDate(),
                rs.getLong("total_lidos"),
                rs.getLong("total_gravados"),
                rs.getLong("total_rejeitados"),
                rs.getLong("total_faturas"),
                rs.getString("status_final"),
                rs.getTimestamp("data_inicio") == null ? null : rs.getTimestamp("data_inicio").toLocalDateTime(),
                rs.getTimestamp("data_fim") == null ? null : rs.getTimestamp("data_fim").toLocalDateTime());
    }

    private RegistroRejeitado mapearRejeicao(ResultSet rs, int rowNum) throws SQLException {
        return new RegistroRejeitado(
                rs.getLong("id"),
                rs.getLong("job_execution_id"),
                rs.getLong("step_execution_id"),
                rs.getObject("numero_linha", Integer.class),
                rs.getString("id_venda"),
                rs.getString("motivo_rejeicao"),
                rs.getString("conteudo_original"),
                rs.getTimestamp("data_rejeicao").toLocalDateTime());
    }

    private Fatura mapearFatura(ResultSet rs, int rowNum) throws SQLException {
        return Fatura.builder()
                .id(rs.getLong("id"))
                .vendaStagingId(rs.getLong("venda_staging_id"))
                .idVenda(rs.getString("id_venda"))
                .codigoCliente(rs.getString("codigo_cliente"))
                .codigoProduto(rs.getString("codigo_produto"))
                .valorTotal(rs.getBigDecimal("valor_total"))
                .dataEmissao(rs.getDate("data_emissao").toLocalDate())
                .dataVencimento(rs.getDate("data_vencimento").toLocalDate())
                .jobExecutionId(rs.getLong("job_execution_id"))
                .build();
    }
}

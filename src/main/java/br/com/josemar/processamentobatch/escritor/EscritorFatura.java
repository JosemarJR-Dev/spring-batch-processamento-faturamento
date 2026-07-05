package br.com.josemar.processamentobatch.escritor;

import java.sql.Date;
import java.util.List;

import br.com.josemar.processamentobatch.dominio.Fatura;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Grava faturas e marca as vendas de staging como faturadas no mesmo chunk.
 */
@Component
public class EscritorFatura implements ItemWriter<Fatura> {

    private final JdbcTemplate jdbcTemplate;

    public EscritorFatura(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Persiste faturas geradas e atualiza o status das vendas correspondentes.
     */
    @Override
    public void write(Chunk<? extends Fatura> chunk) {
        List<? extends Fatura> faturas = chunk.getItems();
        jdbcTemplate.batchUpdate("""
                INSERT INTO FATURA (
                    venda_staging_id, id_venda, codigo_cliente, codigo_produto,
                    valor_total, data_emissao, data_vencimento, job_execution_id
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """, faturas, faturas.size(), (ps, fatura) -> {
            ps.setLong(1, fatura.getVendaStagingId());
            ps.setString(2, fatura.getIdVenda());
            ps.setString(3, fatura.getCodigoCliente());
            ps.setString(4, fatura.getCodigoProduto());
            ps.setBigDecimal(5, fatura.getValorTotal());
            ps.setDate(6, Date.valueOf(fatura.getDataEmissao()));
            ps.setDate(7, Date.valueOf(fatura.getDataVencimento()));
            ps.setLong(8, fatura.getJobExecutionId());
        });

        jdbcTemplate.batchUpdate("""
                UPDATE VENDA_STAGING
                SET status = 'FATURADA'
                WHERE id = ?
                """, faturas, faturas.size(), (ps, fatura) -> ps.setLong(1, fatura.getVendaStagingId()));
    }
}

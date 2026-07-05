package br.com.josemar.processamentobatch.configuracao;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import br.com.josemar.processamentobatch.dominio.FormaPagamento;
import br.com.josemar.processamentobatch.dominio.StatusVenda;
import br.com.josemar.processamentobatch.dominio.VendaStaging;
import br.com.josemar.processamentobatch.dto.RegistroVendaCsv;
import br.com.josemar.processamentobatch.leitor.MapeadorLinhaVendaCsv;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.RowMapper;

/**
 * Centraliza leitores usados pelos steps de importacao e faturamento.
 */
@Configuration
public class ConfiguracaoLeitores {

    /**
     * Le o CSV informado por JobParameter e transforma cada linha em RegistroVendaCsv.
     */
    @Bean
    @StepScope
    public FlatFileItemReader<RegistroVendaCsv> leitorRegistroVendaCsv(
            @Value("#{jobParameters['arquivoEntrada']}") String arquivoEntrada) {
        return new FlatFileItemReaderBuilder<RegistroVendaCsv>()
                .name("leitorRegistroVendaCsv")
                .resource(new FileSystemResource(arquivoEntrada))
                .linesToSkip(1)
                .lineMapper(new MapeadorLinhaVendaCsv())
                .saveState(true)
                .build();
    }

    /**
     * Le vendas validas ainda nao faturadas na tabela de staging.
     */
    @Bean
    @StepScope
    public JdbcCursorItemReader<VendaStaging> leitorVendasNaoFaturadas(DataSource dataSource) {
        JdbcCursorItemReader<VendaStaging> reader = new JdbcCursorItemReader<>();
        reader.setName("leitorVendasNaoFaturadas");
        reader.setDataSource(dataSource);
        reader.setSql("""
                SELECT *
                FROM VENDA_STAGING
                WHERE status = 'IMPORTADA'
                ORDER BY id
                """);
        reader.setRowMapper(new VendaStagingRowMapper());
        return reader;
    }

    private static class VendaStagingRowMapper implements RowMapper<VendaStaging> {

        @Override
        public VendaStaging mapRow(ResultSet rs, int rowNum) throws SQLException {
            return VendaStaging.builder()
                    .id(rs.getLong("id"))
                    .idVenda(rs.getString("id_venda"))
                    .codigoCliente(rs.getString("codigo_cliente"))
                    .codigoProduto(rs.getString("codigo_produto"))
                    .quantidade(rs.getInt("quantidade"))
                    .valorUnitario(rs.getBigDecimal("valor_unitario"))
                    .valorTotal(rs.getBigDecimal("valor_total"))
                    .dataVenda(rs.getDate("data_venda").toLocalDate())
                    .formaPagamento(FormaPagamento.valueOf(rs.getString("forma_pagamento")))
                    .status(StatusVenda.valueOf(rs.getString("status")))
                    .dataImportacao(rs.getTimestamp("data_importacao").toLocalDateTime())
                    .jobExecutionId(rs.getLong("job_execution_id"))
                    .build();
        }
    }
}

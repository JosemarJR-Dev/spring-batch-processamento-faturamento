package br.com.josemar.processamentobatch.configuracao;

import javax.sql.DataSource;

import br.com.josemar.processamentobatch.dominio.VendaStaging;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Centraliza writers JDBC usados pelo job de faturamento.
 */
@Configuration
public class ConfiguracaoEscritores {

    /**
     * Grava vendas validadas na tabela VENDA_STAGING.
     */
    @Bean
    public JdbcBatchItemWriter<VendaStaging> escritorVendaStaging(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<VendaStaging>()
                .dataSource(dataSource)
                .beanMapped()
                .sql("""
                        INSERT INTO VENDA_STAGING (
                            id_venda, codigo_cliente, codigo_produto, quantidade,
                            valor_unitario, valor_total, data_venda, forma_pagamento,
                            status, data_importacao, job_execution_id
                        ) VALUES (
                            :idVenda, :codigoCliente, :codigoProduto, :quantidade,
                            :valorUnitario, :valorTotal, :dataVenda, :formaPagamentoNome,
                            :statusNome, :dataImportacao, :jobExecutionId
                        )
                        """)
                .build();
    }
}

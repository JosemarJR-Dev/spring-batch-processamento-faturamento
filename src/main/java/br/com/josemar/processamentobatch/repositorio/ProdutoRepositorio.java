package br.com.josemar.processamentobatch.repositorio;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Consulta produtos usados na validacao das vendas importadas.
 */
@Repository
public class ProdutoRepositorio {

    private final JdbcTemplate jdbcTemplate;

    public ProdutoRepositorio(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Verifica se o produto existe no cadastro corporativo simulado.
     */
    public boolean existePorCodigo(String codigoProduto) {
        Integer total = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM PRODUTO WHERE codigo = ?",
                Integer.class,
                codigoProduto);
        return total != null && total > 0;
    }
}

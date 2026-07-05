package br.com.josemar.processamentobatch.leitor;

import br.com.josemar.processamentobatch.dto.RegistroVendaCsv;
import org.springframework.batch.item.file.LineMapper;

/**
 * Converte uma linha fisica do CSV em objeto bruto preservando rastreabilidade.
 */
public class MapeadorLinhaVendaCsv implements LineMapper<RegistroVendaCsv> {

    /**
     * Mapeia colunas por posicao sem validar regras de negocio.
     */
    @Override
    public RegistroVendaCsv mapLine(String line, int lineNumber) {
        String[] colunas = line.split(",", -1);
        return new RegistroVendaCsv(
                lineNumber,
                line,
                valor(colunas, 0),
                valor(colunas, 1),
                valor(colunas, 2),
                valor(colunas, 3),
                valor(colunas, 4),
                valor(colunas, 5),
                valor(colunas, 6));
    }

    private String valor(String[] colunas, int indice) {
        return indice < colunas.length ? colunas[indice].trim() : "";
    }
}

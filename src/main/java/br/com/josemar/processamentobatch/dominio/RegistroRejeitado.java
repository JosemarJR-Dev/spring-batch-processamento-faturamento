package br.com.josemar.processamentobatch.dominio;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representa um registro recusado durante a importacao do arquivo.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistroRejeitado {

    private Long id;
    private Long jobExecutionId;
    private Long stepExecutionId;
    private Integer numeroLinha;
    private String idVenda;
    private String motivoRejeicao;
    private String conteudoOriginal;
    private LocalDateTime dataRejeicao;
}

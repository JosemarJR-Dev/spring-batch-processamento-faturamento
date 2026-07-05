package br.com.josemar.processamentobatch.controlador;

import java.util.List;

import br.com.josemar.processamentobatch.dominio.ResumoExecucao;
import br.com.josemar.processamentobatch.dto.DetalheExecucaoJob;
import br.com.josemar.processamentobatch.dto.RequisicaoExecucaoJob;
import br.com.josemar.processamentobatch.dto.RespostaExecucaoJob;
import br.com.josemar.processamentobatch.repositorio.ResumoExecucaoRepositorio;
import br.com.josemar.processamentobatch.servico.ServicoExecucaoJob;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expoe endpoints simples para disparar e consultar execucoes do job.
 */
@RestController
@RequestMapping("/jobs/faturamento")
public class ControladorJobBatch {

    private final ServicoExecucaoJob servicoExecucaoJob;
    private final ResumoExecucaoRepositorio resumoExecucaoRepositorio;

    public ControladorJobBatch(
            ServicoExecucaoJob servicoExecucaoJob,
            ResumoExecucaoRepositorio resumoExecucaoRepositorio) {
        this.servicoExecucaoJob = servicoExecucaoJob;
        this.resumoExecucaoRepositorio = resumoExecucaoRepositorio;
    }

    /**
     * Dispara o job de faturamento com os parametros recebidos no corpo da requisicao.
     */
    @PostMapping("/executar")
    public RespostaExecucaoJob executar(@Valid @RequestBody RequisicaoExecucaoJob requisicao) {
        return servicoExecucaoJob.executar(requisicao.arquivoEntrada(), requisicao.dataProcessamento());
    }

    /**
     * Retorna as ultimas execucoes consolidadas na tabela de resumo.
     */
    @GetMapping("/execucoes")
    public List<ResumoExecucao> listarExecucoes() {
        return resumoExecucaoRepositorio.listarUltimasExecucoes();
    }

    /**
     * Retorna resumo, rejeicoes e faturas de uma execucao especifica.
     */
    @GetMapping("/execucoes/{id}")
    public ResponseEntity<DetalheExecucaoJob> detalharExecucao(@PathVariable Long id) {
        return resumoExecucaoRepositorio.buscarPorJobExecutionId(id)
                .map(resumo -> new DetalheExecucaoJob(
                        resumo,
                        resumoExecucaoRepositorio.listarRejeicoes(id),
                        resumoExecucaoRepositorio.listarFaturas(id)))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

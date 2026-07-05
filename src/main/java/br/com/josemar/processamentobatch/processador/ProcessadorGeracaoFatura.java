package br.com.josemar.processamentobatch.processador;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import br.com.josemar.processamentobatch.dominio.Fatura;
import br.com.josemar.processamentobatch.dominio.VendaStaging;
import br.com.josemar.processamentobatch.excecao.ErroTransitorioBancoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

/**
 * Converte vendas importadas em faturas e simula retry de falha transitoria.
 */
@Component
public class ProcessadorGeracaoFatura implements ItemProcessor<VendaStaging, Fatura> {

    private final boolean simularErroTransitorio;
    private final String idVendaErroTransitorio;
    private final Map<String, AtomicInteger> tentativasPorVenda = new ConcurrentHashMap<>();

    public ProcessadorGeracaoFatura(
            @Value("${batch.faturamento.simular-erro-transitorio:false}") boolean simularErroTransitorio,
            @Value("${batch.faturamento.id-venda-erro-transitorio:1002}") String idVendaErroTransitorio) {
        this.simularErroTransitorio = simularErroTransitorio;
        this.idVendaErroTransitorio = idVendaErroTransitorio;
    }

    /**
     * Gera a fatura e, quando habilitado, falha duas vezes para demonstrar retry.
     */
    @Override
    public Fatura process(VendaStaging item) {
        if (deveSimularErro(item)) {
            throw new ErroTransitorioBancoException("Falha transitoria simulada para a venda " + item.getIdVenda());
        }

        LocalDate dataEmissao = LocalDate.now();
        return Fatura.builder()
                .vendaStagingId(item.getId())
                .idVenda(item.getIdVenda())
                .codigoCliente(item.getCodigoCliente())
                .codigoProduto(item.getCodigoProduto())
                .valorTotal(item.getValorTotal())
                .dataEmissao(dataEmissao)
                .dataVencimento(dataEmissao.plusDays(10))
                .jobExecutionId(item.getJobExecutionId())
                .build();
    }

    private boolean deveSimularErro(VendaStaging item) {
        if (!simularErroTransitorio || !idVendaErroTransitorio.equals(item.getIdVenda())) {
            return false;
        }
        AtomicInteger tentativas = tentativasPorVenda.computeIfAbsent(item.getIdVenda(), chave -> new AtomicInteger());
        return tentativas.incrementAndGet() <= 2;
    }
}

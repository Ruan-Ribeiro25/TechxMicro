package com.vidaplus.service.impl;

import com.vidaplus.entity.TransacaoFinanceira;
import com.vidaplus.entity.TransacaoFinanceira.CategoriaFinanceira;
import com.vidaplus.entity.TransacaoFinanceira.StatusPagamento;
import com.vidaplus.entity.TransacaoFinanceira.TipoTransacao;
import com.vidaplus.repository.TransacaoFinanceiraRepository;
import com.vidaplus.service.FinanceiroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FinanceiroServiceImpl implements FinanceiroService {

    @Autowired
    private TransacaoFinanceiraRepository transacaoRepository;

    @Override
    public TransacaoFinanceira registrarReceita(String descricao, BigDecimal valor, String mpPreferenceId, String mpLink, LocalDate dataVencimento) {
        TransacaoFinanceira t = new TransacaoFinanceira();
        t.setDescricao(descricao);
        t.setValor(valor);
        t.setTipo(TipoTransacao.RECEITA);
        t.setCategoria(CategoriaFinanceira.CONSULTA); // Padrão
        
        // Dados Financeiros
        t.setDataVencimento(dataVencimento != null ? dataVencimento : LocalDate.now().plusDays(3)); // Padrão 3 dias se nulo
        t.setStatus(StatusPagamento.PENDENTE); // Nasce pendente
        
        // Dados do Mercado Pago
        t.setMpPreferenceId(mpPreferenceId);
        t.setMpPaymentLink(mpLink);
        t.setMpStatus("pending"); 
        
        return transacaoRepository.save(t);
    }

    @Override
    public TransacaoFinanceira registrarDespesa(String descricao, BigDecimal valor, CategoriaFinanceira categoria, LocalDate dataVencimento, StatusPagamento status) {
        TransacaoFinanceira t = new TransacaoFinanceira();
        t.setDescricao(descricao);
        t.setValor(valor);
        t.setTipo(TipoTransacao.DESPESA);
        t.setCategoria(categoria);
        
        // Dados Financeiros
        t.setDataVencimento(dataVencimento);
        t.setStatus(status != null ? status : StatusPagamento.PENDENTE);
        
        // Se já nascer PAGO, define a data de pagamento para hoje
        if (t.getStatus() == StatusPagamento.PAGO) {
            t.setDataPagamento(LocalDate.now());
        }
        
        return transacaoRepository.save(t);
    }

    @Override
    public void atualizarStatusPagamento(String mpPreferenceId, String novoStatus) {
        Optional<TransacaoFinanceira> transacaoOpt = transacaoRepository.findByMpPreferenceId(mpPreferenceId);
        if (transacaoOpt.isPresent()) {
            TransacaoFinanceira t = transacaoOpt.get();
            t.setMpStatus(novoStatus);
            
            // Lógica Inteligente: Se o MP aprovar, baixa no sistema financeiro
            if ("approved".equalsIgnoreCase(novoStatus)) {
                t.setStatus(StatusPagamento.PAGO);
                t.setDataPagamento(LocalDate.now());
            } else if ("rejected".equalsIgnoreCase(novoStatus) || "cancelled".equalsIgnoreCase(novoStatus)) {
                t.setStatus(StatusPagamento.CANCELADO);
            }
            
            transacaoRepository.save(t);
        }
    }

    @Override
    public void processarStatusAtrasados() {
        // Busca todas as transações (Idealmente seria uma query filtrada, mas faremos a lógica aqui por segurança)
        List<TransacaoFinanceira> todas = transacaoRepository.findAll();
        
        for (TransacaoFinanceira t : todas) {
            // Se estiver PENDENTE e a Data de Vencimento for anterior a HOJE -> Marca como ATRASADO
            if (t.getStatus() == StatusPagamento.PENDENTE && 
                t.getDataVencimento() != null && 
                t.getDataVencimento().isBefore(LocalDate.now())) {
                
                t.setStatus(StatusPagamento.ATRASADO);
                transacaoRepository.save(t);
            }
        }
    }

    @Override
    public BigDecimal calcularTotalReceitas() {
        BigDecimal total = transacaoRepository.sumTotalByTipo(TipoTransacao.RECEITA);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal calcularTotalDespesas() {
        BigDecimal total = transacaoRepository.sumTotalByTipo(TipoTransacao.DESPESA);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal calcularSaldoLiquido() {
        return calcularTotalReceitas().subtract(calcularTotalDespesas());
    }

    @Override
    public List<TransacaoFinanceira> listarUltimasTransacoes() {
        return transacaoRepository.findAll(
            PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "dataHora"))
        ).getContent();
    }

    @Override
    public Map<String, BigDecimal> obterDadosDespesasPorCategoria() {
        List<Object[]> resultados = transacaoRepository.sumByCategoria(TipoTransacao.DESPESA);
        Map<String, BigDecimal> mapa = new HashMap<>();
        
        for (Object[] obj : resultados) {
            CategoriaFinanceira cat = (CategoriaFinanceira) obj[0];
            BigDecimal valor = (BigDecimal) obj[1];
            mapa.put(cat.name(), valor);
        }
        return mapa;
    }
}
package com.techxmicro.service;

import com.techxmicro.dto.GraficoFluxoDTO;
import com.techxmicro.entity.TransacaoFinanceira;
import com.techxmicro.entity.TransacaoFinanceira.CategoriaFinanceira;
import com.techxmicro.entity.TransacaoFinanceira.StatusPagamento;
import com.techxmicro.entity.TransacaoFinanceira.TipoTransacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface FinanceiroService {
    
    // Métodos Originais do seu Sistema (Mercado Pago etc)
    TransacaoFinanceira registrarReceita(String descricao, BigDecimal valor, String mpPreferenceId, String mpLink, LocalDate dataVencimento);
    TransacaoFinanceira registrarDespesa(String descricao, BigDecimal valor, CategoriaFinanceira categoria, LocalDate dataVencimento, StatusPagamento status);
    void atualizarStatusPagamento(String mpPreferenceId, String novoStatus);
    
    // Novos Métodos do Dashboard
    void registrarTransacao(String descricao, BigDecimal valor, TipoTransacao tipo, CategoriaFinanceira categoria, LocalDate dataVencimento, StatusPagamento status);
    GraficoFluxoDTO obterDadosGrafico(String periodo);
    
    // Processamento e Cálculos
    void processarStatusAtrasados();
    BigDecimal calcularTotalReceitas();
    BigDecimal calcularTotalDespesas();
    BigDecimal calcularSaldoLiquido();
    List<TransacaoFinanceira> listarUltimasTransacoes();
    Map<String, BigDecimal> obterDadosDespesasPorCategoria();
}
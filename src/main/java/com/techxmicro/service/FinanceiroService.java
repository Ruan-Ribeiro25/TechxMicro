package com.techxmicro.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.techxmicro.entity.TransacaoFinanceira;
import com.techxmicro.entity.TransacaoFinanceira.CategoriaFinanceira;
import com.techxmicro.entity.TransacaoFinanceira.StatusPagamento;

public interface FinanceiroService {

    // Registra entrada (ACEITANDO DATA DE VENCIMENTO)
    TransacaoFinanceira registrarReceita(String descricao, BigDecimal valor, String mpPreferenceId, String mpLink, LocalDate dataVencimento);

    // Registra saída (ACEITANDO DATA DE VENCIMENTO E STATUS)
    TransacaoFinanceira registrarDespesa(String descricao, BigDecimal valor, CategoriaFinanceira categoria, LocalDate dataVencimento, StatusPagamento status);

    void atualizarStatusPagamento(String mpPreferenceId, String novoStatus);

    // NOVO MÉTODO (Que estava faltando na interface e gerando erro)
    void processarStatusAtrasados();

    BigDecimal calcularTotalReceitas();
    BigDecimal calcularTotalDespesas();
    BigDecimal calcularSaldoLiquido();

    List<TransacaoFinanceira> listarUltimasTransacoes();

    Map<String, BigDecimal> obterDadosDespesasPorCategoria(); 
}
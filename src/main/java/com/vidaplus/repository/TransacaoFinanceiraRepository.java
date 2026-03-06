package com.vidaplus.repository;

import com.vidaplus.entity.TransacaoFinanceira;
import com.vidaplus.entity.TransacaoFinanceira.TipoTransacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransacaoFinanceiraRepository extends JpaRepository<TransacaoFinanceira, Long> {

    // Buscar transação específica pelo ID do Mercado Pago (para atualizar status via Webhook)
    Optional<TransacaoFinanceira> findByMpPreferenceId(String mpPreferenceId);

    // Listar transações por período (para filtros de data no painel)
    List<TransacaoFinanceira> findByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim);

    // SOMA TOTAL de Receitas ou Despesas (Para o Card de Resumo Financeiro)
    @Query("SELECT SUM(t.valor) FROM TransacaoFinanceira t WHERE t.tipo = :tipo")
    BigDecimal sumTotalByTipo(@Param("tipo") TipoTransacao tipo);

    // SOMA TOTAL por período e tipo (Para o fluxo de caixa mensal)
    @Query("SELECT SUM(t.valor) FROM TransacaoFinanceira t WHERE t.tipo = :tipo AND t.dataHora BETWEEN :inicio AND :fim")
    BigDecimal sumTotalByTipoAndPeriodo(@Param("tipo") TipoTransacao tipo, @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    // Agrupamento por Categoria (Essencial para o Gráfico de Pizza: "Onde estou gastando mais?")
    @Query("SELECT t.categoria, SUM(t.valor) FROM TransacaoFinanceira t WHERE t.tipo = :tipo GROUP BY t.categoria")
    List<Object[]> sumByCategoria(@Param("tipo") TipoTransacao tipo);
}
package com.techxmicro.repository;

import com.techxmicro.entity.TransacaoFinanceira;
import com.techxmicro.entity.TransacaoFinanceira.TipoTransacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface TransacaoFinanceiraRepository extends JpaRepository<TransacaoFinanceira, Long> {

    Optional<TransacaoFinanceira> findByMpPreferenceId(String mpPreferenceId);

    // Soma apenas os valores que estão como PAGO
    @Query("SELECT SUM(t.valor) FROM TransacaoFinanceira t WHERE t.tipo = :tipo AND t.status = 'PAGO'")
    BigDecimal sumTotalByTipo(@Param("tipo") TipoTransacao tipo);

    // Agrupa as despesas por categoria
    @Query("SELECT t.categoria, SUM(t.valor) FROM TransacaoFinanceira t WHERE t.tipo = :tipo AND t.status = 'PAGO' GROUP BY t.categoria")
    List<Object[]> sumByCategoria(@Param("tipo") TipoTransacao tipo);
}
package com.helpdesk.repository;

import com.helpdesk.entity.Usuario;
import com.helpdesk.enums.StatusPagamento;
import com.helpdesk.models.PedidoExame;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<PedidoExame, Long> {

    // 1. Busca pedidos de um paciente para o dashboard, ordenados por data
    List<PedidoExame> findByPacienteOrderByDataCriacaoDesc(Usuario paciente);

    // 2. Conta pedidos com pagamento pendente (para o KPI)
    long countByPacienteAndStatusPagamento(Usuario paciente, StatusPagamento statusPagamento);

    // 3. Busca genérica por paciente
    List<PedidoExame> findByPaciente(Usuario paciente);

    // 4. Busca pelo ID da preferência do Mercado Pago (para o Webhook futuro)
    PedidoExame findByMpPreferenceId(String mpPreferenceId);
}
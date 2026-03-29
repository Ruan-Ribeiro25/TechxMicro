package com.helpdesk.repository;

import com.helpdesk.entity.Chamado;
import com.helpdesk.enums.StatusChamado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChamadoRepository extends JpaRepository<Chamado, Long> {
    
    // Traz a lista de chamados de um solicitante específico
    List<Chamado> findBySolicitanteIdOrderByDataAberturaDesc(Long solicitanteId);
    
    // Traz a lista de chamados atribuídos a um técnico
    List<Chamado> findByResponsavelIdOrderByDataAberturaDesc(Long responsavelId);
    
    // Conta quantos chamados estão em um status específico (para o Dashboard)
    long countByStatus(StatusChamado status);
}
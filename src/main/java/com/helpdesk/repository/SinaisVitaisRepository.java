package com.helpdesk.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.helpdesk.entity.SinaisVitais;
import com.helpdesk.entity.Usuario;

import java.util.List;

@Repository
public interface SinaisVitaisRepository extends JpaRepository<SinaisVitais, Long> {
    // Busca o histórico ordenado para a linha do tempo e gráficos
    List<SinaisVitais> findByPacienteOrderByDataHoraDesc(Usuario paciente);
}
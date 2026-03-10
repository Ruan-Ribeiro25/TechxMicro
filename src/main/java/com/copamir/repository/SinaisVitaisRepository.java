package com.copamir.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.copamir.entity.SinaisVitais;
import com.copamir.entity.Usuario;

import java.util.List;

@Repository
public interface SinaisVitaisRepository extends JpaRepository<SinaisVitais, Long> {
    // Busca o histórico ordenado para a linha do tempo e gráficos
    List<SinaisVitais> findByPacienteOrderByDataHoraDesc(Usuario paciente);
}
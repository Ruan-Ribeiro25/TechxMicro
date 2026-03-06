package com.vidaplus.repository;

import com.vidaplus.entity.SinaisVitais;
import com.vidaplus.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SinaisVitaisRepository extends JpaRepository<SinaisVitais, Long> {
    // Busca o histórico ordenado para a linha do tempo e gráficos
    List<SinaisVitais> findByPacienteOrderByDataHoraDesc(Usuario paciente);
}
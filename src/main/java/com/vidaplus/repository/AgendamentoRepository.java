package com.vidaplus.repository;

import com.vidaplus.entity.Agendamento;
import com.vidaplus.entity.StatusAgendamento; // Import necessário
import com.vidaplus.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {
    
    // Busca histórico do paciente
    List<Agendamento> findByUsuario(Usuario usuario);

    // --- CORREÇÃO: O parâmetro 'status' agora deve ser do tipo ENUM, e não String ---
    // Isso resolve o erro na linha 135 do AgendamentoController
    boolean existsByProfissionalAndDataHoraAndStatusNot(String profissional, LocalDateTime dataHora, StatusAgendamento status);
}
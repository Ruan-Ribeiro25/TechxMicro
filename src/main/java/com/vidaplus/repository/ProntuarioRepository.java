package com.vidaplus.repository;

import com.vidaplus.entity.Prontuario;
import com.vidaplus.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProntuarioRepository extends JpaRepository<Prontuario, Long> {
    
    // Busca o histórico de prontuários de um paciente, do mais recente para o mais antigo
    List<Prontuario> findByPacienteOrderByDataHoraDesc(Usuario paciente);
}
package com.helpdesk.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.helpdesk.entity.Prontuario;
import com.helpdesk.entity.Usuario;

import java.util.List;

@Repository
public interface ProntuarioRepository extends JpaRepository<Prontuario, Long> {
    
    // Busca o histórico de prontuários de um paciente, do mais recente para o mais antigo
    List<Prontuario> findByPacienteOrderByDataHoraDesc(Usuario paciente);
}
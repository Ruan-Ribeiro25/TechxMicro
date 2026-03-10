package com.copamir.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.copamir.entity.Prontuario;
import com.copamir.entity.Usuario;

import java.util.List;

@Repository
public interface ProntuarioRepository extends JpaRepository<Prontuario, Long> {
    
    // Busca o histórico de prontuários de um paciente, do mais recente para o mais antigo
    List<Prontuario> findByPacienteOrderByDataHoraDesc(Usuario paciente);
}
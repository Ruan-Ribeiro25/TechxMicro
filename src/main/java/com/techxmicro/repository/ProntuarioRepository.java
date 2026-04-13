package com.techxmicro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techxmicro.entity.Prontuario;
import com.techxmicro.entity.Usuario;

import java.util.List;

@Repository
public interface ProntuarioRepository extends JpaRepository<Prontuario, Long> {
    
    // Busca o histórico de prontuários de um paciente, do mais recente para o mais antigo
    List<Prontuario> findByPacienteOrderByDataHoraDesc(Usuario paciente);
}
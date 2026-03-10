package com.helpdesk.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.helpdesk.entity.Documento;
import com.helpdesk.entity.Usuario;

import java.util.List;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Long> {
    
    // Busca documentos de um usuário específico
    List<Documento> findByUsuario(Usuario usuario);
}
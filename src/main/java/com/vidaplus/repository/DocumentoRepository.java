package com.vidaplus.repository;

import com.vidaplus.entity.Documento;
import com.vidaplus.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Long> {
    
    // Busca documentos de um usuário específico
    List<Documento> findByUsuario(Usuario usuario);
}
package com.vidaplus.repository;

import com.vidaplus.entity.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdministradorRepository extends JpaRepository<Administrador, Long> {
    // Busca o perfil de admin através do ID do usuário (login)
    Administrador findByUsuarioId(Long usuarioId);
}
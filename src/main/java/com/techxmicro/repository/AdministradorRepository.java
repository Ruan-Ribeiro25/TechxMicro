package com.techxmicro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techxmicro.entity.Administrador;

@Repository
public interface AdministradorRepository extends JpaRepository<Administrador, Long> {
    // Busca o perfil de admin através do ID do usuário (login)
    Administrador findByUsuarioId(Long usuarioId);
}
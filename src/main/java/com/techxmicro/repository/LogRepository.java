package com.techxmicro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techxmicro.entity.Log;

@Repository
public interface LogRepository extends JpaRepository<Log, Long> {
    // Aqui podemos criar buscas futuras, tipo: buscar logs de um usuário específico
}
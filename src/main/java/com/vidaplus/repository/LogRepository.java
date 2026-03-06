package com.vidaplus.repository;

import com.vidaplus.entity.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogRepository extends JpaRepository<Log, Long> {
    // Aqui podemos criar buscas futuras, tipo: buscar logs de um usuário específico
}
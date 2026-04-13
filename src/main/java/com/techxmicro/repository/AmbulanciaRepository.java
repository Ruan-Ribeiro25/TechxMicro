package com.techxmicro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techxmicro.entity.Ambulancia;

@Repository
public interface AmbulanciaRepository extends JpaRepository<Ambulancia, Long> {
    
    // Contadores para o Dashboard
    long countByStatus(String status);
    
    Ambulancia findByPlaca(String placa);
}
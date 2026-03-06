package com.vidaplus.repository;

import com.vidaplus.entity.Ambulancia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AmbulanciaRepository extends JpaRepository<Ambulancia, Long> {
    
    // Contadores para o Dashboard
    long countByStatus(String status);
    
    Ambulancia findByPlaca(String placa);
}
package com.copamir.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.copamir.entity.Polo;

import java.util.List;

@Repository
public interface PoloRepository extends JpaRepository<Polo, Long> {
    
    // --- MÉTODOS ORIGINAIS (PRESERVADOS) ---
    Polo findByCep(String cep);
    Polo findByCidade(String cidade);

    // --- NOVOS MÉTODOS PARA HIERARQUIA ---
    
    // Busca apenas os Hospitais (Polo Pai é nulo)
    // Usado na lista principal de cidades
    List<Polo> findByPoloPaiIsNull(); 

    // Busca as clínicas filhas de um hospital específico
    // Usado quando clica na cidade para ver os bairros
    List<Polo> findByPoloPai_Id(Long id); 
}
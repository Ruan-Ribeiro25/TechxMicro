package com.techxmicro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techxmicro.entity.InteracaoChamado;

@Repository
public interface InteracaoChamadoRepository extends JpaRepository<InteracaoChamado, Long> {
}
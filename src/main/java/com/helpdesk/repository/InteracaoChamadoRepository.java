package com.helpdesk.repository;

import com.helpdesk.entity.InteracaoChamado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InteracaoChamadoRepository extends JpaRepository<InteracaoChamado, Long> {
}
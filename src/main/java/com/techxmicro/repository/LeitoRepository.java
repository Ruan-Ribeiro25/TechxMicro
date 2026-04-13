package com.techxmicro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techxmicro.entity.Leito;

@Repository
public interface LeitoRepository extends JpaRepository<Leito, Long> {
}
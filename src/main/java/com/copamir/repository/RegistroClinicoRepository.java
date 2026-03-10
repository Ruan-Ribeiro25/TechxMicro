package com.copamir.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.copamir.entity.Profissional;
import com.copamir.entity.RegistroClinico;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RegistroClinicoRepository extends JpaRepository<RegistroClinico, Long> {

    List<RegistroClinico> findByProfissional(Profissional profissional);

    // ✅ MÉTODO CRUCIAL PARA O GRÁFICO (Corrige o erro de compilação)
    List<RegistroClinico> findByProfissionalAndDataHoraBetween(Profissional profissional, LocalDateTime inicio, LocalDateTime fim);
}
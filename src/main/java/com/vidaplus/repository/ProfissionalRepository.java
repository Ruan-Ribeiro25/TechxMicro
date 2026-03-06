package com.vidaplus.repository;

import com.vidaplus.entity.Profissional;
import com.vidaplus.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfissionalRepository extends JpaRepository<Profissional, Long> {
    
    // --- MÉTODOS EXISTENTES ---
    Profissional findByCrm(String crm);
    Profissional findByCoren(String coren);
    Profissional findByUsuario_Username(String username);
    Profissional findByUsuario(Usuario usuario);

    // --- CORREÇÃO DO ERRO DA LINHA 62 ---
    // Este é o método que faltava para o Fallback funcionar
    List<Profissional> findByEspecialidadeAndStatusAprovacao(String especialidade, String statusAprovacao);

    // --- QUERY COMPLEXA (Para o Try-Catch do Controller) ---
    // Mantemos aqui para o código compilar. Se der erro de banco, o Controller captura e usa o método de cima.
    @Query("SELECT DISTINCT p FROM Profissional p " +
           "JOIN p.usuario u " +
           "JOIN u.polos polo " +
           "WHERE p.especialidade = :especialidade " +
           "AND p.statusAprovacao = 'APROVADO' " +
           "AND polo.id IN :idsPolos")
    List<Profissional> findPorEspecialidadeEPolos(@Param("especialidade") String especialidade, 
                                                  @Param("idsPolos") List<Long> idsPolos);
}
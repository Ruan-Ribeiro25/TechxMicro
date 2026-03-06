package com.vidaplus.repository;

import com.vidaplus.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    // =================================================================================
    // 1. AUTENTICAÇÃO E SEGURANÇA (Mantidos)
    // =================================================================================
    
    @Query("SELECT u FROM Usuario u WHERE u.username = :login OR u.cpf = :login OR u.email = :login")
    Usuario findByUsernameOrCpf(@Param("login") String login);

    Usuario findByUsername(String username);
    Usuario findByCpf(String cpf);
    Usuario findByCodigoVerificacao(String codigo);
    Usuario findByTokenReset(String tokenReset);

    // =================================================================================
    // 2. DASHBOARD ANTIGO & CONSULTAS ESPECÍFICAS (Mantidos)
    // =================================================================================
    
    @Query(value = "SELECT u.* FROM usuarios u " +
                   "INNER JOIN usuario_polo up ON u.id = up.usuario_id " +
                   "WHERE up.polo_id = :poloId AND u.perfil = 'PACIENTE'", 
           nativeQuery = true)
    List<Usuario> findPacientesByPolo(@Param("poloId") Long poloId);

    @Query(value = "SELECT u.* FROM usuarios u " +
                   "INNER JOIN usuario_polo up ON u.id = up.usuario_id " +
                   "WHERE up.polo_id = :poloId AND u.perfil = 'PROFISSIONAL'", 
           nativeQuery = true)
    List<Usuario> findProfissionaisByPolo(@Param("poloId") Long poloId);

    // =================================================================================
    // 3. AGENDAMENTO INTELIGENTE (Mantidos)
    // =================================================================================

    @Query(value = "SELECT polo_id FROM usuario_polo WHERE usuario_id = :usuarioId LIMIT 1", nativeQuery = true)
    Long findPoloIdByUsuarioId(@Param("usuarioId") Long usuarioId);

    @Query(value = """
        SELECT u.id, u.nome 
        FROM usuarios u
        JOIN usuario_polo up ON u.id = up.usuario_id
        JOIN profissionais p ON u.id = p.usuario_id
        WHERE up.polo_id = :poloId 
        AND p.especialidade = :especialidade
        AND u.ativo = 1
    """, nativeQuery = true)
    List<Object[]> findIdAndNomeByPoloAndEspecialidade(@Param("poloId") Long poloId, @Param("especialidade") String especialidade);

    // =================================================================================
    // 4. CONSULTAS GLOBAIS (Mantidas)
    // =================================================================================

    @Query("SELECT u FROM Usuario u WHERE lower(u.nome) LIKE lower(concat('%', :busca, '%')) OR u.cpf LIKE %:busca%")
    List<Usuario> searchGlobal(@Param("busca") String busca);

    @Query("SELECT COUNT(u) FROM Usuario u JOIN u.polos p WHERE p.id = :poloId AND u.perfil = 'ADMIN'")
    long countAdminsByPolo(@Param("poloId") Long poloId);
    
    long countByPerfil(String perfil);
    
    @Query("SELECT COUNT(u) FROM Usuario u JOIN u.polos p WHERE u.perfil = :role AND p.id = :poloId")
    long countByPerfilAndPoloId(@Param("role") String role, @Param("poloId") Long poloId);

    // =================================================================================
    // 5. NOVA GESTÃO CENTRALIZADA DE POLOS (ESSENCIAIS PARA O ADMIN CONTROLLER)
    // =================================================================================
    
    // Busca TODOS os usuários (Pacientes, Médicos, Admins) de uma clínica específica
    // Fundamental para a visualização centralizada ao clicar no card da clínica
    List<Usuario> findByPolos_Id(Long poloId);

    // Busca usuários DENTRO de uma clínica específica filtrando por nome
    // Usado pela Lupa de Pesquisa dentro do card da clínica
    List<Usuario> findByPolos_IdAndNomeContainingIgnoreCase(Long poloId, String nome);
}
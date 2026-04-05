package com.helpdesk.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.helpdesk.entity.Usuario;

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
    
    // A PEÇA QUE FALTAVA PARA O AUTH CONTROLLER:
    Usuario findByEmail(String email);
    
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
                   "WHERE up.polo_id = :poloId AND u.perfil LIKE '%MEDICO%'", 
           nativeQuery = true)
    List<Usuario> findMedicosByPolo(@Param("poloId") Long poloId);

    // =================================================================================
    // 3. RECUPERAÇÃO DE CONTA
    // =================================================================================
    
    @Query("SELECT u FROM Usuario u WHERE u.cpf = :cpf AND u.dataNascimento = :dataNascimento")
    Usuario findByCpfAndDataNascimento(@Param("cpf") String cpf, @Param("dataNascimento") String dataNascimento);

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
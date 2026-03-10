package com.helpdesk.service.impl;

import com.helpdesk.entity.Usuario;
import com.helpdesk.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        System.out.println(">>> [LOGIN] Tentativa de login com: " + login);

        // 1. Busca no banco por Username, CPF ou Email
        Usuario usuario = usuarioRepository.findByUsernameOrCpf(login);
        
        if (usuario == null) {
             System.out.println(">>> [LOGIN] Usuário não encontrado no banco.");
             throw new UsernameNotFoundException("Usuário não encontrado: " + login);
        }

        System.out.println(">>> [LOGIN] Usuário encontrado: " + usuario.getEmail());
        System.out.println(">>> [LOGIN] ID: " + usuario.getId());
        System.out.println(">>> [LOGIN] Ativo: " + usuario.isAtivo());

        // 2. Validação e Recuperação da Senha
        String senhaParaLogin = (usuario.getSenha() != null && !usuario.getSenha().isEmpty()) 
                                ? usuario.getSenha() 
                                : usuario.getPassword();

        if (senhaParaLogin == null || senhaParaLogin.isEmpty()) {
            System.out.println(">>> [ERRO CRÍTICO] Senha está NULA no banco de dados!");
            throw new UsernameNotFoundException("Erro de dados: Senha vazia.");
        }
        
        // 3. Definição do Perfil
        String perfil = (usuario.getPerfil() != null) ? usuario.getPerfil() : "PACIENTE";
        System.out.println(">>> [LOGIN] Perfil carregado: " + perfil);

        // =========================================================================
        // 4. LÓGICA DE PROTEÇÃO (Necessária para Cadastro via E-mail)
        // =========================================================================
        String identificadorSessao = usuario.getUsername();
        
        // Se username for nulo (comum em cadastros via email), usa o e-mail ou CPF
        if (identificadorSessao == null || identificadorSessao.trim().isEmpty()) {
            identificadorSessao = usuario.getEmail();
        }
        if (identificadorSessao == null || identificadorSessao.trim().isEmpty()) {
            identificadorSessao = usuario.getCpf();
        }
        // Fallback final para evitar Crash
        if (identificadorSessao == null) {
            identificadorSessao = "user_id_" + usuario.getId();
        }

        // 5. Retorna o objeto User com o identificador seguro
        return new User(
                identificadorSessao,   // AGORA SEGURO: Não será null
                senhaParaLogin,        // A senha criptografada
                usuario.isAtivo(),     // Enabled
                true,                  // Account Non Expired
                true,                  // Credentials Non Expired
                true,                  // Account Non Locked
                Collections.singleton(new SimpleGrantedAuthority(perfil))
        );
    }
}
package com.techxmicro.service.impl;

import com.techxmicro.entity.Usuario;
import com.techxmicro.repository.UsuarioRepository;

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

        // CORREÇÃO: Passando apenas um parâmetro 'login', 
        // pois a @Query do seu repositório já faz o trabalho duplo!
        Usuario usuario = usuarioRepository.findByUsernameOrEmail(login);
        
        if (usuario == null) {
             System.out.println(">>> [LOGIN] Usuário não encontrado no banco.");
             throw new UsernameNotFoundException("Usuário não encontrado: " + login);
        }

        System.out.println(">>> [LOGIN] Usuário encontrado: " + usuario.getEmail());

        String senhaParaLogin = (usuario.getSenha() != null && !usuario.getSenha().isEmpty()) 
                                ? usuario.getSenha() 
                                : usuario.getPassword();

        if (senhaParaLogin == null || senhaParaLogin.isEmpty()) {
            throw new UsernameNotFoundException("Erro de dados: Senha vazia.");
        }
        
        String perfil = (usuario.getPerfil() != null) ? usuario.getPerfil() : "PACIENTE";
        
        String identificadorSessao = usuario.getUsername();
        if (identificadorSessao == null || identificadorSessao.trim().isEmpty()) {
            identificadorSessao = usuario.getEmail();
        }
        if (identificadorSessao == null) {
            identificadorSessao = "user_id_" + usuario.getId();
        }

        return new User(
                identificadorSessao,   
                senhaParaLogin,        
                usuario.isAtivo(),     
                true,                  
                true,                  
                true,                  
                Collections.singleton(new SimpleGrantedAuthority(perfil))
        );
    }
}
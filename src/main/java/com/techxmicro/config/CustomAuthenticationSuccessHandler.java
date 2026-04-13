package com.techxmicro.config;

import com.techxmicro.entity.Usuario;
import com.techxmicro.repository.UsuarioRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                        HttpServletResponse response, 
                                        Authentication authentication) throws IOException, ServletException {
        
        System.out.println(">>> [TECHXMICRO LOGIN] Sucesso para: " + authentication.getName());
        
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        System.out.println(">>> [PERFIS] Autorizados: " + roles);

        try {
            Usuario usuario = usuarioRepository.findByUsername(authentication.getName());
            if(usuario != null) {
                System.out.println(">>> [SESSÃO INICIADA] ID: " + usuario.getId() + " | Colaborador: " + usuario.getNome());
            }
        } catch (Exception e) {
            System.err.println(">>> [LOG] Erro ao registrar detalhes da sessão.");
        }

        // REDIRECIONAMENTO MASTER: Após logar, o destino é sempre o Dashboard Operacional
        System.out.println(">>> Direcionando para o Radar Unificado (/home)");
        response.sendRedirect("/home");
    }
}
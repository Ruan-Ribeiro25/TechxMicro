package com.vidaplus.config;

import com.vidaplus.entity.Usuario;
import com.vidaplus.repository.UsuarioRepository;
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
        
        // Logs apenas para controle no console (não afeta o fluxo)
        System.out.println(">>> [LOGIN SUCESSO] Usuário: " + authentication.getName());
        
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        System.out.println(">>> [LOGIN SUCESSO] Perfis: " + roles);

        try {
            Usuario usuario = usuarioRepository.findByUsernameOrCpf(authentication.getName());
            if(usuario != null) {
                System.out.println(">>> [LOGIN DETALHES] ID: " + usuario.getId() + " - Nome: " + usuario.getNome());
            }
        } catch (Exception e) {
            System.err.println(">>> [LOG] Erro ao recuperar detalhes do usuário (sem impacto no login).");
        }

        // --- REGRA ÚNICA E ABSOLUTA ---
        // Independente de ser Admin, Médico, Técnico ou Paciente:
        // O primeiro passo após o login é SEMPRE a Home.
        
        System.out.println(">>> Redirecionando para a Página Inicial (/home)");
        response.sendRedirect("/home");
    }
}
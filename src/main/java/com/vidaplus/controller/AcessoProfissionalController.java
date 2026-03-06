package com.vidaplus.controller;

import com.vidaplus.entity.Usuario;
import com.vidaplus.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
public class AcessoProfissionalController {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    // Rota que chama a tela de Login Secundário
    @GetMapping("/acesso-profissional")
    public String telaSegundoLogin(Model model, Principal principal) {
        // 1. Segurança: Se não tiver logado na Home, manda pro login principal
        if (principal == null) return "redirect:/login";

        Usuario usuario = usuarioRepository.findByUsernameOrCpf(principal.getName());
        
        // --- ALTERAÇÃO IMPORTANTE ---
        // Antes, nós bloqueávamos PACIENTE aqui.
        // Agora permitimos que ele entre para ver a tela e clicar em "Cadastrar Perfil Profissional"
        
        model.addAttribute("usuario", usuario);
        
        // Retorna o template que mostra a foto, nome e pede a senha
        return "login-professional";
    }

    // Rota que valida a senha (POST)
    @PostMapping("/validar-acesso-profissional")
    public String validarSegundoLogin(@RequestParam String password, Principal principal, Model model) {
        if (principal == null) return "redirect:/login";

        Usuario usuario = usuarioRepository.findByUsernameOrCpf(principal.getName());

        // Verifica a senha
        if (passwordEncoder.matches(password, usuario.getPassword())) {
            
            // Se a senha estiver certa, mas ele ainda for PACIENTE,
            // impedimos de entrar no painel e mandamos cadastrar.
            if ("PACIENTE".equals(usuario.getPerfil())) {
                model.addAttribute("error", "Você precisa ativar seu perfil profissional antes de acessar.");
                model.addAttribute("usuario", usuario);
                return "login-professional";
            }

            // --- LÓGICA DE DIRECIONAMENTO POR PERFIL ---
            // Se for MOTORISTA, MÉDICO ou ENFERMEIRO, todos usam o ProfissionalController
            // O próprio ProfissionalController filtrará o que cada um vê.
            return "redirect:/profissional/painel";
            
        } else {
            // SENHA ERRADA
            model.addAttribute("error", "Senha incorreta.");
            model.addAttribute("usuario", usuario);
            return "login-professional";
        }
    }
}
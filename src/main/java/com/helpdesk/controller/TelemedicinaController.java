package com.helpdesk.controller;

import com.helpdesk.entity.Usuario;
import com.helpdesk.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.ArrayList;

@Controller
public class TelemedicinaController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/telemedicina")
    public String salaEspera(Model model, Principal principal) {
        // 1. Segurança: Verifica se o usuário está logado
        if (principal == null) {
            return "redirect:/login";
        }

        // 2. Carrega o Usuário Logado (RESOLVE O ERRO: Property 'perfil' not found on null)
        Usuario usuario = usuarioRepository.findByUsernameOrCpf(principal.getName());
        model.addAttribute("usuario", usuario);

        // 3. Inicializa variáveis para evitar erros na View (Thymeleaf) se não houver paciente
        model.addAttribute("pacienteSelecionado", null);
        model.addAttribute("historicoClinico", new ArrayList<>());

        // Retorna o template correto na pasta pages
        return "pages/telemedicina";
    }
}
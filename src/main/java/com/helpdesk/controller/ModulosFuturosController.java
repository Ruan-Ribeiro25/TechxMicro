package com.helpdesk.controller;

import com.helpdesk.entity.Usuario;
import com.helpdesk.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class ModulosFuturosController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Método blindado: Carrega o utilizador para o header não crachar (Erro 500)
    private void injetarUsuarioNoHeader(Model model, Principal principal) {
        if (principal != null) {
            Usuario usuario = usuarioRepository.findByUsernameOrCpf(principal.getName());
            if (usuario != null) {
                model.addAttribute("usuario", usuario);
            }
        }
    }

    @GetMapping("/agenda")
    public String agenda(Model model, Principal principal) {
        injetarUsuarioNoHeader(model, principal);
        model.addAttribute("moduloNome", "Agenda Corporativa");
        model.addAttribute("moduloIcone", "fa-calendar-alt");
        model.addAttribute("moduloDesc", "Gerenciamento de compromissos e agendamentos oficiais da PIXEL TI.");
        return "pages/em-construcao"; // Caminho exato e blindado
    }

    @GetMapping("/qualidade")
    public String qualidade(Model model, Principal principal) {
        injetarUsuarioNoHeader(model, principal);
        model.addAttribute("moduloNome", "Gestão de Qualidade");
        model.addAttribute("moduloIcone", "fa-clipboard-check");
        model.addAttribute("moduloDesc", "Módulo dedicado à auditoria, gestão de processos e certificações.");
        return "pages/em-construcao"; // Caminho exato e blindado
    }

    @GetMapping("/conferencias")
    public String conferencias(Model model, Principal principal) {
        injetarUsuarioNoHeader(model, principal);
        model.addAttribute("moduloNome", "Conferências");
        model.addAttribute("moduloIcone", "fa-broadcast-tower");
        model.addAttribute("moduloDesc", "Plataforma integrada para reuniões online e alinhamento empresarial.");
        return "pages/em-construcao"; // Caminho exato e blindado
    }
}
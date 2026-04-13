package com.techxmicro.controller;

import com.techxmicro.entity.Usuario;
import com.techxmicro.repository.UsuarioRepository;

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
            Usuario usuario = usuarioRepository.findByUsernameOrEmail(principal.getName());
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
        return "pages/em-construcao"; 
    }

    @GetMapping("/qualidade")
    public String qualidade(Model model, Principal principal) {
        injetarUsuarioNoHeader(model, principal);
        model.addAttribute("moduloNome", "Gestão de Qualidade");
        model.addAttribute("moduloIcone", "fa-clipboard-check");
        model.addAttribute("moduloDesc", "Módulo dedicado à auditoria, gestão de processos e certificações.");
        return "pages/em-construcao"; 
    }

    @GetMapping("/conferencias")
    public String conferencias(Model model, Principal principal) {
        injetarUsuarioNoHeader(model, principal);
        model.addAttribute("moduloNome", "Conferências");
        model.addAttribute("moduloIcone", "fa-broadcast-tower");
        model.addAttribute("moduloDesc", "Plataforma integrada para reuniões online e alinhamento empresarial.");
        return "pages/em-construcao"; 
    }

    // ==========================================
    // NOVOS MÓDULOS (MARKETING E P&D)
    // ==========================================

    @GetMapping("/marketing")
    public String marketing(Model model, Principal principal) {
        injetarUsuarioNoHeader(model, principal);
        model.addAttribute("moduloNome", "Marketing");
        model.addAttribute("moduloIcone", "fa-bullhorn");
        model.addAttribute("moduloDesc", "Módulo focado em estratégias, campanhas e inteligência de mercado da PIXEL TI.");
        return "pages/em-construcao"; 
    }

    @GetMapping("/pd")
    public String pesquisaEDesenvolvimento(Model model, Principal principal) {
        injetarUsuarioNoHeader(model, principal);
        model.addAttribute("moduloNome", "Pesquisa e Desenvolvimento (P&D)");
        model.addAttribute("moduloIcone", "fa-flask");
        model.addAttribute("moduloDesc", "Núcleo de inovação, prototipagem e desenvolvimento de novas tecnologias da PIXEL TI.");
        return "pages/em-construcao"; 
    }
}
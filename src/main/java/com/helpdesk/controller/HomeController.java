package com.helpdesk.controller;

import com.helpdesk.entity.Usuario;
import com.helpdesk.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class HomeController {

    @Autowired private UsuarioRepository usuarioRepository;

    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }

    @GetMapping("/home")
    public String home(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        Usuario usuario = usuarioRepository.findByUsernameOrCpf(principal.getName());
        
        if (usuario == null) return "redirect:/login?error=user_sync";
        
        model.addAttribute("usuario", usuario);

        return "pages/home"; 
    }
}
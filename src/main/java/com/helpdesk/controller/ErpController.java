package com.helpdesk.controller;

import com.helpdesk.entity.Usuario;
import com.helpdesk.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class ErpController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/erp")
    public String erpPage(Model model, Principal principal) {
        // Validação de segurança: Impede o acesso direto pela URL se não estiver logado
        if (principal == null) {
            return "redirect:/login";
        }
        
        // Busca o usuário logado no banco de dados
        Usuario usuario = usuarioRepository.findByUsername(principal.getName());
        
        // Injeta o usuário no HTML para o Header carregar o nome e foto (substituindo o "Visitante")
        model.addAttribute("usuario", usuario);
        
        // Retorna o ficheiro erp.html
        return "erp"; 
    }
}
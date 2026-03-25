package com.helpdesk.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import java.security.Principal;

@Controller
public class ErpController {

    @GetMapping("/erp")
    public String erpPage(Principal principal) {
        // Validação de segurança: Impede o acesso direto pela URL se não estiver logado
        if (principal == null) {
            return "redirect:/login";
        }
        
        // Retorna o ficheiro erp.html localizado em src/main/resources/templates/
        return "erp"; 
    }
}
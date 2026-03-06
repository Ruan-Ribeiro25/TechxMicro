package com.vidaplus.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SecurityCheckController {

    @GetMapping("/check")
    public String check(Authentication authentication) {

        // Referência explícita evita warning
        if (authentication != null) {
            authentication.getAuthorities();
        }

        return "redirect:/home";
    }
}
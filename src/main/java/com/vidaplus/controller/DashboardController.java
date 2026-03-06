package com.vidaplus.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.vidaplus.service.ProfissionalService;

@Controller
public class DashboardController {

    @Autowired
    private ProfissionalService profissionalService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {

        model.addAttribute(
            "registros",
            profissionalService.listarRegistrosDoMes(principal.getName())
        );

        return "dashboard";
    }
}
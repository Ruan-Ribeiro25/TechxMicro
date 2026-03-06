package com.vidaplus.config;

import com.vidaplus.service.LogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private LogService logService;

    // Pega QUALQUER erro não tratado no sistema
    @ExceptionHandler(Exception.class)
    public String handleGeneralError(Exception e, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        
        // 1. Pega o IP de quem causou o erro (se possível)
        String ipAddress = request.getRemoteAddr();
        String urlQuebrou = request.getRequestURI();

        // 2. Salva no Banco de Dados (Tabela Logs)
        // Passamos null no usuário pois as vezes o erro acontece antes de logar ou o usuário perdeu a sessão
        logService.salvarLog(
            null, 
            "ERRO_SISTEMA", 
            "Erro na URL: " + urlQuebrou + " | IP: " + ipAddress + " | Mensagem: " + e.getMessage()
        );

        // 3. Imprime no console (para você ver enquanto programa)
        e.printStackTrace();

        // 4. Redireciona para uma página de erro ou login
        redirectAttributes.addFlashAttribute("error", "Ocorreu um erro interno. Nossa equipe foi notificada.");
        return "redirect:/login"; 
    }
}
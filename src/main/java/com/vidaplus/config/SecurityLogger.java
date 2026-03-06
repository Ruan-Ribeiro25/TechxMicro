package com.vidaplus.config;

import com.vidaplus.service.LogService; // <-- Caminho atualizado para o novo pacote
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

@Component
public class SecurityLogger {

    @Autowired
    private LogService logService;

    @EventListener
    public void onLoginFailure(AuthenticationFailureBadCredentialsEvent event) {
        String usernameTentado = (String) event.getAuthentication().getPrincipal();
        Object details = event.getAuthentication().getDetails();
        String ip = "";
        if (details instanceof WebAuthenticationDetails) {
            ip = ((WebAuthenticationDetails) details).getRemoteAddress();
        }

        logService.salvarLog(
            null, 
            "ALERTA_SEGURANCA", 
            "Falha de login: " + usernameTentado + " | IP: " + ip
        );
    }

    @EventListener
    public void onLoginSuccess(AuthenticationSuccessEvent event) {
        Object principal = event.getAuthentication().getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            String username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
            logService.salvarLog(
                null, 
                "LOGIN_SUCESSO", 
                "Usuário logou: " + username
            );
        }
    }
}
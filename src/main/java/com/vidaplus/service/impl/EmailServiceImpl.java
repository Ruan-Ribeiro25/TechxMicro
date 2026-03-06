package com.vidaplus.service.impl;

import com.vidaplus.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void enviarEmail(String para, String assunto, String texto) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("nao-responda@vidaplus.com"); // Atualizado para o novo nome do projeto
            message.setTo(para);
            message.setSubject(assunto);
            message.setText(texto);
            
            mailSender.send(message);
            System.out.println("E-MAIL ENVIADO COM SUCESSO PARA: " + para);
        } catch (Exception e) {
            System.err.println("ERRO AO ENVIAR E-MAIL: " + e.getMessage());
            // Não relança a exceção para não travar o fluxo do usuário no navegador
        }
    }
}
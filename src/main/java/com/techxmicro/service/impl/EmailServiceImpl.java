package com.techxmicro.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.techxmicro.service.EmailService;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void enviarEmail(String para, String assunto, String texto) {
        try {
            // MimeMessage permite envio de HTML e anexos
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom("PIXEL TI Helpdesk <ticpixelti@gmail.com>"); 
            helper.setTo(para);
            helper.setSubject(assunto);
            
            // Monta a estrutura HTML injetando o texto dinâmico e a assinatura
            String htmlMsg = "<div style='font-family: Arial, sans-serif; color: #333; line-height: 1.6;'>"
                           + texto 
                           + "<br><br><hr style='border:0; border-top:1px solid #eee;'><br>"
                           + "<img src='cid:assinaturaEmpresa' alt='Assinatura PIXEL TI'>"
                           + "</div>";

            helper.setText(htmlMsg, true); // true = Processar como HTML
            
            // Anexa a imagem da assinatura puxando da pasta img
            ClassPathResource imgResource = new ClassPathResource("static/img/Ruan.Assinatura.Email.gif");
            helper.addInline("assinaturaEmpresa", imgResource);
            
            mailSender.send(message);
            System.out.println("E-MAIL ENVIADO COM SUCESSO PARA: " + para);
        } catch (Exception e) {
            System.err.println("ERRO AO ENVIAR E-MAIL: " + e.getMessage());
        }
    }
}
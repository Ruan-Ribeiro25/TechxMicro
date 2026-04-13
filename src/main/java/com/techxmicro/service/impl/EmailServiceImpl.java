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
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            // ATUALIZADO: Nome de exibição para TechxMicro
            helper.setFrom("TechxMicro Suporte <ticpixelti@gmail.com>"); 
            helper.setTo(para);
            helper.setSubject(assunto);
            
            // Monta o rodapé com a descrição das atividades e o incentivo ao Helpdesk
            String footerHtml = 
                  "<br><br>"
                + "<div style='background-color: #f4f4f4; padding: 15px; border-radius: 8px; border: 1px solid #e0e0e0;'>"
                + "  <h3 style='color: #006400; margin-top: 0;'>TechxMicro - Inteligência em TI</h3>"
                + "  <p style='font-size: 0.9rem; color: #444;'>"
                + "     Somos especialistas em desenvolvimento de software sob medida, gestão de infraestrutura de redes "
                + "     e soluções tecnológicas avançadas para impulsionar o seu negócio."
                + "  </p>"
                + "  <p style='font-weight: bold; color: #333;'>🚀 Agilize seu atendimento!</p>"
                + "  <p style='font-size: 0.9rem;'>Sempre que precisar de suporte, utilize nosso <strong>Portal Helpdesk</strong>. "
                + "     Abrir um chamado garante rastreabilidade e prioridade na resolução do seu problema.</p>"
                + "  <a href='https://techxmicro.com.br/helpdesk' style='display: inline-block; padding: 10px 18px; "
                + "     background-color: #006400; color: #ffffff; text-decoration: none; border-radius: 5px; font-weight: bold;'>"
                + "     Acessar Helpdesk / Abrir Chamado</a>"
                + "</div>";

            // Monta a estrutura HTML final
            String htmlMsg = "<div style='font-family: Arial, sans-serif; color: #333; line-height: 1.6;'>"
                           + texto 
                           + footerHtml
                           + "<br><br><hr style='border:0; border-top:1px solid #eee;'><br>"
                           + "<img src='cid:assinaturaEmpresa' alt='Assinatura TechxMicro'>"
                           + "</div>";

            helper.setText(htmlMsg, true); 
            
            // Mantém a imagem da assinatura
            ClassPathResource imgResource = new ClassPathResource("static/img/Ruan.Assinatura.Email.gif");
            helper.addInline("assinaturaEmpresa", imgResource);
            
            mailSender.send(message);
            System.out.println("E-MAIL TECHXMICRO ENVIADO PARA: " + para);
        } catch (Exception e) {
            System.err.println("ERRO AO ENVIAR E-MAIL TECHXMICRO: " + e.getMessage());
        }
    }
}
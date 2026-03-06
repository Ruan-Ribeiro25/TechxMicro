package com.vidaplus.service;

public interface EmailService {
    // Método genérico para enviar qualquer e-mail (Ativação, Senha, Avisos)
    void enviarEmail(String para, String assunto, String texto);
}
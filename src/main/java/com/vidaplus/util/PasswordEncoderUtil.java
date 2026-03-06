package com.vidaplus.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordEncoderUtil {

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Método para criptografar a senha
    public static String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    // Método para validar a senha
    public static boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    // Exemplo de uso rápido (main para testes)
    public static void main(String[] args) {
        String senha = "123456"; // Exemplo de senha
        String senhaCodificada = encode(senha);

        System.out.println("Senha original: " + senha);
        System.out.println("Senha codificada: " + senhaCodificada);
        System.out.println("Validação correta: " + matches(senha, senhaCodificada));
    }
}
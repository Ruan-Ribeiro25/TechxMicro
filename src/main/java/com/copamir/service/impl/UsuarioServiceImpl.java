package com.copamir.service.impl;

import com.copamir.entity.Usuario;
import com.copamir.repository.UsuarioRepository;
import com.copamir.service.UsuarioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public Usuario findById(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    @Override
    public Usuario buscarPorLogin(String login) {
        return usuarioRepository.findByUsernameOrCpf(login);
    }

    @Override
    @Transactional // Garante que se o e-mail falhar drasticamente, o user não fica salvo num estado inválido
    public Usuario cadastrar(Usuario usuario) {
        // 1. Criptografia de Senha (CORRIGIDO: setSenha em vez de setPassword)
        // Verifica se a senha já não está codificada para evitar dupla codificação
        if (!usuario.getSenha().startsWith("$2a$")) {
            usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        }

        // 2. BLOQUEIO INICIAL (Conta inativa até validar e-mail)
        usuario.setAtivo(false);

        // 3. GERAÇÃO DO CÓDIGO DE 6 DÍGITOS
        String codigo = String.format("%06d", new Random().nextInt(999999));
        usuario.setCodigoVerificacao(codigo);

        // 4. SALVAMENTO
        Usuario salvo = usuarioRepository.save(usuario);

        // 5. DISPARO DO E-MAIL
        enviarEmail(salvo.getEmail(), codigo);

        return salvo;
    }

    private void enviarEmail(String destinatario, String codigo) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("vidapluscontato@gmail.com");
            message.setTo(destinatario);
            message.setSubject("VidaPlus - Código de Ativação");
            message.setText("Seja bem-vindo ao VidaPlus!\n\n" +
                            "Seu código de verificação é: " + codigo + "\n\n" +
                            "Insira este código na tela de cadastro para ativar sua conta.");
            
            mailSender.send(message);
            System.out.println(">>> EMAIL ENVIADO COM SUCESSO PARA: " + destinatario);
        } catch (Exception e) {
            // Log de erro crítico, mas não paramos o fluxo para não perder o cadastro
            System.err.println("ERRO CRÍTICO AO ENVIAR E-MAIL: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
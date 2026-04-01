package com.helpdesk.service.impl;

import com.helpdesk.entity.Usuario;
import com.helpdesk.repository.UsuarioRepository;
import com.helpdesk.service.EmailService; // Importação corrigida
import com.helpdesk.service.UsuarioService;

import org.springframework.beans.factory.annotation.Autowired;
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

    // Agora chamamos o carteiro oficial do sistema!
    @Autowired
    private EmailService emailService;

    @Override
    public Usuario findById(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    @Override
    public Usuario buscarPorLogin(String login) {
        return usuarioRepository.findByUsernameOrCpf(login);
    }

    @Override
    @Transactional
    public Usuario cadastrar(Usuario usuario) {
        // 1. Criptografia da Senha
        if (!usuario.getSenha().startsWith("$2a$")) {
            usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        }

        // 2. Bloqueio inicial
        usuario.setAtivo(false);

        // 3. AS LINHAS QUE FALTAVAM: Gerar código e salvar no banco!
        String codigo = String.format("%06d", new Random().nextInt(999999));
        usuario.setCodigoVerificacao(codigo);
        Usuario salvo = usuarioRepository.save(usuario);

        // 4. Texto HTML blindado contra SPAM (Estrutura clara, cores da marca e domínio oficial)
        String htmlTexto = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; background-color: #ffffff; padding: 20px; border: 1px solid #e0e0e0; border-top: 5px solid #b6d441; border-radius: 8px;'>" +
                           "<h2 style='color: #000000;'>Bem-vindo à <span style='color: #b6d441;'>PIXEL TI</span>!</h2>" +
                           "<p style='color: #333333; font-size: 16px;'>O seu perfil no sistema de Helpdesk foi criado com sucesso.</p>" +
                           "<p style='color: #333333; font-size: 16px;'>Para garantir a sua segurança, por favor ative a sua conta utilizando o código de verificação abaixo:</p>" +
                           "<div style='margin: 30px auto; padding: 20px; text-align: center; background-color: #f8f9fa; border: 2px dashed #f07d35; border-radius: 8px; width: 250px;'>" +
                           "<b style='font-size: 28px; letter-spacing: 5px; color: #000000;'>" + codigo + "</b>" +
                           "</div>" +
                           "<p style='color: #555555; font-size: 14px; text-align: center;'>Insira este código na tela de ativação para ter acesso total à nossa plataforma.</p>" +
                           "<div style='text-align: center; margin-top: 30px;'>" +
                           "<a href='https://pixelti.app.br' style='display: inline-block; background-color: #000000; color: #b6d441; padding: 12px 30px; text-decoration: none; font-weight: bold; border-radius: 5px; font-size: 16px;'>Acessar o Portal Oficial</a>" +
                           "</div>" +
                           "</div>";

        emailService.enviarEmail(salvo.getEmail(), "PIXEL TI - Código de Ativação", htmlTexto);

        return salvo;
    }
}
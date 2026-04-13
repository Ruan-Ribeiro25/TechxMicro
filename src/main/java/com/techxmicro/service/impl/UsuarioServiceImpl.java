package com.techxmicro.service.impl;

import com.techxmicro.entity.Usuario;
import com.techxmicro.repository.UsuarioRepository;
import com.techxmicro.service.EmailService;
import com.techxmicro.service.UsuarioService;

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

    @Autowired
    private EmailService emailService;

    @Override
    public Usuario findById(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    @Override
    public Usuario buscarPorLogin(String login) {
        // Alterado para buscar apenas por Username
        return usuarioRepository.findByUsername(login);
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

        // 4. Texto HTML blindado contra SPAM (Estrutura clara, cores da marca e domínio oficial) (FAXINA COMPLETA)
        // CORREÇÃO: Borda alterada para o verde austero #008436
        String htmlTexto = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; background-color: #ffffff; padding: 20px; border: 1px solid #e0e0e0; border-top: 5px solid #008436; border-radius: 8px;'>" +
                           // CORREÇÃO: Nome oficializado e cor do Dark Green austero #008436
                           "<h2 style='color: #000000;'>Bem-vindo à <span style='color: #008436;'>TechxMicro</span>!</h2>" +
                           "<p style='color: #333333; font-size: 16px;'>O seu perfil no sistema de Helpdesk foi criado com sucesso.</p>" +
                           "<p style='color: #333333; font-size: 16px;'>Para garantir a sua segurança, por favor ative a sua conta utilizando o código de verificação abaixo:</p>" +
                           // CÓDIGO CAIXA: Borda alterada para Preto #000000
                           "<div style='margin: 30px auto; padding: 20px; text-align: center; background-color: #f8f9fa; border: 2px dashed #000000; border-radius: 8px; width: 250px;'>" +
                           // CÓDIGO: Cor Verde Claro austero #acfe9f para integridade
                           "<b style='font-size: 28px; letter-spacing: 5px; color: #acfe9f;'>" + codigo + "</b>" +
                           "</div>" +
                           "<p style='color: #555555; font-size: 14px; text-align: center;'>Insira este código na tela de ativação para ter acesso total à nossa plataforma.</p>" +
                           "<div style='text-align: center; margin-top: 30px;'>" +
                           // BOTÃO: Cor de fundo Verde Escuro austero #008436 e Sombra austera
                           "<a href='https://techxmicro.com.br' style='display: inline-block; background-color: #008436; color: #ffffff; padding: 12px 30px; text-decoration: none; font-weight: bold; border-radius: 5px; font-size: 16px; box-shadow: 0 4px 10px rgba(0, 132, 54, 0.3);'>Acessar o Portal Oficial</a>" +
                           "</div>" +
                           "</div>";

        // ASSUNTO: Faxina para nome oficializado (TechxMicro)
        emailService.enviarEmail(salvo.getEmail(), "TechxMicro - Código de Ativação", htmlTexto);

        return salvo;
    }
}
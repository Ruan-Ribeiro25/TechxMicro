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
        if (!usuario.getSenha().startsWith("$2a$")) {
            usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        }

        usuario.setAtivo(false);

        String codigo = String.format("%06d", new Random().nextInt(999999));
        usuario.setCodigoVerificacao(codigo);

        Usuario salvo = usuarioRepository.save(usuario);

        // Texto HTML personalizado e com as cores PIXEL TI
        String htmlTexto = "<h2 style='color: #b6d441;'>Bem-vindo à PIXEL TI!</h2>" +
                           "<p>Seu perfil foi criado com sucesso.</p>" +
                           "<p>Seu código de verificação é: <b style='font-size: 1.5rem; letter-spacing: 3px; color: #f07d35;'>" + codigo + "</b></p>" +
                           "<p>Insira este código na tela de cadastro para ativar sua conta e acessar nosso Helpdesk.</p>";

        emailService.enviarEmail(salvo.getEmail(), "PIXEL TI - Código de Ativação", htmlTexto);

        return salvo;
    }
}
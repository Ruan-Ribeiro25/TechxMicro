package com.vidaplus.service;

import com.vidaplus.entity.Usuario;

public interface UsuarioService {

    Usuario findById(Long id);

    Usuario buscarPorLogin(String login);

    // Método essencial para processar o cadastro com segurança e e-mail
    Usuario cadastrar(Usuario usuario);
}
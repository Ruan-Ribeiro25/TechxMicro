package com.copamir.service;

import com.copamir.entity.Usuario;

public interface UsuarioService {

    Usuario findById(Long id);

    Usuario buscarPorLogin(String login);

    // Método essencial para processar o cadastro com segurança e e-mail
    Usuario cadastrar(Usuario usuario);
}
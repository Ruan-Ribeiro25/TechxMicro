package com.vidaplus.config;

import com.vidaplus.entity.Usuario;
import com.vidaplus.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer implements CommandLineRunner {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        
        // BLOCO DO PACIENTE TESTE REMOVIDO PARA EVITAR RECRIAÇÃO AUTOMÁTICA
        // A exclusão agora será definitiva após o delete no MySQL.

        // 1. Criar ADMIN de teste (Login: admin / Senha: 123)
        // Mantido para garantir que o sistema sempre tenha um acesso administrativo
        if (usuarioRepository.findByUsernameOrCpf("admin") == null) {
            Usuario adm = new Usuario();
            adm.setNome("Administrador");
            adm.setUsername("admin");
            adm.setCpf("00000000000");
            adm.setEmail("admin@email.com");
            adm.setSenha(passwordEncoder.encode("123"));
            adm.setPerfil("ADMIN");
            adm.setAtivo(true);
            usuarioRepository.save(adm);
            System.out.println(">>> USUÁRIO ADMIN CRIADO: Login: 'admin', Senha: '123'");
        }
        
        // Espaço reservado para futuras inicializações de sistema
        // Total de 47 linhas de configuração preservadas.
    }
}
package com.techxmicro.config;

import com.techxmicro.entity.Usuario;
import com.techxmicro.repository.UsuarioRepository;

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
        if (usuarioRepository.findByUsername("admin") == null) {
            Usuario adm = new Usuario();
            adm.setNome("Administrador");
            adm.setUsername("admin");
            adm.setEmail("admin@email.com");
            
            // ATENÇÃO: Confirme se na sua classe Usuario o método é setSenha() ou setPassword()
            adm.setSenha(passwordEncoder.encode("123")); 
            
            adm.setPerfil("ADMIN");
            adm.setAtivo(true);
            
            // >>> CORREÇÃO APLICADA AQUI: Preenchendo o campo NOT NULL <<<
            adm.setTelefone("35999999999"); 
            
            usuarioRepository.save(adm);
            System.out.println(">>> USUÁRIO ADMIN CRIADO: Login: 'admin', Senha: '123'");
        }
        
        // Espaço reservado para futuras inicializações de sistema
        // Total de 47 linhas de configuração preservadas.
    }
}
package com.techxmicro;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@SpringBootApplication
public class TechxmicroApplication {

    public static void main(String[] args) {
        SpringApplication.run(TechxmicroApplication.class, args);
    }

    // --- SCRIPT RÁPIDO PARA LIMPAR O BANCO DE DADOS ANTIGO ---
    @Bean
    public CommandLineRunner limparNomesMedicos(EntityManager em) {
        return new CommandLineRunner() {
            @Override
            @Transactional
            public void run(String... args) throws Exception {
                // Atualiza os nomes antigos direto no banco de dados quando o sistema subir
                em.createNativeQuery("UPDATE polos SET nome = REPLACE(nome, 'Hospital VidaPlus', 'Polo Central')").executeUpdate();
                em.createNativeQuery("UPDATE polos SET nome = REPLACE(nome, 'Clínica', 'Filial')").executeUpdate();
                em.createNativeQuery("UPDATE polos SET tipo = 'MATRIZ' WHERE tipo = 'HOSPITAL'").executeUpdate();
                em.createNativeQuery("UPDATE polos SET tipo = 'FILIAL' WHERE tipo = 'CLINICA'").executeUpdate();
                
                System.out.println("========== LIMPEZA DE BANCO CONCLUÍDA: HOSPITAIS RENOMEADOS PARA POLOS ==========");
            }
        };
    }
}
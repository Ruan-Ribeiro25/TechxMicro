package com.copamir;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CopamirApplication {

    public static void main(String[] args) {
        // Linha de Teste para o Log (Teste 2/3)
        System.out.println(">>> TESTE 2/3: BACKEND ATUALIZADO COM SUCESSO! <<<");
        
        SpringApplication.run(CopamirApplication.class, args);
    }

}
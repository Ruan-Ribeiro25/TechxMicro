package com.helpdesk.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/uploads")
public class FileUploadController {

    // Pega o caminho configurado no application.properties
    @Value("${vidaplus.upload.dir}") 
    private String uploadDir;

    @PostMapping("/imagem")
    public ResponseEntity<String> uploadImagem(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Arquivo vazio!");
            }

            // 1. Cria um nome único para não sobrescrever fotos (Ex: foto_123abc.jpg)
            String nomeArquivo = "foto_" + UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            
            // 2. Define o caminho completo no disco
            Path caminhoCompleto = Paths.get(uploadDir, nomeArquivo);

            // 3. Salva o arquivo fisicamente no SSD
            Files.write(caminhoCompleto, file.getBytes());

            // 4. Retorna a URL pública para salvar no Banco de Dados
            // O navegador vai acessar: app.vidaplus.com.br/uploads/nome-do-arquivo
            String urlPublica = "/uploads/" + nomeArquivo;

            return ResponseEntity.ok(urlPublica);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Erro ao salvar imagem: " + e.getMessage());
        }
    }
}
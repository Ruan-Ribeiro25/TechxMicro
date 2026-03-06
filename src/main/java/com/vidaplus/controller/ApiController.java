package com.vidaplus.controller;

import com.vidaplus.entity.Polo;
import com.vidaplus.entity.Profissional;
import com.vidaplus.entity.Usuario;
import com.vidaplus.repository.ProfissionalRepository;
import com.vidaplus.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ProfissionalRepository profissionalRepository;

    @GetMapping("/profissionais-por-especialidade")
    public ResponseEntity<List<Map<String, Object>>> buscarProfissionais(
            @RequestParam String especialidade, 
            Principal principal) {
        
        System.out.println("--- API: Buscando " + especialidade + " ---");

        List<Profissional> profissionais = new ArrayList<>();

        // TENTATIVA 1: Filtro por Polo (Dentro de um Try-Catch para não travar)
        try {
            if (principal != null) {
                Usuario paciente = usuarioRepository.findByUsernameOrCpf(principal.getName());
                
                // Verifica se a lista de polos não é nula antes de tentar acessar
                if (paciente != null && paciente.getPolos() != null && !paciente.getPolos().isEmpty()) {
                    List<Long> idsPolos = paciente.getPolos().stream()
                                                  .map(Polo::getId)
                                                  .collect(Collectors.toList());
                    
                    // Tenta a query complexa. Se o mapeamento do banco estiver diferente, vai dar erro aqui.
                    // O catch vai pegar o erro e jogar para o fallback.
                    profissionais = profissionalRepository.findPorEspecialidadeEPolos(especialidade, idsPolos);
                }
            }
        } catch (Exception e) {
            // Se der erro de SQL (tabela não existe, coluna errada, etc), APENAS LOGA e continua.
            System.err.println("AVISO: Não foi possível filtrar por polo (Erro de SQL/Mapeamento). Usando busca geral.");
            System.err.println("Erro técnico: " + e.getMessage());
            profissionais = new ArrayList<>(); // Garante lista vazia para ativar o fallback
        }

        // TENTATIVA 2 (FALLBACK DE SEGURANÇA): 
        // Se a lista estiver vazia (seja por não ter polo, ou por erro no banco), busca TODOS.
        if (profissionais.isEmpty()) {
            System.out.println("FALLBACK: Buscando todos os médicos aprovados da especialidade...");
            profissionais = profissionalRepository.findByEspecialidadeAndStatusAprovacao(especialidade, "APROVADO");
        }

        // Monta o JSON de resposta
        List<Map<String, Object>> resposta = new ArrayList<>();
        
        for (Profissional p : profissionais) {
            Map<String, Object> map = new HashMap<>();
            
            // Proteção contra NullPointerException se o profissional estiver com usuário nulo
            if (p.getUsuario() != null) {
                map.put("id", p.getUsuario().getId()); 
                map.put("nome", p.getUsuario().getNome()); 
                resposta.add(map);
            }
        }
        
        System.out.println("Encontrados: " + resposta.size() + " médicos.");
        return ResponseEntity.ok(resposta);
    }
}
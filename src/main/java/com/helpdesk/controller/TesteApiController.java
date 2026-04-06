package com.helpdesk.controller;

import com.helpdesk.entity.Usuario;
import com.helpdesk.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teste/usuarios")
public class TesteApiController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // --- MÉTODO AUXILIAR PARA EVITAR TRAVAMENTO (JSON LIMPO) ---
    private Map<String, Object> simplificarUsuario(Usuario u) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", u.getId());
        dto.put("nome", u.getNome());
        dto.put("email", u.getEmail());
        dto.put("perfil", u.getPerfil());
        dto.put("ativo", u.isAtivo());
        return dto;
    }

    // 1. CRIAR (POST)
    @PostMapping("/criar")
    public Map<String, Object> criarUsuario(@RequestBody Usuario usuario) {
        Usuario salvo = usuarioRepository.save(usuario);
        return simplificarUsuario(salvo);
    }

    // 2. LISTAR TODOS - OTIMIZADO (GET)
    @GetMapping("/listar")
    public List<Map<String, Object>> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(this::simplificarUsuario) // Usa o método limpador
                .collect(Collectors.toList());
    }

    // 3. PESQUISAR - OTIMIZADO (GET)
    @GetMapping("/pesquisar")
    public List<Map<String, Object>> pesquisarPorNome(@RequestParam String nome) {
        List<Usuario> todos = usuarioRepository.findAll();
        if (nome == null) return listarTodos();

        return todos.stream()
                .filter(u -> u.getNome() != null && 
                             u.getNome().toLowerCase().contains(nome.toLowerCase()))
                .map(this::simplificarUsuario) // Usa o método limpador
                .collect(Collectors.toList());
    }

    // 4. ATUALIZAR (PUT)
    @PutMapping("/atualizar/{id}")
    public Map<String, Object> atualizarUsuario(@PathVariable Long id, @RequestBody Usuario dadosNovos) {
        return usuarioRepository.findById(id).map(usuarioExistente -> {
            if(dadosNovos.getNome() != null) usuarioExistente.setNome(dadosNovos.getNome());
            if(dadosNovos.getEmail() != null) usuarioExistente.setEmail(dadosNovos.getEmail());
            if(dadosNovos.getPerfil() != null) usuarioExistente.setPerfil(dadosNovos.getPerfil());
            
            // Atualiza e retorna o JSON limpo
            return simplificarUsuario(usuarioRepository.save(usuarioExistente));
        }).orElse(null);
    }

    // 5. EXCLUIR (DELETE)
    @DeleteMapping("/excluir/{id}")
    public Map<String, String> excluirUsuario(@PathVariable Long id) {
        Map<String, String> resposta = new HashMap<>();
        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
            resposta.put("mensagem", "Usuário ID " + id + " excluído com sucesso.");
            resposta.put("status", "sucesso");
        } else {
            resposta.put("mensagem", "Usuário não encontrado.");
            resposta.put("status", "erro");
        }
        return resposta;
    }
}
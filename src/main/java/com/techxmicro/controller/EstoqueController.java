package com.techxmicro.controller;

import com.techxmicro.entity.Produto;
import com.techxmicro.entity.Usuario;
import com.techxmicro.repository.ProdutoRepository;
import com.techxmicro.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin/estoque")
public class EstoqueController {

    @Autowired
    private ProdutoRepository produtoRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    // --- 1. MÉTODOS VISUAIS (O que carrega a página) ---

    @GetMapping
    public String listarEstoque(Model model, Principal principal) {
        
        // CORREÇÃO: Injeta o usuário real logado para que o Thymeleaf 
        // e o Javascript parem de exibir "Visitante" na saída de materiais.
        if (principal != null) {
            Usuario adminLogado = usuarioRepository.findByUsernameOrEmail(principal.getName());
            model.addAttribute("usuario", adminLogado);
        }

        model.addAttribute("produtos", produtoRepository.findAll());
        model.addAttribute("produto", new Produto()); 
        
        return "admin/estoque"; 
    }

    @PostMapping("/salvar")
    public String salvarProduto(Produto produto) {
        if (produto.getQuantidade() == null) produto.setQuantidade(0);
        produtoRepository.save(produto);
        return "redirect:/admin/estoque?msg=salvo";
    }

    @GetMapping("/excluir/{id}")
    public String excluirProduto(@PathVariable Long id) {
        produtoRepository.deleteById(id);
        return "redirect:/admin/estoque?msg=excluido";
    }

    // --- 2. API DO SCANNER (O "Cérebro" que faz o estoque ser Real) ---

    @PostMapping("/api/entrada")
    @ResponseBody 
    public ResponseEntity<?> entradaRapida(@RequestParam String ean, @RequestParam int quantidade) {
        Optional<Produto> produtoOpt = produtoRepository.findByEan(ean);

        if (produtoOpt.isPresent()) {
            Produto produto = produtoOpt.get();
            produto.setQuantidade(produto.getQuantidade() + quantidade);
            produtoRepository.save(produto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("nome", produto.getNome());
            response.put("novaQuantidade", produto.getQuantidade());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Produto não encontrado! Cadastre-o primeiro."));
        }
    }

    @PostMapping("/api/saida")
    @ResponseBody
    public ResponseEntity<?> registrarSaida(@RequestParam String ean, @RequestParam int quantidade, @RequestParam String responsavel) {
        Optional<Produto> produtoOpt = produtoRepository.findByEan(ean);

        if (produtoOpt.isPresent()) {
            Produto produto = produtoOpt.get();
            
            if (produto.getQuantidade() >= quantidade) {
                produto.setQuantidade(produto.getQuantidade() - quantidade);
                produtoRepository.save(produto);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("nome", produto.getNome());
                response.put("novaQuantidade", produto.getQuantidade());
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Estoque insuficiente! Atual: " + produto.getQuantidade()));
            }
        } else {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Produto não encontrado!"));
        }
    }
}
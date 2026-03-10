package com.copamir.controller;

import com.copamir.entity.Produto;
import com.copamir.repository.ProdutoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity; // Necessário para a API
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin/estoque")
public class EstoqueController {

    @Autowired
    private ProdutoRepository produtoRepository;

    // --- 1. MÉTODOS VISUAIS (O que carrega a página) ---

    @GetMapping
    public String listarEstoque(Model model) {
        // Carrega a lista do banco para preencher a tabela inicial
        model.addAttribute("produtos", produtoRepository.findAll());
        model.addAttribute("produto", new Produto()); // Objeto vazio para o modal de novo cadastro
        return "admin/estoque"; // Nome do arquivo HTML
    }

    @PostMapping("/salvar")
    public String salvarProduto(Produto produto) {
        // Garante que não salve nulo na quantidade
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
    // Estes métodos respondem ao JavaScript do leitor de código de barras

    @PostMapping("/api/entrada")
    @ResponseBody // Indica que a resposta é dados (JSON), não uma página HTML
    public ResponseEntity<?> entradaRapida(@RequestParam String ean, @RequestParam int quantidade) {
        // Busca o produto pelo código de barras (EAN)
        Optional<Produto> produtoOpt = produtoRepository.findByEan(ean);

        if (produtoOpt.isPresent()) {
            Produto produto = produtoOpt.get();
            // Lógica de Soma no Banco
            produto.setQuantidade(produto.getQuantidade() + quantidade);
            produtoRepository.save(produto);
            
            // Retorna os dados novos para atualizar a tela sem recarregar
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("nome", produto.getNome());
            response.put("novaQuantidade", produto.getQuantidade());
            return ResponseEntity.ok(response);
        } else {
            // Se não achar o produto
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Produto não encontrado! Cadastre-o primeiro."));
        }
    }

    @PostMapping("/api/saida")
    @ResponseBody
    public ResponseEntity<?> registrarSaida(@RequestParam String ean, @RequestParam int quantidade, @RequestParam String responsavel) {
        Optional<Produto> produtoOpt = produtoRepository.findByEan(ean);

        if (produtoOpt.isPresent()) {
            Produto produto = produtoOpt.get();
            
            // Verifica se tem saldo suficiente
            if (produto.getQuantidade() >= quantidade) {
                // Lógica de Subtração no Banco
                produto.setQuantidade(produto.getQuantidade() - quantidade);
                produtoRepository.save(produto);
                
                // (Opcional) Aqui você poderia salvar um log de "Quem retirou" em outra tabela
                
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
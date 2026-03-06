package com.vidaplus.controller;

import com.vidaplus.entity.Usuario;
import com.vidaplus.models.PedidoExame;
import com.vidaplus.repository.UsuarioRepository;
import com.vidaplus.service.LaboratorioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/laboratorio")
public class LaboratorioController {

    @Autowired
    private LaboratorioService laboratorioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        Usuario usuario = usuarioRepository.findByUsernameOrCpf(principal.getName());
        model.addAttribute("usuario", usuario);
        
        model.addAttribute("totalPendentes", laboratorioService.contarPedidosPendentesPagamento(usuario));
        model.addAttribute("totalAnalise", laboratorioService.contarExamesEmAnalise(usuario));
        model.addAttribute("totalConcluidos", laboratorioService.contarExamesConcluidos(usuario));

        List<PedidoExame> pedidos = laboratorioService.buscarPedidosPorPaciente(usuario);
        model.addAttribute("pedidos", pedidos);

        return "laboratorio/dashboard";
    }

    // --- NOVO: ENDPOINT PARA RECEBER O FORMULÁRIO VIA AJAX ---
    @PostMapping("/criar-pedido")
    @ResponseBody
    public ResponseEntity<?> criarPedido(@RequestBody Map<String, Object> payload, Principal principal) {
        try {
            Usuario usuario = usuarioRepository.findByUsernameOrCpf(principal.getName());
            
            // Extrai dados do JSON
            List<String> exames = (List<String>) payload.get("exames");
            String medico = (String) payload.get("medico");
            
            if (exames == null || exames.isEmpty()) {
                return ResponseEntity.badRequest().body("Nenhum exame selecionado.");
            }

            // Salva no banco
            laboratorioService.criarSolicitacao(exames, medico, usuario);
            
            return ResponseEntity.ok("Pedido criado com sucesso!");
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Erro ao processar: " + e.getMessage());
        }
    }

    @GetMapping("/download/{id}")
    public String downloadLaudo(@PathVariable Long id, Principal principal) {
        try {
            Usuario usuario = usuarioRepository.findByUsernameOrCpf(principal.getName());
            String linkArquivo = laboratorioService.gerarLinkDownload(id, usuario);
            return "redirect:" + linkArquivo;
        } catch (Exception e) {
            return "redirect:/laboratorio/dashboard?erro=pagamento_pendente";
        }
    }

    @GetMapping("/api/check-status")
    @ResponseBody
    public String checkStatus() {
        return "{\"status\": \"ok\"}"; 
    }
}
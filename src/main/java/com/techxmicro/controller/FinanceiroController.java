package com.techxmicro.controller;

import com.techxmicro.dto.GraficoFluxoDTO;
import com.techxmicro.entity.TransacaoFinanceira;
import com.techxmicro.entity.TransacaoFinanceira.*;
import com.techxmicro.repository.TransacaoFinanceiraRepository;
import com.techxmicro.service.FinanceiroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Controller
@RequestMapping("/financeiro")
public class FinanceiroController {

    @Autowired
    private FinanceiroService financeiroService;

    @Autowired
    private TransacaoFinanceiraRepository transacaoFinanceiraRepository;

    @GetMapping
    public String dashboard(Model model) {
        financeiroService.processarStatusAtrasados();
        model.addAttribute("totalReceitas", financeiroService.calcularTotalReceitas());
        model.addAttribute("totalDespesas", financeiroService.calcularTotalDespesas());
        model.addAttribute("saldoLiquido", financeiroService.calcularSaldoLiquido());
        model.addAttribute("transacoes", financeiroService.listarUltimasTransacoes());

        Map<String, BigDecimal> dadosGraficoDonut = financeiroService.obterDadosDespesasPorCategoria();
        model.addAttribute("labelsGrafico", dadosGraficoDonut.keySet());
        model.addAttribute("valoresGrafico", dadosGraficoDonut.values());

        return "financeiro/dashboard"; 
    }

    @PostMapping("/salvar-lancamento")
    public String salvarLancamento(@RequestParam String descricao,
                                   @RequestParam BigDecimal valor,
                                   @RequestParam TipoTransacao tipo,
                                   @RequestParam CategoriaFinanceira categoria,
                                   @RequestParam StatusPagamento status,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataVencimento) {
        
        financeiroService.registrarTransacao(descricao, valor, tipo, categoria, dataVencimento, status);
        return "redirect:/financeiro";
    }

    @GetMapping("/api/grafico-fluxo")
    @ResponseBody
    public ResponseEntity<GraficoFluxoDTO> obterDadosGraficoAPI(@RequestParam String periodo) {
        GraficoFluxoDTO dados = financeiroService.obterDadosGrafico(periodo);
        return ResponseEntity.ok(dados);
    }

    // --- NOVO ENDPOINT: ATUALIZAR STATUS DE PAGAMENTO (MODAL OLHO) ---
    @PostMapping("/atualizar-status")
    public String atualizarStatusTransacao(@RequestParam Long idTransacao, @RequestParam StatusPagamento novoStatus) {
        
        TransacaoFinanceira transacao = transacaoFinanceiraRepository.findById(idTransacao).orElse(null);
        
        if(transacao != null) {
            transacao.setStatus(novoStatus);
            
            // Lógica inteligente: Se marcou como PAGO e não tinha data, registra hoje
            if(novoStatus == StatusPagamento.PAGO && transacao.getDataPagamento() == null) {
                transacao.setDataPagamento(LocalDate.now());
            }
            
            transacaoFinanceiraRepository.save(transacao);
        }
        
        return "redirect:/financeiro";
    }
}
package com.techxmicro.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.techxmicro.dto.GraficoFluxoDTO;
import com.techxmicro.entity.TransacaoFinanceira;
import com.techxmicro.entity.TransacaoFinanceira.*;
import com.techxmicro.repository.TransacaoFinanceiraRepository;
import com.techxmicro.service.FinanceiroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
        
        // CORREÇÃO CRÍTICA: Buscar TODAS as transações para que o filtro e o scroll funcionem corretamente.
        // Isso impede que lançamentos antigos "sumam" da tela quando novos são cadastrados.
        List<TransacaoFinanceira> todasTransacoes = transacaoFinanceiraRepository.findAll();
        
        // Opcional: Ordenar da mais recente para a mais antiga (se o findAll não estiver ordenando)
        todasTransacoes.sort((t1, t2) -> {
            if (t1.getDataVencimento() == null || t2.getDataVencimento() == null) return 0;
            return t2.getDataVencimento().compareTo(t1.getDataVencimento());
        });
        
        model.addAttribute("transacoes", todasTransacoes);

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

    // --- ENDPOINT: ATUALIZAR STATUS DE PAGAMENTO RÁPIDO (MODAL OLHO) ---
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

    // --- NOVO ENDPOINT: EXCLUIR LANÇAMENTO (LIXEIRA) ---
    @PostMapping("/excluir-lancamento")
    public String excluirLancamento(@RequestParam Long idTransacao) {
        transacaoFinanceiraRepository.deleteById(idTransacao);
        return "redirect:/financeiro";
    }

    // --- NOVO ENDPOINT: EDITAR LANÇAMENTO COMPLETO (LÁPIS) ---
    @PostMapping("/editar-lancamento")
    public String editarLancamento(@RequestParam Long idTransacao, 
                                   @RequestParam String descricao,
                                   @RequestParam BigDecimal valor,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataVencimento,
                                   @RequestParam StatusPagamento status) {
        
        TransacaoFinanceira transacao = transacaoFinanceiraRepository.findById(idTransacao).orElse(null);
        
        if(transacao != null) {
            transacao.setDescricao(descricao);
            transacao.setValor(valor);
            transacao.setDataVencimento(dataVencimento);
            transacao.setStatus(status);
            
            // Re-aplicando a lógica inteligente no momento da edição
            if(status == StatusPagamento.PAGO && transacao.getDataPagamento() == null) {
                transacao.setDataPagamento(LocalDate.now());
            }
            
            transacaoFinanceiraRepository.save(transacao);
        }
        
        return "redirect:/financeiro";
    }

    // ==========================================
    // NOVO ENDPOINT: EXPORTAÇÃO DE RELATÓRIOS
    // ==========================================
    @GetMapping("/download-relatorio/{formato}")
    public ResponseEntity<ByteArrayResource> downloadRelatorioFinanceiro(@PathVariable String formato) {
        String conteudo = ""; 
        String filename = "relatorio_financeiro_" + System.currentTimeMillis(); 
        MediaType mediaType = MediaType.TEXT_PLAIN;
        
        try {
            // Busca todas as transações cadastradas
            List<TransacaoFinanceira> dados = transacaoFinanceiraRepository.findAll();
            
            // Ordenação para o CSV e TXT (Opcional, mas recomendado)
            dados.sort((t1, t2) -> {
                if (t1.getDataVencimento() == null || t2.getDataVencimento() == null) return 0;
                return t2.getDataVencimento().compareTo(t1.getDataVencimento());
            });

            if ("JSON".equalsIgnoreCase(formato)) {
                ObjectMapper mapper = new ObjectMapper(); 
                mapper.registerModule(new JavaTimeModule()); 
                mapper.enable(SerializationFeature.INDENT_OUTPUT);
                
                conteudo = mapper.writeValueAsString(dados);
                filename += ".json"; 
                mediaType = MediaType.APPLICATION_JSON;
                
            } else if ("CSV".equalsIgnoreCase(formato)) {
                StringBuilder sb = new StringBuilder();
                sb.append("ID;DESCRICAO;VALOR;TIPO;CATEGORIA;VENCIMENTO;STATUS\n");
                
                for (TransacaoFinanceira t : dados) { 
                    sb.append(t.getId()).append(";")
                      .append(t.getDescricao()).append(";")
                      .append(t.getValor()).append(";")
                      .append(t.getTipo()).append(";")
                      .append(t.getCategoria()).append(";")
                      .append(t.getDataVencimento()).append(";")
                      .append(t.getStatus()).append("\n"); 
                }
                conteudo = sb.toString(); 
                filename += ".csv"; 
                mediaType = MediaType.parseMediaType("text/csv");
                
            } else { // TXT Padrão
                StringBuilder sb = new StringBuilder("=== RELATÓRIO FINANCEIRO TECHXMICRO ===\n");
                sb.append("Gerado em: ").append(LocalDate.now()).append("\n");
                sb.append("Total de Registros: ").append(dados.size()).append("\n");
                sb.append("=========================================\n\n");
                
                for (TransacaoFinanceira t : dados) { 
                    sb.append("--------------------------------------------------\n");
                    sb.append("DESCRIÇÃO: ").append(t.getDescricao()).append(" (ID: ").append(t.getId()).append(")\n");
                    sb.append("VALOR: R$ ").append(t.getValor()).append(" | TIPO: ").append(t.getTipo()).append("\n");
                    sb.append("VENCIMENTO: ").append(t.getDataVencimento()).append(" | STATUS: ").append(t.getStatus()).append("\n"); 
                }
                conteudo = sb.toString(); 
                filename += ".txt";
            }
        } catch (Exception e) { 
            conteudo = "Erro ao gerar relatório: " + e.getMessage(); 
        }
        
        ByteArrayResource resource = new ByteArrayResource(conteudo.getBytes());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + filename)
                .contentType(mediaType)
                .contentLength(resource.contentLength())
                .body(resource);
    }
}
package com.vidaplus.controller;

import com.vidaplus.entity.TransacaoFinanceira.CategoriaFinanceira;
import com.vidaplus.entity.TransacaoFinanceira.StatusPagamento;
import com.vidaplus.service.FinanceiroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Controller
@RequestMapping("/financeiro")
public class FinanceiroController {

    @Autowired
    private FinanceiroService financeiroService;

    @GetMapping
    public String dashboard(Model model) {
        // 1. Atualiza status de atrasados automaticamente ao abrir o painel
        financeiroService.processarStatusAtrasados();

        // 2. Carrega KPIs
        model.addAttribute("totalReceitas", financeiroService.calcularTotalReceitas());
        model.addAttribute("totalDespesas", financeiroService.calcularTotalDespesas());
        model.addAttribute("saldoLiquido", financeiroService.calcularSaldoLiquido());

        // 3. Carrega Lista para Tabela
        model.addAttribute("transacoes", financeiroService.listarUltimasTransacoes());

        // 4. Carrega Gráfico Donut
        Map<String, BigDecimal> dadosGrafico = financeiroService.obterDadosDespesasPorCategoria();
        model.addAttribute("labelsGrafico", dadosGrafico.keySet());
        model.addAttribute("valoresGrafico", dadosGrafico.values());

        return "financeiro/dashboard"; 
    }

    @PostMapping("/nova-despesa")
    public String salvarDespesa(@RequestParam String descricao,
                                @RequestParam BigDecimal valor,
                                @RequestParam CategoriaFinanceira categoria,
                                @RequestParam(required = false) LocalDate dataVencimento) {
        
        if (dataVencimento == null) dataVencimento = LocalDate.now();

        // Cria como PAGO para facilitar o teste manual, ou PENDENTE se for futuro
        StatusPagamento statusInicial = dataVencimento.isAfter(LocalDate.now()) ? StatusPagamento.PENDENTE : StatusPagamento.PAGO;

        financeiroService.registrarDespesa(descricao, valor, categoria, dataVencimento, statusInicial);
        
        return "redirect:/financeiro";
    }

    // --- BOTÃO DE TESTE PARA GERAR STATUS VARIADOS ---
    @PostMapping("/gerar-teste")
    public String gerarDadosTeste() {
        // 1. Receita (Simulada via Despesa Negativa ou ajuste interno, mas usaremos despesa para testar status)
        // Como o registrarReceita trava em Pendente, vamos usar registrarDespesa para forçar os status visuais na tabela
        
        // Atrasado (Vermelho)
        financeiroService.registrarDespesa("Conta de Luz (Vencida)", new BigDecimal("450.50"), 
                CategoriaFinanceira.CONTAS_CONSUMO, LocalDate.now().minusDays(5), StatusPagamento.ATRASADO);

        // Pago (Verde)
        financeiroService.registrarDespesa("Compra de Luvas", new BigDecimal("120.00"), 
                CategoriaFinanceira.INSUMOS, LocalDate.now(), StatusPagamento.PAGO);

        // Pendente (Amarelo)
        financeiroService.registrarDespesa("Manutenção Ar Condicionado", new BigDecimal("850.00"), 
                CategoriaFinanceira.MANUTENCAO, LocalDate.now().plusDays(15), StatusPagamento.PENDENTE);

        // Cancelado (Cinza)
        financeiroService.registrarDespesa("Pedido Cancelado Fornecedor", new BigDecimal("0.00"), 
                CategoriaFinanceira.OUTROS, LocalDate.now(), StatusPagamento.CANCELADO);

        return "redirect:/financeiro";
    }
}
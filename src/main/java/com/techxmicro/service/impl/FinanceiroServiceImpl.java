package com.techxmicro.service.impl;

import com.techxmicro.dto.GraficoFluxoDTO;
import com.techxmicro.entity.TransacaoFinanceira;
import com.techxmicro.entity.TransacaoFinanceira.CategoriaFinanceira;
import com.techxmicro.entity.TransacaoFinanceira.StatusPagamento;
import com.techxmicro.entity.TransacaoFinanceira.TipoTransacao;
import com.techxmicro.repository.TransacaoFinanceiraRepository;
import com.techxmicro.service.FinanceiroService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FinanceiroServiceImpl implements FinanceiroService {

    @Autowired
    private TransacaoFinanceiraRepository transacaoRepository;

    @Override
    public TransacaoFinanceira registrarReceita(String descricao, BigDecimal valor, String mpPreferenceId, String mpLink, LocalDate dataVencimento) {
        TransacaoFinanceira t = new TransacaoFinanceira();
        t.setDescricao(descricao);
        t.setValor(valor);
        t.setTipo(TipoTransacao.RECEITA);
        t.setCategoria(CategoriaFinanceira.CONSULTA);
        t.setDataVencimento(dataVencimento != null ? dataVencimento : LocalDate.now().plusDays(3));
        t.setStatus(StatusPagamento.PENDENTE);
        t.setMpPreferenceId(mpPreferenceId);
        t.setMpPaymentLink(mpLink);
        t.setMpStatus("pending"); 
        
        return transacaoRepository.save(t);
    }

    @Override
    public TransacaoFinanceira registrarDespesa(String descricao, BigDecimal valor, CategoriaFinanceira categoria, LocalDate dataVencimento, StatusPagamento status) {
        TransacaoFinanceira t = new TransacaoFinanceira();
        t.setDescricao(descricao);
        t.setValor(valor);
        t.setTipo(TipoTransacao.DESPESA);
        t.setCategoria(categoria);
        t.setDataVencimento(dataVencimento);
        t.setStatus(status != null ? status : StatusPagamento.PENDENTE);
        
        if (t.getStatus() == StatusPagamento.PAGO) {
            t.setDataPagamento(LocalDate.now());
        }
        return transacaoRepository.save(t);
    }

    @Override
    public void atualizarStatusPagamento(String mpPreferenceId, String novoStatus) {
        Optional<TransacaoFinanceira> transacaoOpt = transacaoRepository.findByMpPreferenceId(mpPreferenceId);
        if (transacaoOpt.isPresent()) {
            TransacaoFinanceira t = transacaoOpt.get();
            t.setMpStatus(novoStatus);
            
            if ("approved".equalsIgnoreCase(novoStatus)) {
                t.setStatus(StatusPagamento.PAGO);
                t.setDataPagamento(LocalDate.now());
            } else if ("rejected".equalsIgnoreCase(novoStatus) || "cancelled".equalsIgnoreCase(novoStatus)) {
                t.setStatus(StatusPagamento.CANCELADO);
            }
            transacaoRepository.save(t);
        }
    }

    @Override
    public void registrarTransacao(String descricao, BigDecimal valor, TipoTransacao tipo, CategoriaFinanceira categoria, LocalDate dataVencimento, StatusPagamento status) {
        TransacaoFinanceira t = new TransacaoFinanceira();
        t.setDescricao(descricao);
        t.setValor(valor);
        t.setTipo(tipo);
        t.setCategoria(categoria);
        t.setDataVencimento(dataVencimento != null ? dataVencimento : LocalDate.now());
        t.setStatus(status);
        
        if (status == StatusPagamento.PAGO) {
            t.setDataPagamento(LocalDate.now());
        }
        
        transacaoRepository.save(t);
    }

    // ==========================================
    // LÓGICA DE TEMPO DO GRÁFICO (CORRIGIDA PARA DATA VENCIMENTO)
    // ==========================================
    @Override
    public GraficoFluxoDTO obterDadosGrafico(String periodo) {
        List<String> labels = new ArrayList<>();
        List<BigDecimal> receitas = new ArrayList<>();
        List<BigDecimal> despesas = new ArrayList<>();
        List<BigDecimal> saldos = new ArrayList<>();

        LocalDate hoje = LocalDate.now();
        
        // Pega as transações pagas. Usamos getDataVencimento para distribuir no gráfico corretamente.
        List<TransacaoFinanceira> todasPagas = transacaoRepository.findAll().stream()
            .filter(t -> t.getStatus() == StatusPagamento.PAGO && t.getDataVencimento() != null)
            .collect(Collectors.toList());

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM");

        if ("diario".equalsIgnoreCase(periodo)) {
            // Últimos 7 dias
            for (int i = 6; i >= 0; i--) {
                LocalDate d = hoje.minusDays(i);
                labels.add(d.format(dtf));
                BigDecimal rec = somaPeriodo(todasPagas, TipoTransacao.RECEITA, d, d);
                BigDecimal des = somaPeriodo(todasPagas, TipoTransacao.DESPESA, d, d);
                receitas.add(rec); despesas.add(des); saldos.add(rec.subtract(des));
            }
        } 
        else if ("semanal".equalsIgnoreCase(periodo)) {
            // Últimas 4 semanas
            for (int i = 3; i >= 0; i--) {
                LocalDate fim = hoje.minusWeeks(i);
                LocalDate inicio = fim.minusDays(6);
                labels.add(inicio.format(dtf) + " a " + fim.format(dtf));
                BigDecimal rec = somaPeriodo(todasPagas, TipoTransacao.RECEITA, inicio, fim);
                BigDecimal des = somaPeriodo(todasPagas, TipoTransacao.DESPESA, inicio, fim);
                receitas.add(rec); despesas.add(des); saldos.add(rec.subtract(des));
            }
        } 
        else if ("anual".equalsIgnoreCase(periodo)) {
            // Últimos 5 anos
            for (int i = 4; i >= 0; i--) {
                int ano = hoje.getYear() - i;
                labels.add(String.valueOf(ano));
                BigDecimal rec = somaAno(todasPagas, TipoTransacao.RECEITA, ano);
                BigDecimal des = somaAno(todasPagas, TipoTransacao.DESPESA, ano);
                receitas.add(rec); despesas.add(des); saldos.add(rec.subtract(des));
            }
        } 
        else if ("decada".equalsIgnoreCase(periodo)) {
            // Últimos 10 anos
            for (int i = 9; i >= 0; i--) {
                int ano = hoje.getYear() - i;
                labels.add(String.valueOf(ano));
                BigDecimal rec = somaAno(todasPagas, TipoTransacao.RECEITA, ano);
                BigDecimal des = somaAno(todasPagas, TipoTransacao.DESPESA, ano);
                receitas.add(rec); despesas.add(des); saldos.add(rec.subtract(des));
            }
        } 
        else {
            // Mensal (Padrão) - 12 meses do ano atual
            String[] meses = {"Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"};
            labels = Arrays.asList(meses);
            for (int i = 1; i <= 12; i++) {
                BigDecimal rec = somaMes(todasPagas, TipoTransacao.RECEITA, i, hoje.getYear());
                BigDecimal des = somaMes(todasPagas, TipoTransacao.DESPESA, i, hoje.getYear());
                receitas.add(rec); despesas.add(des); saldos.add(rec.subtract(des));
            }
        }

        return new GraficoFluxoDTO(labels, receitas, despesas, saldos);
    }

    // Auxiliares Matemáticos para o Gráfico
    private BigDecimal somaPeriodo(List<TransacaoFinanceira> lista, TipoTransacao tipo, LocalDate inicio, LocalDate fim) {
        return lista.stream()
                .filter(t -> t.getTipo() == tipo && !t.getDataVencimento().isBefore(inicio) && !t.getDataVencimento().isAfter(fim))
                .map(TransacaoFinanceira::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal somaMes(List<TransacaoFinanceira> lista, TipoTransacao tipo, int mes, int ano) {
        return lista.stream()
                .filter(t -> t.getTipo() == tipo && t.getDataVencimento().getMonthValue() == mes && t.getDataVencimento().getYear() == ano)
                .map(TransacaoFinanceira::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal somaAno(List<TransacaoFinanceira> lista, TipoTransacao tipo, int ano) {
        return lista.stream()
                .filter(t -> t.getTipo() == tipo && t.getDataVencimento().getYear() == ano)
                .map(TransacaoFinanceira::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ==========================================
    // ROTINAS DE CÁLCULO E BUSCA
    // ==========================================

    @Override
    public void processarStatusAtrasados() {
        List<TransacaoFinanceira> todas = transacaoRepository.findAll();
        for (TransacaoFinanceira t : todas) {
            if (t.getStatus() == StatusPagamento.PENDENTE && 
                t.getDataVencimento() != null && 
                t.getDataVencimento().isBefore(LocalDate.now())) {
                
                t.setStatus(StatusPagamento.ATRASADO);
                transacaoRepository.save(t);
            }
        }
    }

    @Override
    public BigDecimal calcularTotalReceitas() {
        BigDecimal total = transacaoRepository.sumTotalByTipo(TipoTransacao.RECEITA);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal calcularTotalDespesas() {
        BigDecimal total = transacaoRepository.sumTotalByTipo(TipoTransacao.DESPESA);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal calcularSaldoLiquido() {
        return calcularTotalReceitas().subtract(calcularTotalDespesas());
    }

    @Override
    public List<TransacaoFinanceira> listarUltimasTransacoes() {
        return transacaoRepository.findAll(
            PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "dataHora"))
        ).getContent();
    }

    @Override
    public Map<String, BigDecimal> obterDadosDespesasPorCategoria() {
        List<Object[]> resultados = transacaoRepository.sumByCategoria(TipoTransacao.DESPESA);
        Map<String, BigDecimal> mapa = new HashMap<>();
        
        for (Object[] obj : resultados) {
            CategoriaFinanceira cat = (CategoriaFinanceira) obj[0];
            BigDecimal valor = (BigDecimal) obj[1];
            mapa.put(cat.name(), valor);
        }
        return mapa;
    }
}
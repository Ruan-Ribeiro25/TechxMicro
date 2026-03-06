package com.vidaplus.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "transacoes_financeiras")
public class TransacaoFinanceira {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String descricao; // Ex: "Consulta Dr. Silva", "Conta de Luz"

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoTransacao tipo; // RECEITA ou DESPESA

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaFinanceira categoria;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    // --- NOVOS CAMPOS PARA STATUS FINANCEIRO ---
    @Column(name = "data_vencimento")
    private LocalDate dataVencimento; // Para calcular "Atrasado"

    @Column(name = "data_pagamento")
    private LocalDate dataPagamento; // Data efetiva da baixa

    @Enumerated(EnumType.STRING)
    @Column(name = "status_atual")
    private StatusPagamento status; // PENDENTE, PAGO, ATRASADO...

    // --- Campos do Mercado Pago ---
    @Column(name = "mp_preference_id")
    private String mpPreferenceId; // ID único da transação no MP

    @Column(name = "mp_status")
    private String mpStatus; // approved, pending, failure

    @Column(name = "mp_payment_link", length = 500)
    private String mpPaymentLink; // Link para o checkout

    // --- Enums Internos ---
    public enum TipoTransacao {
        RECEITA,
        DESPESA
    }

    public enum StatusPagamento {
        PENDENTE,   // Aguardando pagamento (Amarelo)
        PAGO,       // Dinheiro em caixa (Verde)
        ATRASADO,   // Passou do vencimento e não pagou (Vermelho)
        CANCELADO,  // Cancelado manualmente (Cinza)
        ERRO        // Erro na transação (Laranja)
    }

    public enum CategoriaFinanceira {
        CONSULTA,
        EXAME,
        PROCEDIMENTO,
        VENDA_FARMACIA,
        FOLHA_PAGAMENTO,
        INSUMOS,
        MANUTENCAO,
        CONTAS_CONSUMO, // Água, Luz, Internet
        OUTROS
    }

    // --- Construtores ---
    public TransacaoFinanceira() {
        this.dataHora = LocalDateTime.now();
        this.status = StatusPagamento.PENDENTE; // Padrão ao criar
    }

    // --- Lógica Auxiliar (Fácil acesso no Thymeleaf) ---
    public boolean isAtrasado() {
        // Se não foi pago E tem vencimento E vencimento é antes de hoje
        return this.status != StatusPagamento.PAGO 
            && this.dataVencimento != null 
            && this.dataVencimento.isBefore(LocalDate.now());
    }

    // --- Getters e Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public TipoTransacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoTransacao tipo) {
        this.tipo = tipo;
    }

    public CategoriaFinanceira getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaFinanceira categoria) {
        this.categoria = categoria;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public LocalDate getDataVencimento() {
        return dataVencimento;
    }

    public void setDataVencimento(LocalDate dataVencimento) {
        this.dataVencimento = dataVencimento;
    }

    public LocalDate getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(LocalDate dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    public StatusPagamento getStatus() {
        return status;
    }

    public void setStatus(StatusPagamento status) {
        this.status = status;
    }

    public String getMpPreferenceId() {
        return mpPreferenceId;
    }

    public void setMpPreferenceId(String mpPreferenceId) {
        this.mpPreferenceId = mpPreferenceId;
    }

    public String getMpStatus() {
        return mpStatus;
    }

    public void setMpStatus(String mpStatus) {
        this.mpStatus = mpStatus;
    }

    public String getMpPaymentLink() {
        return mpPaymentLink;
    }

    public void setMpPaymentLink(String mpPaymentLink) {
        this.mpPaymentLink = mpPaymentLink;
    }
}
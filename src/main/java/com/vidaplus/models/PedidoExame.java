package com.vidaplus.models;

import com.vidaplus.entity.Polo;    // Importando do pacote correto existente
import com.vidaplus.entity.Usuario; // Importando do pacote correto existente
import com.vidaplus.enums.StatusPagamento;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos_exame")
public class PedidoExame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "paciente_id", nullable = false)
    private Usuario paciente;

    @ManyToOne
    @JoinColumn(name = "polo_id", nullable = false)
    private Polo polo;

    @Column(name = "medico_solicitante_id")
    private Long medicoSolicitanteId;

    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao = LocalDateTime.now();

    // Financeiro
    @Column(name = "valor_total", nullable = false)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_pagamento")
    private StatusPagamento statusPagamento = StatusPagamento.PENDENTE;

    // Integração Mercado Pago
    @Column(name = "mp_preference_id")
    private String mpPreferenceId;

    @Column(name = "mp_payment_link")
    private String mpPaymentLink;

    // Relacionamento com os itens (Agora funciona pois ItemExame existe)
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemExame> itens = new ArrayList<>();

    // Construtor vazio (obrigatório JPA)
    public PedidoExame() {}

    // Método auxiliar para regra de negócio: Pode baixar o laudo?
    public boolean podeBaixarLaudos() {
        return this.statusPagamento == StatusPagamento.APROVADO 
            || this.statusPagamento == StatusPagamento.CORTESIA
            || this.statusPagamento == StatusPagamento.FATURADO_CONVENIO;
    }

    // --- Getters e Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getPaciente() {
        return paciente;
    }

    public void setPaciente(Usuario paciente) {
        this.paciente = paciente;
    }

    public Polo getPolo() {
        return polo;
    }

    public void setPolo(Polo polo) {
        this.polo = polo;
    }

    public Long getMedicoSolicitanteId() {
        return medicoSolicitanteId;
    }

    public void setMedicoSolicitanteId(Long medicoSolicitanteId) {
        this.medicoSolicitanteId = medicoSolicitanteId;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public StatusPagamento getStatusPagamento() {
        return statusPagamento;
    }

    public void setStatusPagamento(StatusPagamento statusPagamento) {
        this.statusPagamento = statusPagamento;
    }

    public String getMpPreferenceId() {
        return mpPreferenceId;
    }

    public void setMpPreferenceId(String mpPreferenceId) {
        this.mpPreferenceId = mpPreferenceId;
    }

    public String getMpPaymentLink() {
        return mpPaymentLink;
    }

    public void setMpPaymentLink(String mpPaymentLink) {
        this.mpPaymentLink = mpPaymentLink;
    }

    public List<ItemExame> getItens() {
        return itens;
    }

    public void setItens(List<ItemExame> itens) {
        this.itens = itens;
    }
}
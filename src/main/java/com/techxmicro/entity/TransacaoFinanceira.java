package com.techxmicro.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class TransacaoFinanceira {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String descricao;
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    private TipoTransacao tipo;

    @Enumerated(EnumType.STRING)
    private CategoriaFinanceira categoria;

    @Enumerated(EnumType.STRING)
    private StatusPagamento status;

    private LocalDate dataVencimento;
    private LocalDate dataPagamento;
    private LocalDateTime dataHora = LocalDateTime.now(); // Para ordenação

    // Campos Mercado Pago (da sua regra de negócio)
    private String mpPreferenceId;
    private String mpPaymentLink;
    private String mpStatus;

    // --- ENUMS INTERNOS ---
    public enum TipoTransacao { RECEITA, DESPESA }
    public enum StatusPagamento { PAGO, PENDENTE, ATRASADO, CANCELADO }
    public enum CategoriaFinanceira {
        CONSULTA, SERVICOS, VENDAS, INSUMOS, FOLHA_PAGAMENTO, MANUTENCAO, CONTAS_CONSUMO, ALUGUEL, OUTROS
    }

    // --- GETTERS E SETTERS (Substituindo o Lombok para evitar erro no Eclipse) ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public TipoTransacao getTipo() { return tipo; }
    public void setTipo(TipoTransacao tipo) { this.tipo = tipo; }

    public CategoriaFinanceira getCategoria() { return categoria; }
    public void setCategoria(CategoriaFinanceira categoria) { this.categoria = categoria; }

    public StatusPagamento getStatus() { return status; }
    public void setStatus(StatusPagamento status) { this.status = status; }

    public LocalDate getDataVencimento() { return dataVencimento; }
    public void setDataVencimento(LocalDate dataVencimento) { this.dataVencimento = dataVencimento; }

    public LocalDate getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(LocalDate dataPagamento) { this.dataPagamento = dataPagamento; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public String getMpPreferenceId() { return mpPreferenceId; }
    public void setMpPreferenceId(String mpPreferenceId) { this.mpPreferenceId = mpPreferenceId; }

    public String getMpPaymentLink() { return mpPaymentLink; }
    public void setMpPaymentLink(String mpPaymentLink) { this.mpPaymentLink = mpPaymentLink; }

    public String getMpStatus() { return mpStatus; }
    public void setMpStatus(String mpStatus) { this.mpStatus = mpStatus; }
}
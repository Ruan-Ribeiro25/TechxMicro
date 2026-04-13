package com.techxmicro.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

import com.techxmicro.enums.CategoriaChamado;
import com.techxmicro.enums.StatusChamado;
import com.techxmicro.enums.UrgenciaChamado;

@Entity
@Table(name = "chamados")
public class Chamado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String assunto;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false)
    private LocalDateTime dataAbertura = LocalDateTime.now();

    private LocalDateTime dataFechamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusChamado status = StatusChamado.PENDENTE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaChamado categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UrgenciaChamado urgencia;

    @ManyToOne
    @JoinColumn(name = "solicitante_id", nullable = false)
    private Usuario solicitante;

    @ManyToOne
    @JoinColumn(name = "responsavel_id")
    private Usuario responsavel;

    // ==========================================
    // CORREÇÃO: fetch = FetchType.EAGER
    // ==========================================
    @OneToMany(mappedBy = "chamado", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<InteracaoChamado> interacoes = new java.util.ArrayList<>();

    public Chamado() {
        this.interacoes = new java.util.ArrayList<>();
    }

    // ==========================================
    // GETTERS E SETTERS EXPLÍCITOS
    // ==========================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAssunto() { return assunto; }
    public void setAssunto(String assunto) { this.assunto = assunto; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public LocalDateTime getDataAbertura() { return dataAbertura; }
    public void setDataAbertura(LocalDateTime dataAbertura) { this.dataAbertura = dataAbertura; }

    public LocalDateTime getDataFechamento() { return dataFechamento; }
    public void setDataFechamento(LocalDateTime dataFechamento) { this.dataFechamento = dataFechamento; }

    public StatusChamado getStatus() { return status; }
    public void setStatus(StatusChamado status) { this.status = status; }

    public CategoriaChamado getCategoria() { return categoria; }
    public void setCategoria(CategoriaChamado categoria) { this.categoria = categoria; }

    public UrgenciaChamado getUrgencia() { return urgencia; }
    public void setUrgencia(UrgenciaChamado urgencia) { this.urgencia = urgencia; }

    public Usuario getSolicitante() { return solicitante; }
    public void setSolicitante(Usuario solicitante) { this.solicitante = solicitante; }

    public Usuario getResponsavel() { return responsavel; }
    public void setResponsavel(Usuario responsavel) { this.responsavel = responsavel; }

    public List<InteracaoChamado> getInteracoes() { return interacoes; }
    public void setInteracoes(List<InteracaoChamado> interacoes) { this.interacoes = interacoes; }
    
    
}
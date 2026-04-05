package com.helpdesk.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "interacoes_chamado")
public class InteracaoChamado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chamado_id", nullable = false)
    private Chamado chamado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    // --- PACOTE 1: Colunas originais ---
    @Column(name = "texto", nullable = false, columnDefinition = "TEXT")
    private String texto;

    @Column(name = "data_envio", nullable = false)
    private LocalDateTime dataEnvio = LocalDateTime.now();

    // --- PACOTE 2: Colunas espelhadas da sua arquitetura ---
    @Column(name = "mensagem", nullable = false, columnDefinition = "TEXT")
    private String mensagem;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora = LocalDateTime.now();


    // ==================== CONSTRUTORES ====================
    public InteracaoChamado() {}

    // ==================== GETTERS E SETTERS INTELIGENTES ====================
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Chamado getChamado() { return chamado; }
    public void setChamado(Chamado chamado) { this.chamado = chamado; }

    public Usuario getAutor() { return autor; }
    public void setAutor(Usuario autor) { this.autor = autor; }

    // Ao setar o "texto", ele já preenche a "mensagem" para não dar erro no banco
    public String getTexto() { return texto; }
    public void setTexto(String texto) { 
        this.texto = texto; 
        this.mensagem = texto; 
    }

    // Ao setar a "dataEnvio", ele já preenche a "dataHora" para não dar erro no banco
    public LocalDateTime getDataEnvio() { return dataEnvio; }
    public void setDataEnvio(LocalDateTime dataEnvio) { 
        this.dataEnvio = dataEnvio; 
        this.dataHora = dataEnvio;
    }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { 
        this.mensagem = mensagem; 
        this.texto = mensagem;
    }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { 
        this.dataHora = dataHora; 
        this.dataEnvio = dataHora;
    }
}
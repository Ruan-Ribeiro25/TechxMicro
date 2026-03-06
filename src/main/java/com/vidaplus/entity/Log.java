package com.vidaplus.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historico_logs")
public class Log extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "username_registrado")
    private String usernameRegistrado;

    private String acao;
    
    @Column(columnDefinition = "TEXT")
    private String detalhes;

    @Column(name = "data_hora")
    private LocalDateTime dataHora;

    @PrePersist
    protected void onCreate() {
        dataHora = LocalDateTime.now();
    }

    public Log() {}

    public Log(Usuario usuario, String acao, String detalhes) {
        this.usuario = usuario;
        if (usuario != null) {
            this.usernameRegistrado = (usuario.getUsername() != null) ? usuario.getUsername() : usuario.getEmail();
        }
        this.acao = acao;
        this.detalhes = detalhes;
    }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getUsernameRegistrado() { return usernameRegistrado; }
    public void setUsernameRegistrado(String usernameRegistrado) { this.usernameRegistrado = usernameRegistrado; }
    public String getAcao() { return acao; }
    public void setAcao(String acao) { this.acao = acao; }
    public String getDetalhes() { return detalhes; }
    public void setDetalhes(String detalhes) { this.detalhes = detalhes; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
}
package com.helpdesk.entity;
import jakarta.persistence.*;

@Entity
public class Laboratorio {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nomeExame;
    private String resultado;
    // Getters e Setters
    public Long getId() { return id; }
    public String getNomeExame() { return nomeExame; }
    public void setNomeExame(String nomeExame) { this.nomeExame = nomeExame; }
    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }
}
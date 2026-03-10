package com.copamir.entity;
import jakarta.persistence.*;

@Entity
public class Leito {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String numero;
    private String status; // Ex: "LIVRE", "OCUPADO"
    // Getters e Setters
    public Long getId() { return id; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
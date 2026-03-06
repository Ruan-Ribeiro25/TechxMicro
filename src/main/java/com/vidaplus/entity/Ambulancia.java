package com.vidaplus.entity; // Pacote ajustado conforme solicitado

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ambulancias")
public class Ambulancia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String placa;

    private String tipo;      // UTI, SIMPLES, RESGATE
    private String modelo;    
    
    @Column(nullable = false)
    private String status;    // DISPONIVEL, EM_CHAMADO, MANUTENCAO

    private String motorista;
    
    @Column(name = "previsao_liberacao")
    private String previsaoLiberacao;

    private LocalDateTime dataCadastro = LocalDateTime.now();

    // Construtor Vazio (Obrigatório JPA)
    public Ambulancia() {}

    // Construtor utilitário
    public Ambulancia(String placa, String tipo, String modelo, String status, String motorista, String previsaoLiberacao) {
        this.placa = placa;
        this.tipo = tipo;
        this.modelo = modelo;
        this.status = status;
        this.motorista = motorista;
        this.previsaoLiberacao = previsaoLiberacao;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMotorista() { return motorista; }
    public void setMotorista(String motorista) { this.motorista = motorista; }

    public String getPrevisaoLiberacao() { return previsaoLiberacao; }
    public void setPrevisaoLiberacao(String previsaoLiberacao) { this.previsaoLiberacao = previsaoLiberacao; }
}
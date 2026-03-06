package com.vidaplus.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sinais_vitais")
public class SinaisVitais {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relacionamento com o Paciente (Usuario)
    @ManyToOne
    @JoinColumn(name = "paciente_id", nullable = false)
    private Usuario paciente;

    // Relacionamento com o Responsável/Enfermeiro (Usuario)
    // Permite acessar a foto via: triagem.responsavel.fotoPerfil
    @ManyToOne
    @JoinColumn(name = "responsavel_id", nullable = false)
    private Usuario responsavel;

    // Dados da Triagem
    private Integer pressaoSistolica;
    private Integer pressaoDiastolica;
    private Integer glicemia;
    private Double temperatura;
    private String queixa;

    private LocalDateTime dataHora;

    // --- CONSTRUTORES ---
    public SinaisVitais() {
        this.dataHora = LocalDateTime.now();
    }

    public SinaisVitais(Usuario paciente, Usuario responsavel, Integer sistolica, Integer diastolica, Integer glicemia, Double temperatura, String queixa) {
        this.paciente = paciente;
        this.responsavel = responsavel;
        this.pressaoSistolica = sistolica;
        this.pressaoDiastolica = diastolica;
        this.glicemia = glicemia;
        this.temperatura = temperatura;
        this.queixa = queixa;
        this.dataHora = LocalDateTime.now();
    }

    // --- GETTERS E SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getPaciente() { return paciente; }
    public void setPaciente(Usuario paciente) { this.paciente = paciente; }

    public Usuario getResponsavel() { return responsavel; }
    public void setResponsavel(Usuario responsavel) { this.responsavel = responsavel; }

    public Integer getPressaoSistolica() { return pressaoSistolica; }
    public void setPressaoSistolica(Integer pressaoSistolica) { this.pressaoSistolica = pressaoSistolica; }

    public Integer getPressaoDiastolica() { return pressaoDiastolica; }
    public void setPressaoDiastolica(Integer pressaoDiastolica) { this.pressaoDiastolica = pressaoDiastolica; }

    public Integer getGlicemia() { return glicemia; }
    public void setGlicemia(Integer glicemia) { this.glicemia = glicemia; }

    public Double getTemperatura() { return temperatura; }
    public void setTemperatura(Double temperatura) { this.temperatura = temperatura; }

    public String getQueixa() { return queixa; }
    public void setQueixa(String queixa) { this.queixa = queixa; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
}
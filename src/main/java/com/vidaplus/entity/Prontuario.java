package com.vidaplus.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "prontuarios")
public class Prontuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relacionamento com o Paciente
    @ManyToOne
    @JoinColumn(name = "paciente_id", nullable = false)
    private Usuario paciente;

    // Relacionamento com o Médico (Usuario)
    // Permite acessar a foto no HTML via: reg.medico.fotoPerfil
    @ManyToOne
    @JoinColumn(name = "medico_id", nullable = false)
    private Usuario medico;

    private LocalDateTime dataHora;

    // Campos de texto longo para o histórico médico
    @Column(columnDefinition = "TEXT")
    private String conteudo; // Anamnese / Evolução

    @Column(columnDefinition = "TEXT")
    private String diagnostico;

    @Column(columnDefinition = "TEXT")
    private String prescricao;

    // --- CONSTRUTORES ---

    public Prontuario() {
        this.dataHora = LocalDateTime.now();
    }

    public Prontuario(Usuario paciente, Usuario medico, String conteudo, String diagnostico, String prescricao) {
        this.paciente = paciente;
        this.medico = medico;
        this.conteudo = conteudo;
        this.diagnostico = diagnostico;
        this.prescricao = prescricao;
        this.dataHora = LocalDateTime.now();
    }

    // --- GETTERS E SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getPaciente() { return paciente; }
    public void setPaciente(Usuario paciente) { this.paciente = paciente; }

    public Usuario getMedico() { return medico; }
    public void setMedico(Usuario medico) { this.medico = medico; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public String getConteudo() { return conteudo; }
    public void setConteudo(String conteudo) { this.conteudo = conteudo; }

    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }

    public String getPrescricao() { return prescricao; }
    public void setPrescricao(String prescricao) { this.prescricao = prescricao; }
}
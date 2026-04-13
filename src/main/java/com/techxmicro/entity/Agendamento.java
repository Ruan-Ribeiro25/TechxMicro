package com.techxmicro.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "agendamentos")
public class Agendamento extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    private String procedimento;
    
    // Mantido para compatibilidade com legados, mas o novo usa o objeto 'medico'
    private String profissional; 

    private LocalDateTime dataHora;

    // --- NOVOS CAMPOS EXIGIDOS PELO CONTROLLER DA RECEPÇÃO ---
    @Column(name = "data_consulta")
    private LocalDate dataConsulta;

    @Column(name = "horario")
    private LocalTime horario;

    private Boolean prioridade = false;

    // Atualizado de String para Enum para corrigir o erro "StatusAgendamento"
    @Enumerated(EnumType.STRING)
    private StatusAgendamento status;

    @ManyToOne
    @JoinColumn(name = "paciente_id")
    private Usuario usuario; 

    @ManyToOne
    @JoinColumn(name = "medico_id")
    private Usuario medico; // O Controller pede .getMedico()

    public Agendamento() {}

    public Agendamento(String procedimento, String profissional, LocalDateTime dataHora, StatusAgendamento status, Usuario usuario) {
        this.procedimento = procedimento;
        this.profissional = profissional;
        this.dataHora = dataHora;
        this.status = status;
        this.usuario = usuario;
        // Sincronia automática para facilitar
        if (dataHora != null) {
            this.dataConsulta = dataHora.toLocalDate();
            this.horario = dataHora.toLocalTime();
        }
    }

    // --- GETTERS E SETTERS ---

    public String getProcedimento() { return procedimento; }
    public void setProcedimento(String procedimento) { this.procedimento = procedimento; }

    public String getProfissional() { return profissional; }
    public void setProfissional(String profissional) { this.profissional = profissional; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { 
        this.dataHora = dataHora;
        // Atualiza campos separados se dataHora for definido
        if (dataHora != null) {
            this.dataConsulta = dataHora.toLocalDate();
            this.horario = dataHora.toLocalTime();
        }
    }

    // --- MÉTODOS DE COMPATIBILIDADE (Resolve os erros vermelhos do Controller) ---

    // O Controller chama .getPaciente(), criamos esse atalho para retornar o usuario
    public Usuario getPaciente() { return usuario; }
    public void setPaciente(Usuario usuario) { this.usuario = usuario; }
    
    // Getter original
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Usuario getMedico() { return medico; }
    public void setMedico(Usuario medico) { this.medico = medico; }

    public LocalDate getDataConsulta() { return dataConsulta; }
    public void setDataConsulta(LocalDate dataConsulta) { this.dataConsulta = dataConsulta; }

    public LocalTime getHorario() { return horario; }
    public void setHorario(LocalTime horario) { this.horario = horario; }

    public Boolean getPrioridade() { return prioridade; }
    public void setPrioridade(Boolean prioridade) { this.prioridade = prioridade; }

    public StatusAgendamento getStatus() { return status; }
    public void setStatus(StatusAgendamento status) { this.status = status; }
}
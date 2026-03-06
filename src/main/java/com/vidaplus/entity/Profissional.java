package com.vidaplus.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "profissionais")
public class Profissional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // RELACIONAMENTO CRÍTICO:
    // Permite navegar: registro.profissional.usuario.fotoPerfil
    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // Dados Específicos
    private String crm;   // Para Médicos
    private String coren; // Para Enfermeiros
    
    @Column(unique = true)
    private String matricula;
    
    private String especialidade;
    
    @Column(name = "tipo_profissional")
    private String tipoProfissional; // "MEDICO" ou "ENFERMEIRO"

    @Column(name = "data_matricula")
    private LocalDate dataMatricula;

    @Column(name = "status_aprovacao")
    private String statusAprovacao; // "APROVADO", "PENDENTE"

    // --- CONSTRUTORES ---
    public Profissional() {}

    // --- GETTERS E SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public String getCrm() { return crm; }
    public void setCrm(String crm) { this.crm = crm; }

    public String getCoren() { return coren; }
    public void setCoren(String coren) { this.coren = coren; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getEspecialidade() { return especialidade; }
    public void setEspecialidade(String especialidade) { this.especialidade = especialidade; }

    public String getTipoProfissional() { return tipoProfissional; }
    public void setTipoProfissional(String tipoProfissional) { this.tipoProfissional = tipoProfissional; }

    public LocalDate getDataMatricula() { return dataMatricula; }
    public void setDataMatricula(LocalDate dataMatricula) { this.dataMatricula = dataMatricula; }

    public String getStatusAprovacao() { return statusAprovacao; }
    public void setStatusAprovacao(String statusAprovacao) { this.statusAprovacao = statusAprovacao; }
    
    // Método auxiliar para facilitar o acesso ao nome
    public String getNome() {
        return usuario != null ? usuario.getNome() : "---";
    }
}
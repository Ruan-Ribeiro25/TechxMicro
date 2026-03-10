package com.vidaplus.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "polos")
public class Polo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String tipo; // "HOSPITAL" (Pai) ou "CLINICA" (Filho)
    private String cidade;
    private String bairro; // Novo campo para identificar a Clínica de Bairro
    private String cep;
    private boolean ativo = true; // Para controle de exclusão lógica

    // --- NOVOS CAMPOS PARA DETALHAMENTO ---
    private String logradouro;
    private LocalDate dataInauguracao;
    private String horarioFuncionamento;

    // --- HIERARQUIA (CIDADE x BAIRRO) ---
    @ManyToOne
    @JoinColumn(name = "polo_pai_id")
    private Polo poloPai; // O Hospital da Cidade será o Pai da Clínica de Bairro

    @OneToMany(mappedBy = "poloPai", cascade = CascadeType.ALL)
    private List<Polo> filiais = new ArrayList<>();

    // --- RELACIONAMENTOS ORIGINAIS MANTIDOS ---
    @ManyToOne
    @JoinColumn(name = "responsavel_id")
    private Usuario responsavel; // Profissional ou Admin responsável pelo Polo

    // Este campo é OBRIGATÓRIO porque na classe Usuario usamos mappedBy="usuario"
    @ManyToOne
    @JoinColumn(name = "usuario_id") 
    private Usuario usuario; 
    // -------------------------------------------

    public Polo() {}

    // Getters e Setters Originais
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public String getLogradouro() { return logradouro; }
    public void setLogradouro(String logradouro) { this.logradouro = logradouro; }

    public LocalDate getDataInauguracao() { return dataInauguracao; }
    public void setDataInauguracao(LocalDate dataInauguracao) { this.dataInauguracao = dataInauguracao; }

    public String getHorarioFuncionamento() { return horarioFuncionamento; }
    public void setHorarioFuncionamento(String horarioFuncionamento) { this.horarioFuncionamento = horarioFuncionamento; }

    public Usuario getResponsavel() { return responsavel; }
    public void setResponsavel(Usuario responsavel) { this.responsavel = responsavel; }

    // Novos Getters e Setters para a Hierarquia
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public Polo getPoloPai() { return poloPai; }
    public void setPoloPai(Polo poloPai) { this.poloPai = poloPai; }

    public List<Polo> getFiliais() { return filiais; }
    public void setFiliais(List<Polo> filiais) { this.filiais = filiais; }
}
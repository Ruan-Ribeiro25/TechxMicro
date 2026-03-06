package com.vidaplus.entity;

import jakarta.persistence.*;
import java.util.List;
import java.time.LocalDate;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    
    @Column(unique = true, nullable = false)
    private String cpf;
    
    private String rg; 
    private String cartaoSus;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Column(unique = true)
    private String username;

    @Column(unique = true)
    private String email;

    @Column(name = "password") 
    private String senha;

    private String perfil; 
    private boolean ativo = false;
    private String codigoVerificacao;
    private String tokenReset;

    // ENDEREÇO
    private String telefone;
    private String cep;
    private String cidade;
    private String uf;
    private String logradouro;
    private String numero;
    private String bairro;

    // FOTO DE PERFIL (BASE64)
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String fotoPerfil;

    // --- CORREÇÃO DE RELACIONAMENTO (GEOLOCALIZAÇÃO) ---
    // Alterado de @OneToMany para @ManyToMany para suportar múltiplos polos por usuário
    // e múltiplos usuários por polo. Conecta com a tabela 'usuarios_polos'.
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "usuarios_polos",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "polos_id")
    )
    private List<Polo> polos;

    // --- OUTROS RELACIONAMENTOS (MANTIDOS) ---
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Documento> documentos;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Agendamento> agendamentos;

    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SinaisVitais> sinaisVitais;

    // Atualizado: Em relacionamentos @ManyToMany, não precisamos 'zerar' o pai manualmente
    // O Hibernate cuida de limpar a tabela de junção 'usuarios_polos'.
    @PreRemove
    private void preRemove() {
        // Lógica de limpeza específica removida para evitar conflito com M2M
    }

    // --- GETTERS E SETTERS ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    
    public String getRg() { return rg; }
    public void setRg(String rg) { this.rg = rg; }
    
    public String getCartaoSus() { return cartaoSus; }
    public void setCartaoSus(String cartaoSus) { this.cartaoSus = cartaoSus; }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    
    public String getPassword() { return senha; } 
    
    public String getPerfil() { return perfil; }
    public void setPerfil(String perfil) { this.perfil = perfil; }
    
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    
    public String getCodigoVerificacao() { return codigoVerificacao; }
    public void setCodigoVerificacao(String codigoVerificacao) { this.codigoVerificacao = codigoVerificacao; }
    
    public String getTokenReset() { return tokenReset; }
    public void setTokenReset(String tokenReset) { this.tokenReset = tokenReset; }
    
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    
    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }
    
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    
    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    
    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }
    
    public String getLogradouro() { return logradouro; }
    public void setLogradouro(String logradouro) { this.logradouro = logradouro; }
    
    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }
    
    public List<Polo> getPolos() { return polos; }
    public void setPolos(List<Polo> polos) { this.polos = polos; }

    public List<Agendamento> getAgendamentos() { return agendamentos; }
    public void setAgendamentos(List<Agendamento> agendamentos) { this.agendamentos = agendamentos; }

    public List<Documento> getDocumentos() { return documentos; }
    public void setDocumentos(List<Documento> documentos) { this.documentos = documentos; }

    public List<SinaisVitais> getSinaisVitais() { return sinaisVitais; }
    public void setSinaisVitais(List<SinaisVitais> sinaisVitais) { this.sinaisVitais = sinaisVitais; }

    public String getFotoPerfil() { return fotoPerfil; }
    public void setFotoPerfil(String fotoPerfil) { this.fotoPerfil = fotoPerfil; }
}
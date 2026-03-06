package com.vidaplus.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table; // Import Adicionado
import java.time.LocalDateTime;

@Entity
@Table(name = "documentos") // <--- Adicionado para travar na tabela certa (que tem o BLOB)
public class Documento extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    private String nomeArquivo;
    private String tipo; 
    private String descricao;
    private LocalDateTime dataUpload;

    // --- NOVO CAMPO: Onde fica o arquivo real (BLOB) ---
    @Lob
    @Column(columnDefinition = "LONGBLOB") // Garante que cabe arquivos grandes no MySQL
    private byte[] dados;

    @ManyToOne
    private Usuario usuario;

    public Documento() {}

    public String getNomeArquivo() { return nomeArquivo; }
    public void setNomeArquivo(String nomeArquivo) { this.nomeArquivo = nomeArquivo; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public LocalDateTime getDataUpload() { return dataUpload; }
    public void setDataUpload(LocalDateTime dataUpload) { this.dataUpload = dataUpload; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    
    // Getters e Setters do novo campo
    public byte[] getDados() { return dados; }
    public void setDados(byte[] dados) { this.dados = dados; }
}
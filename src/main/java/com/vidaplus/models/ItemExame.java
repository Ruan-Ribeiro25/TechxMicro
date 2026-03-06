package com.vidaplus.models;

import com.vidaplus.enums.StatusExame;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "itens_exame")
public class ItemExame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pedido_id", nullable = false)
    private PedidoExame pedido;

    @Column(name = "nome_exame", nullable = false)
    private String nomeExame;

    @Column(name = "codigo_tuss")
    private String codigoTuss;

    @Column(nullable = false)
    private BigDecimal preco;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_clinico")
    private StatusExame statusClinico = StatusExame.SOLICITADO;

    @Column(name = "caminho_laudo_pdf")
    private String caminhoLaudoPdf;

    @Column(name = "data_liberacao")
    private LocalDateTime dataLiberacao;

    @Column(name = "responsavel_tecnico")
    private String responsavelTecnico;

    public ItemExame() {}

    // Getters e Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PedidoExame getPedido() { return pedido; }
    public void setPedido(PedidoExame pedido) { this.pedido = pedido; }

    public String getNomeExame() { return nomeExame; }
    public void setNomeExame(String nomeExame) { this.nomeExame = nomeExame; }

    public String getCodigoTuss() { return codigoTuss; }
    public void setCodigoTuss(String codigoTuss) { this.codigoTuss = codigoTuss; }

    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }

    public StatusExame getStatusClinico() { return statusClinico; }
    public void setStatusClinico(StatusExame statusClinico) { this.statusClinico = statusClinico; }

    public String getCaminhoLaudoPdf() { return caminhoLaudoPdf; }
    public void setCaminhoLaudoPdf(String caminhoLaudoPdf) { this.caminhoLaudoPdf = caminhoLaudoPdf; }

    public LocalDateTime getDataLiberacao() { return dataLiberacao; }
    public void setDataLiberacao(LocalDateTime dataLiberacao) { this.dataLiberacao = dataLiberacao; }

    public String getResponsavelTecnico() { return responsavelTecnico; }
    public void setResponsavelTecnico(String responsavelTecnico) { this.responsavelTecnico = responsavelTecnico; }
}
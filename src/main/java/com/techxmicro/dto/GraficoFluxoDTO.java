package com.techxmicro.dto;

import java.math.BigDecimal;
import java.util.List;

public class GraficoFluxoDTO {
    private List<String> labels;
    private List<BigDecimal> lucro;
    private List<BigDecimal> despesa;
    private List<BigDecimal> saldo; // NOVA LISTA PARA A LINHA DO SALDO

    public GraficoFluxoDTO(List<String> labels, List<BigDecimal> lucro, List<BigDecimal> despesa, List<BigDecimal> saldo) {
        this.labels = labels;
        this.lucro = lucro;
        this.despesa = despesa;
        this.saldo = saldo;
    }

    public List<String> getLabels() { return labels; }
    public List<BigDecimal> getLucro() { return lucro; }
    public List<BigDecimal> getDespesa() { return despesa; }
    public List<BigDecimal> getSaldo() { return saldo; } // NOVO GETTER
}
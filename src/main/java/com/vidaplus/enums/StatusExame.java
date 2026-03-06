package com.vidaplus.enums;

/**
 * Enum responsável por mapear todo o ciclo de vida de um exame laboratorial.
 * Usado na entidade ItemExame e PedidoExame.
 */
public enum StatusExame {

    SOLICITADO("Solicitado"),           // Médico pediu, paciente ainda não iniciou o processo
    AGUARDANDO_COLETA("Aguardando Coleta"),    // Paciente fez check-in, aguardando chamar na sala
    COLETADO("Coletado"),             // Amostra retirada (sangue, urina, etc)
    EM_PROCESSAMENTO("Em Processamento"),     // Está na máquina/analisador
    ANALISE_TECNICA("Análise Técnica"),      // Bioquímico está revisando (pendências ou dúvidas)
    RECOLETA_NECESSARIA("Recoleta Necessária"),  // Amostra coagulou, insuficiente ou acidente (ERRO)
    LIBERADO("Liberado"),             // Laudo pronto e assinado (PDF gerado)
    CANCELADO("Cancelado");             // Erro administrativo ou desistência

    private final String descricao;

    StatusExame(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
package com.vidaplus.enums;

/**
 * Enum responsável por controlar o fluxo financeiro dos pedidos de exame.
 * Usado na entidade PedidoExame e na integração com Mercado Pago.
 */
public enum StatusPagamento {

    PENDENTE("Pendente"),             // Criado, mas não pago
    AGUARDANDO_GATEWAY("Processando"),   // Enviado ao Mercado Pago, esperando callback
    APROVADO("Aprovado"),             // Pago (Libera Download)
    REJEITADO("Rejeitado"),            // Cartão recusado ou boleto vencido
    ESTORNADO("Estornado"),            // Dinheiro devolvido ao paciente
    CORTESIA("Cortesia"),             // Gratuito (Autorizado pela gerência)
    FATURADO_CONVENIO("Faturado Convênio"); // Pago pelo plano de saúde (não cobra do paciente)

    private final String descricao;

    StatusPagamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
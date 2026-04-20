package com.techxmicro.enums;

public class TransacaoFinanceira {

    public enum TipoTransacao {
        RECEITA, 
        DESPESA
    }

    public enum StatusPagamento {
        PAGO, 
        PENDENTE, 
        ATRASADO, 
        CANCELADO
    }

    public enum CategoriaFinanceira {
        CONSULTA, 
        SERVICOS, 
        VENDAS, 
        INSUMOS, 
        FOLHA_PAGAMENTO, 
        MANUTENCAO, 
        CONTAS_CONSUMO, 
        ALUGUEL, 
        OUTROS
    }
}
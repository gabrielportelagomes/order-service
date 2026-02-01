package br.com.tcc.orderservice.enums;

public enum OrderStatus {

    /**
     * Pedido registrado e aguardando processamento.
     * Usado no início do fluxo assíncrono.
     */
    PENDING,

    /**
     * Pedido em processamento ativo.
     * Usado no fluxo síncrono enquanto as chamadas externas estão em execução.
     */
    PROCESSING,

    /**
     * Pedido concluído com sucesso.
     * Todas as etapas de negócio foram executadas corretamente.
     */
    CONFIRMED,

    /**
     * Falha na etapa de reserva de estoque.
     * O pedido não prossegue para pagamento.
     */
    FAILED_INVENTORY,

    /**
     * Falha na etapa de pagamento.
     * Pode ter havido necessidade de compensação do estoque.
     */
    FAILED_PAYMENT,

    /**
     * Falha técnica de integração entre serviços.
     * Ex: timeout, erro de rede ou falha na compensação.
     */
    ERROR_INTEGRATION,

    /**
     * Erro sistêmico inesperado.
     * Indica falha fora do fluxo de negócio que requer intervenção ou análise manual.
     */
    ERROR_SYSTEM
}

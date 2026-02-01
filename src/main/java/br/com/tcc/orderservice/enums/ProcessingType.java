package br.com.tcc.orderservice.enums;

public enum ProcessingType {

    /**
     * Processamento síncrono.
     * O fluxo é executado por chamadas HTTP diretas, com a thread aguardando
     * resposta dos serviços de estoque e pagamento.
     */
    SYNC,

    /**
     * Processamento assíncrono.
     * O fluxo ocorre por troca de eventos via mensageria (Kafka),
     * com comunicação desacoplada e consistência eventual.
     */
    ASYNC
}

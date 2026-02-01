package br.com.tcc.orderservice.dtos;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

public class PaymentApprovedDeserializer extends ObjectMapperDeserializer<PaymentApprovedEvent> {
    public PaymentApprovedDeserializer() {
        super(PaymentApprovedEvent.class);
    }
}

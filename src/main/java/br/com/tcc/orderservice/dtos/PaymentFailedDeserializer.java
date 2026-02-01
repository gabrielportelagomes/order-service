package br.com.tcc.orderservice.dtos;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

public class PaymentFailedDeserializer extends ObjectMapperDeserializer<PaymentFailedEvent> {
    public PaymentFailedDeserializer() {
        super(PaymentFailedEvent.class);
    }
}

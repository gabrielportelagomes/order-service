package br.com.tcc.orderservice.dtos;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

public class InventoryFailedDeserializer extends ObjectMapperDeserializer<InventoryFailedEvent> {
    public InventoryFailedDeserializer() {
        super(InventoryFailedEvent.class);
    }
}

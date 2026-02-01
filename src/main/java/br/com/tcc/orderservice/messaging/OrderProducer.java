package br.com.tcc.orderservice.messaging;

import br.com.tcc.orderservice.dtos.OrderCreatedEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;


@ApplicationScoped
public class OrderProducer {

    private static final Logger LOG = Logger.getLogger(OrderProducer.class);

    @Inject
    @Channel("order-created")
    Emitter<OrderCreatedEvent> emitter;

    public void publishOrderCreated(OrderCreatedEvent event) {
        emitter.send(event)
                .whenComplete((void_, exception) -> {
                    if (exception != null) {
                        LOG.error("KAFKA DOWN! Pedido " + event.orderId() + " ficou inconsistente.");
                    }
                });
    }
}
package br.com.tcc.orderservice.messaging;

import br.com.tcc.orderservice.dtos.InventoryFailedEvent;
import br.com.tcc.orderservice.dtos.PaymentApprovedEvent;
import br.com.tcc.orderservice.dtos.PaymentFailedEvent;
import br.com.tcc.orderservice.enums.OrderStatus;
import br.com.tcc.orderservice.services.OrderService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class OrderProcess {

    private static final Logger LOG = Logger.getLogger(OrderProcess.class);

    @Inject
    OrderService service;

    @Incoming("payment-result-in")
    public CompletionStage<Void> processSuccess(PaymentApprovedEvent event) {
        return executeUpdate(event.orderId(), OrderStatus.CONFIRMED, "Sucesso de Pagamento");
    }

    @Incoming("inventory-failed-in")
    public CompletionStage<Void> processInvFail(InventoryFailedEvent event) {
        return executeUpdate(event.orderId(), OrderStatus.FAILED_INVENTORY, "Falha de Estoque");
    }

    @Incoming("payment-failed-in")
    public CompletionStage<Void> processPayFail(PaymentFailedEvent event) {
        return executeUpdate(event.orderId(), OrderStatus.FAILED_PAYMENT, "Falha de Pagamento");
    }

    private CompletionStage<Void> executeUpdate(Long orderId, OrderStatus status, String eventContext) {
        return CompletableFuture.runAsync(() -> {
            try {
                LOG.infof("[%s] Tentando atualizar pedido %d para %s", eventContext, orderId, status);
                service.updateStatusWithRetry(orderId, status);

                LOG.infof("[%s] Pedido %d atualizado com sucesso.", eventContext, orderId);
            } catch (WebApplicationException e) {
                LOG.warnf("[%s] Erro de Negócio: %s (Status HTTP: %d). Mensagem descartada.",
                        eventContext, e.getMessage(), e.getResponse().getStatus());
            } catch (Exception e) {
                LOG.errorf("[%s] Erro Técnico ao atualizar pedido %d. Relançando para RETRY...",
                        eventContext, orderId, e);
                throw e;
            }
        });
    }
}

package br.com.tcc.orderservice.services;

import br.com.tcc.orderservice.clients.InventoryClient;
import br.com.tcc.orderservice.clients.PaymentClient;
import br.com.tcc.orderservice.dtos.InventoryRequestDTO;
import br.com.tcc.orderservice.dtos.OrderCreatedEvent;
import br.com.tcc.orderservice.dtos.OrderRequestDTO;
import br.com.tcc.orderservice.dtos.PaymentRequestDTO;
import br.com.tcc.orderservice.enums.OrderStatus;
import br.com.tcc.orderservice.enums.ProcessingType;
import br.com.tcc.orderservice.exceptions.IntegrationException;
import br.com.tcc.orderservice.exceptions.InventoryException;
import br.com.tcc.orderservice.exceptions.PaymentException;
import br.com.tcc.orderservice.messaging.OrderProducer;
import br.com.tcc.orderservice.models.Order;
import br.com.tcc.orderservice.repositories.OrderRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

@ApplicationScoped
public class OrderService {

    private static final Logger LOG = Logger.getLogger(OrderService.class);

    @Inject
    OrderRepository repository;

    @Inject
    OrderProducer producer;

    @RestClient
    InventoryClient inventoryClient;

    @RestClient
    PaymentClient paymentClient;

    @Inject
    OrderService self;

    public void updateStatusWithRetry(Long orderId, OrderStatus status) {
        self.updateStatusRetry(orderId, status);
    }

    @Retry(maxRetries = 3, delay = 2000, abortOn = WebApplicationException.class)
    void updateStatusRetry(Long orderId, OrderStatus status) {
        updateStatus(orderId, status);
    }

    @Transactional
    public Order processOrderSync(OrderRequestDTO dto) {
        LOG.info("Processamento SÍNCRONO iniciado");

        Order order = createInitialOrder(dto);

        try {
            reserveStock(dto.productId());
            processPayment(order.getId(), dto);

            order.setStatus(OrderStatus.CONFIRMED);

        } catch (InventoryException e) {
            LOG.error("Falha no estoque", e);
            order.setStatus(OrderStatus.FAILED_INVENTORY);

        } catch (PaymentException e) {
            LOG.error("Falha no pagamento, iniciando compensação...", e);
            order.setStatus(OrderStatus.FAILED_PAYMENT);
            compensateStock(dto.productId());

        } catch (IntegrationException e) {
            LOG.error("Erro de integração externa", e);
            order.setStatus(OrderStatus.ERROR_INTEGRATION);

        } catch (Exception e) {
            LOG.fatal("Erro inesperado", e);
            order.setStatus(OrderStatus.ERROR_SYSTEM);
        }

        return order;
    }

    public Order processOrderAsync(OrderRequestDTO dto) {
        LOG.info("Recebendo novo pedido assíncrono...");

        Order order = saveOrderInNewTransaction(dto);

        OrderCreatedEvent event = new OrderCreatedEvent(
                order.getId(),
                dto.productId(),
                1,
                dto.customerId(),
                dto.amount()
        );

        producer.publishOrderCreated(event);

        return order;
    }

    @Transactional
    public void updateStatus(Long orderId, OrderStatus status) {
        LOG.info("Atualizando Pedido " + orderId + " para o status: " + status);
        Order order = repository.findById(orderId);

        if (order == null) {
            throw new WebApplicationException("Pedido não encontrado para atualização do status", 404);
        }

        order.setStatus(status);
    }

    @Transactional
    public Order saveOrderInNewTransaction(OrderRequestDTO dto) {
        Order order = new Order();
        order.setCustomerId(dto.customerId());
        order.setTotalAmount(dto.amount());
        order.setStatus(OrderStatus.PENDING);
        order.setProcessingType(ProcessingType.ASYNC);
        repository.persist(order);
        return order;
    }

    private Order createInitialOrder(OrderRequestDTO dto) {
        Order order = new Order();
        order.setCustomerId(dto.customerId());
        order.setTotalAmount(dto.amount());
        order.setStatus(OrderStatus.PROCESSING);
        order.setProcessingType(ProcessingType.SYNC);
        repository.persist(order);
        return order;
    }


    private void reserveStock(Long productId) {
        try {
            inventoryClient.reserveStock(new InventoryRequestDTO(productId, 3));
        } catch (WebApplicationException e) {
            throw new InventoryException("Estoque insuficiente ou erro HTTP", e);
        } catch (ProcessingException e) {
            throw new IntegrationException("Erro de comunicação com estoque", e);
        }
    }


    private void processPayment(Long orderId, OrderRequestDTO dto) {
        try {
            paymentClient.processPayment(
                    new PaymentRequestDTO(orderId, dto.amount(), dto.customerId())
            );
        } catch (WebApplicationException e) {
            throw new PaymentException("Pagamento recusado", e);
        } catch (ProcessingException e) {
            throw new IntegrationException("Erro de comunicação com pagamento", e);
        }
    }


    private void compensateStock(Long productId) {
        try {
            inventoryClient.cancelReservation(new InventoryRequestDTO(productId, 3));
            LOG.warn("Compensação de estoque executada");
        } catch (Exception e) {
            LOG.fatal("FALHA CRÍTICA NA COMPENSAÇÃO", e);
        }
    }
}
package br.com.tcc.orderservice.services;

import br.com.tcc.orderservice.clients.InventoryClient;
import br.com.tcc.orderservice.clients.PaymentClient;
import br.com.tcc.orderservice.dtos.InventoryRequestDTO;
import br.com.tcc.orderservice.dtos.OrderRequestDTO;
import br.com.tcc.orderservice.dtos.PaymentRequestDTO;
import br.com.tcc.orderservice.enums.OrderStatus;
import br.com.tcc.orderservice.enums.ProcessingType;
import br.com.tcc.orderservice.exceptions.IntegrationException;
import br.com.tcc.orderservice.exceptions.InventoryException;
import br.com.tcc.orderservice.exceptions.PaymentException;
import br.com.tcc.orderservice.models.Order;
import br.com.tcc.orderservice.repositories.OrderRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

@ApplicationScoped
public class OrderService {

    private static final Logger LOG = Logger.getLogger(OrderService.class);

    @Inject
    OrderRepository repository;

    @RestClient
    InventoryClient inventoryClient;

    @RestClient
    PaymentClient paymentClient;

    @Transactional
    public Order processOrderSync(OrderRequestDTO dto) {
        LOG.info("Processamento SÍNCRONO iniciado");

        Order order = criarPedidoInicial(dto);

        try {
            reservarEstoque(dto.productId());
            processarPagamento(order.getId(), dto);

            order.setStatus(OrderStatus.CONFIRMED);

        } catch (InventoryException e) {
            LOG.error("Falha no estoque", e);
            order.setStatus(OrderStatus.FAILED_INVENTORY);

        } catch (PaymentException e) {
            LOG.error("Falha no pagamento, iniciando compensação...", e);
            order.setStatus(OrderStatus.FAILED_PAYMENT);
            compensarEstoque(dto.productId());

        } catch (IntegrationException e) {
            LOG.error("Erro de integração externa", e);
            order.setStatus(OrderStatus.ERROR_INTEGRATION);

        } catch (Exception e) {
            LOG.fatal("Erro inesperado", e);
            order.setStatus(OrderStatus.ERROR_SYSTEM);
        }

        return order;
    }

    private Order criarPedidoInicial(OrderRequestDTO dto) {
        Order order = new Order();
        order.setCustomerId(dto.customerId());
        order.setTotalAmount(dto.amount());
        order.setStatus(OrderStatus.PROCESSING);
        order.setProcessingType(ProcessingType.SYNC);
        repository.persist(order);
        return order;
    }


    private void reservarEstoque(Long productId) {
        try {
            inventoryClient.reserveStock(new InventoryRequestDTO(productId, 3));
        } catch (WebApplicationException e) {
            throw new InventoryException("Estoque insuficiente ou erro HTTP", e);
        } catch (ProcessingException e) {
            throw new IntegrationException("Erro de comunicação com estoque", e);
        }
    }


    private void processarPagamento(Long orderId, OrderRequestDTO dto) {
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


    private void compensarEstoque(Long productId) {
        try {
            inventoryClient.cancelReservation(new InventoryRequestDTO(productId, 3));
            LOG.warn("Compensação de estoque executada");
        } catch (Exception e) {
            LOG.fatal("FALHA CRÍTICA NA COMPENSAÇÃO", e);
        }
    }
}
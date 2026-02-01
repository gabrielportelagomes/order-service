package br.com.tcc.orderservice.dtos;

import java.math.BigDecimal;

public record OrderCreatedEvent(
        Long orderId,
        Long productId,
        Integer quantity,
        Long customerId,
        BigDecimal amount
) {
}

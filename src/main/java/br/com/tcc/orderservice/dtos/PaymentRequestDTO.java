package br.com.tcc.orderservice.dtos;

import java.math.BigDecimal;

public record PaymentRequestDTO(Long orderId, BigDecimal amount, Long customerId) {}

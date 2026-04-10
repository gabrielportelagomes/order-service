package br.com.tcc.orderservice.dtos;

import java.math.BigDecimal;

public record OrderRequestDTO(Long customerId, BigDecimal amount, Long productId, Integer quantity) {}

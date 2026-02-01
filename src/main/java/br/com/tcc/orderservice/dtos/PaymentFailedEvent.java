package br.com.tcc.orderservice.dtos;

public record PaymentFailedEvent(Long orderId, Long productId, Integer quantity) {}

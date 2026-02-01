package br.com.tcc.orderservice.dtos;

public record PaymentApprovedEvent(Long orderId, String status) {}

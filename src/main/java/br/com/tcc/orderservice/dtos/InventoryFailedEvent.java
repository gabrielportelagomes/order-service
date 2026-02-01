package br.com.tcc.orderservice.dtos;

public record InventoryFailedEvent(Long orderId, String reason) {}

package br.com.tcc.orderservice.exceptions;

public class InventoryException extends RuntimeException {
    public InventoryException(String msg, Throwable cause) { super(msg, cause); }
}

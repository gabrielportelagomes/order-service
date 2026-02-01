package br.com.tcc.orderservice.exceptions;

public class IntegrationException extends RuntimeException {
    public IntegrationException(String msg, Throwable cause) { super(msg, cause); }
}

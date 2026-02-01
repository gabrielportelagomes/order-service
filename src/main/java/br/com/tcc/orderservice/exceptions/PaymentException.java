package br.com.tcc.orderservice.exceptions;

public class PaymentException extends RuntimeException {
    public PaymentException(String msg, Throwable cause) { super(msg, cause); }
}

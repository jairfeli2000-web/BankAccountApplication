package com.bank.account.exception;

/**
 * Excepción base para errores de reglas de negocio.
 * Todas las excepciones de dominio extienden de esta clase.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}

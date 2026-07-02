package com.bank.account.constants;

public final class ErrorMessages {

    public static final String ACCOUNT_NOT_FOUND = "Cuenta bancaria no encontrada con id: %d";
    public static final String INSUFFICIENT_FUNDS = "Saldo insuficiente para realizar el retiro. Saldo actual: %s, monto solicitado: %s";
    public static final String INVALID_AMOUNT = "El monto debe ser mayor a cero";
    public static final String INVALID_HOLDER_NAME = "El titular debe tener entre 1 y 100 caracteres";
    public static final String VALIDATION_ERROR = "Error de validación en los datos de entrada";
    public static final String INTERNAL_ERROR = "Error interno del servidor";

    private ErrorMessages() {
    }
}

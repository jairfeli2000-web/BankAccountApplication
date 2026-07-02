package com.bank.account.exception;

import com.bank.account.constants.ErrorMessages;

import java.math.BigDecimal;

/**
 * Se lanza cuando un retiro supera el saldo disponible en la cuenta.
 */
public class InsufficientFundsException extends BusinessException {

    public InsufficientFundsException(BigDecimal balance, BigDecimal amount) {
        super(String.format(ErrorMessages.INSUFFICIENT_FUNDS, balance, amount));
    }
}

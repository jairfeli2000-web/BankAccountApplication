package com.bank.account.util;

import java.math.BigDecimal;

/**
 * Utilidad para validaciones de montos monetarios.
 * Centraliza las reglas de negocio relacionadas con cantidades.
 */
public final class AmountValidator {

    private AmountValidator() {
    }

    /**
     * Verifica que el monto sea positivo (mayor a cero).
     * @return true si el monto es válido, false si es null, cero o negativo
     */
    public static boolean isPositive(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Verifica que el saldo sea suficiente para cubrir el monto solicitado.
     * @return true si balance >= amount
     */
    public static boolean hasSufficientFunds(BigDecimal balance, BigDecimal amount) {
        return balance != null && amount != null && balance.compareTo(amount) >= 0;
    }
}

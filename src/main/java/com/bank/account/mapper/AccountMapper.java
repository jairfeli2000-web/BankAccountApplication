package com.bank.account.mapper;

import com.bank.account.dto.response.AccountResponse;
import com.bank.account.dto.response.BalanceResponse;
import com.bank.account.dto.response.TransactionResponse;
import com.bank.account.entity.CuentaBancaria;
import com.bank.account.entity.Transaccion;
import org.springframework.stereotype.Component;

/**
 * Convierte entidades JPA a DTOs de respuesta.
 * Evita exponer la capa de persistencia directamente en la API.
 */
@Component
public class AccountMapper {

    /** Mapea una cuenta bancaria al DTO de creación/consulta */
    public AccountResponse toAccountResponse(CuentaBancaria cuenta) {
        return AccountResponse.builder()
                .id(cuenta.getId())
                .titular(cuenta.getTitular())
                .saldo(cuenta.getSaldo())
                .fechaCreacion(cuenta.getFechaCreacion())
                .build();
    }

    /** Mapea una cuenta bancaria al DTO de consulta de saldo */
    public BalanceResponse toBalanceResponse(CuentaBancaria cuenta) {
        return BalanceResponse.builder()
                .id(cuenta.getId())
                .titular(cuenta.getTitular())
                .saldo(cuenta.getSaldo())
                .build();
    }

    /** Mapea una transacción al DTO de respuesta incluyendo datos de la cuenta */
    public TransactionResponse toTransactionResponse(Transaccion transaccion) {
        CuentaBancaria cuenta = transaccion.getCuentaBancaria();
        return TransactionResponse.builder()
                .transaccionId(transaccion.getId())
                .cuentaId(cuenta.getId())
                .titular(cuenta.getTitular())
                .tipo(transaccion.getTipo())
                .monto(transaccion.getMonto())
                .saldo(cuenta.getSaldo())
                .fecha(transaccion.getFecha())
                .build();
    }
}

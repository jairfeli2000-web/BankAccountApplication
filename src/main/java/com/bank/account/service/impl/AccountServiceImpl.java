package com.bank.account.service.impl;

import com.bank.account.constants.ErrorMessages;
import com.bank.account.dto.request.CreateAccountRequest;
import com.bank.account.dto.request.TransactionRequest;
import com.bank.account.dto.response.AccountResponse;
import com.bank.account.dto.response.BalanceResponse;
import com.bank.account.dto.response.TransactionResponse;
import com.bank.account.entity.CuentaBancaria;
import com.bank.account.entity.Transaccion;
import com.bank.account.enums.TipoTransaccion;
import com.bank.account.exception.AccountNotFoundException;
import com.bank.account.exception.BusinessException;
import com.bank.account.exception.InsufficientFundsException;
import com.bank.account.mapper.AccountMapper;
import com.bank.account.repository.CuentaBancariaRepository;
import com.bank.account.repository.TransaccionRepository;
import com.bank.account.service.AccountService;
import com.bank.account.util.AmountValidator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Implementación de la lógica de negocio para cuentas bancarias.
 * Responsable de validar reglas de negocio, persistir cambios y registrar transacciones.
 */
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);

    private final CuentaBancariaRepository cuentaBancariaRepository;
    private final TransaccionRepository transaccionRepository;
    private final AccountMapper accountMapper;

    /**
     * Crea una nueva cuenta bancaria con saldo inicial en cero.
     * Regla de negocio: toda cuenta nueva inicia con saldo = 0.
     */
    @Override
    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        log.info("[CREAR_CUENTA] Inicio - titular: {}", request.getTitular());

        try {
            CuentaBancaria cuenta = CuentaBancaria.builder()
                    .titular(request.getTitular())
                    .saldo(BigDecimal.ZERO) // Saldo inicial obligatorio en cero
                    .build();

            CuentaBancaria savedAccount = cuentaBancariaRepository.save(cuenta);
            log.info("[CREAR_CUENTA] Éxito - id: {}, titular: {}, saldo: {}",
                    savedAccount.getId(), savedAccount.getTitular(), savedAccount.getSaldo());

            return accountMapper.toAccountResponse(savedAccount);
        } catch (DataAccessException ex) {
            log.error("[CREAR_CUENTA] Fallo al persistir en base de datos - titular: {}. Causa: {}",
                    request.getTitular(), ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * Registra una consignación (depósito) en la cuenta indicada.
     */
    @Override
    @Transactional
    public TransactionResponse deposit(Long accountId, TransactionRequest request) {
        log.info("[DEPOSITO] Inicio - cuentaId: {}, monto: {}", accountId, request.getMonto());
        return processTransaction(accountId, request, TipoTransaccion.DEPOSITO);
    }

    /**
     * Registra un retiro en la cuenta indicada.
     * Valida que exista saldo suficiente antes de procesar.
     */
    @Override
    @Transactional
    public TransactionResponse withdraw(Long accountId, TransactionRequest request) {
        log.info("[RETIRO] Inicio - cuentaId: {}, monto: {}", accountId, request.getMonto());
        return processTransaction(accountId, request, TipoTransaccion.RETIRO);
    }

    /**
     * Consulta el saldo actual de una cuenta existente.
     */
    @Override
    @Transactional(readOnly = true)
    public BalanceResponse getBalance(Long accountId) {
        log.info("[CONSULTA_SALDO] Inicio - cuentaId: {}", accountId);

        try {
            CuentaBancaria cuenta = findAccountById(accountId);
            log.info("[CONSULTA_SALDO] Éxito - cuentaId: {}, titular: {}, saldo: {}",
                    accountId, cuenta.getTitular(), cuenta.getSaldo());
            return accountMapper.toBalanceResponse(cuenta);
        } catch (AccountNotFoundException ex) {
            log.warn("[CONSULTA_SALDO] Fallo - cuentaId: {} no existe", accountId);
            throw ex;
        }
    }

    /**
     * Procesa una transacción (depósito o retiro) de forma atómica.
     * 1. Valida el monto
     * 2. Busca la cuenta
     * 3. Verifica saldo (solo retiros)
     * 4. Actualiza saldo y registra la transacción
     */
    private TransactionResponse processTransaction(Long accountId, TransactionRequest request, TipoTransaccion tipo) {
        validateAmount(request.getMonto());
        CuentaBancaria cuenta = findAccountById(accountId);

        // Regla de negocio: no permitir retiros con saldo insuficiente
        if (tipo == TipoTransaccion.RETIRO && !AmountValidator.hasSufficientFunds(cuenta.getSaldo(), request.getMonto())) {
            log.warn("[{}] Fallo - saldo insuficiente. cuentaId: {}, saldoActual: {}, montoSolicitado: {}",
                    tipo, accountId, cuenta.getSaldo(), request.getMonto());
            throw new InsufficientFundsException(cuenta.getSaldo(), request.getMonto());
        }

        // Calcula el nuevo saldo según el tipo de transacción
        BigDecimal newBalance = tipo == TipoTransaccion.DEPOSITO
                ? cuenta.getSaldo().add(request.getMonto())
                : cuenta.getSaldo().subtract(request.getMonto());

        cuenta.setSaldo(newBalance);

        try {
            Transaccion transaccion = Transaccion.builder()
                    .cuentaBancaria(cuenta)
                    .tipo(tipo)
                    .monto(request.getMonto())
                    .build();

            Transaccion savedTransaction = transaccionRepository.save(transaccion);
            cuentaBancariaRepository.save(cuenta);

            log.info("[{}] Éxito - transaccionId: {}, cuentaId: {}, monto: {}, saldoAnterior: {}, saldoNuevo: {}",
                    tipo, savedTransaction.getId(), accountId, request.getMonto(),
                    tipo == TipoTransaccion.DEPOSITO
                            ? newBalance.subtract(request.getMonto())
                            : newBalance.add(request.getMonto()),
                    newBalance);

            return accountMapper.toTransactionResponse(savedTransaction);
        } catch (DataAccessException ex) {
            log.error("[{}] Fallo al persistir transacción - cuentaId: {}, monto: {}. Causa: {}",
                    tipo, accountId, request.getMonto(), ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * Busca una cuenta por ID o lanza excepción si no existe.
     */
    private CuentaBancaria findAccountById(Long accountId) {
        return cuentaBancariaRepository.findById(accountId)
                .orElseThrow(() -> {
                    log.warn("[BUSCAR_CUENTA] Fallo - no existe cuenta con id: {}", accountId);
                    return new AccountNotFoundException(accountId);
                });
    }

    /**
     * Valida que el monto sea positivo (mayor a cero).
     */
    private void validateAmount(BigDecimal amount) {
        if (!AmountValidator.isPositive(amount)) {
            log.warn("[VALIDAR_MONTO] Fallo - monto inválido: {}. Regla: debe ser > 0", amount);
            throw new BusinessException(ErrorMessages.INVALID_AMOUNT);
        }
    }
}

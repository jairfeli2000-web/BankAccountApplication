package com.bank.account.controller;

import com.bank.account.constants.ApiConstants;
import com.bank.account.dto.request.CreateAccountRequest;
import com.bank.account.dto.request.TransactionRequest;
import com.bank.account.dto.response.AccountResponse;
import com.bank.account.dto.response.BalanceResponse;
import com.bank.account.dto.response.TransactionResponse;
import com.bank.account.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST que expone los endpoints de gestión de cuentas bancarias.
 * Delega toda la lógica de negocio al {@link AccountService}.
 */
@RestController
@RequestMapping(ApiConstants.API_BASE_PATH)
@RequiredArgsConstructor
@Tag(name = "Cuentas Bancarias", description = "API para gestión de cuentas bancarias")
public class AccountController {

    private static final Logger log = LoggerFactory.getLogger(AccountController.class);

    private final AccountService accountService;

    @PostMapping
    @Operation(summary = "Crear cuenta bancaria", description = "Crea una nueva cuenta bancaria con saldo inicial en 0")
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        log.debug("[API] POST /accounts - titular: {}", request.getTitular());
        AccountResponse response = accountService.createAccount(request);
        log.debug("[API] POST /accounts - respuesta 201, cuentaId: {}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(ApiConstants.DEPOSIT_PATH)
    @Operation(summary = "Realizar consignación", description = "Realiza una consignación en una cuenta bancaria")
    public ResponseEntity<TransactionResponse> deposit(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request) {
        log.debug("[API] POST /accounts/{}/deposit - monto: {}", id, request.getMonto());
        TransactionResponse response = accountService.deposit(id, request);
        log.debug("[API] POST /accounts/{}/deposit - respuesta 200, transaccionId: {}", id, response.getTransaccionId());
        return ResponseEntity.ok(response);
    }

    @PostMapping(ApiConstants.WITHDRAW_PATH)
    @Operation(summary = "Realizar retiro", description = "Realiza un retiro de una cuenta bancaria")
    public ResponseEntity<TransactionResponse> withdraw(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request) {
        log.debug("[API] POST /accounts/{}/withdraw - monto: {}", id, request.getMonto());
        TransactionResponse response = accountService.withdraw(id, request);
        log.debug("[API] POST /accounts/{}/withdraw - respuesta 200, transaccionId: {}", id, response.getTransaccionId());
        return ResponseEntity.ok(response);
    }

    @GetMapping(ApiConstants.BALANCE_PATH)
    @Operation(summary = "Consultar saldo", description = "Consulta el saldo de una cuenta bancaria")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable Long id) {
        log.debug("[API] GET /accounts/{}/balance", id);
        BalanceResponse response = accountService.getBalance(id);
        log.debug("[API] GET /accounts/{}/balance - respuesta 200, saldo: {}", id, response.getSaldo());
        return ResponseEntity.ok(response);
    }
}

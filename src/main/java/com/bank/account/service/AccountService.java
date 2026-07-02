package com.bank.account.service;

import com.bank.account.dto.request.CreateAccountRequest;
import com.bank.account.dto.request.TransactionRequest;
import com.bank.account.dto.response.AccountResponse;
import com.bank.account.dto.response.BalanceResponse;
import com.bank.account.dto.response.TransactionResponse;

/**
 * Contrato del servicio de cuentas bancarias.
 * Define las operaciones de negocio disponibles (SOLID - Interface Segregation).
 */
public interface AccountService {

    AccountResponse createAccount(CreateAccountRequest request);

    TransactionResponse deposit(Long accountId, TransactionRequest request);

    TransactionResponse withdraw(Long accountId, TransactionRequest request);

    BalanceResponse getBalance(Long accountId);
}

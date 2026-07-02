package com.bank.account.exception;

import com.bank.account.constants.ErrorMessages;

/**
 * Se lanza cuando se intenta operar sobre una cuenta que no existe.
 */
public class AccountNotFoundException extends BusinessException {

    public AccountNotFoundException(Long accountId) {
        super(String.format(ErrorMessages.ACCOUNT_NOT_FOUND, accountId));
    }
}

package com.bank.account.constants;

public final class ApiConstants {

    public static final String API_BASE_PATH = "/accounts";
    public static final String DEPOSIT_PATH = "/{id}/deposit";
    public static final String WITHDRAW_PATH = "/{id}/withdraw";
    public static final String BALANCE_PATH = "/{id}/balance";

    private ApiConstants() {
    }
}

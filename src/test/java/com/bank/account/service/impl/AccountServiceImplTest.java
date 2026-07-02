package com.bank.account.service.impl;

import com.bank.account.dto.request.CreateAccountRequest;
import com.bank.account.dto.request.TransactionRequest;
import com.bank.account.dto.response.AccountResponse;
import com.bank.account.dto.response.TransactionResponse;
import com.bank.account.entity.CuentaBancaria;
import com.bank.account.entity.Transaccion;
import com.bank.account.enums.TipoTransaccion;
import com.bank.account.exception.AccountNotFoundException;
import com.bank.account.exception.InsufficientFundsException;
import com.bank.account.mapper.AccountMapper;
import com.bank.account.repository.CuentaBancariaRepository;
import com.bank.account.repository.TransaccionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private CuentaBancariaRepository cuentaBancariaRepository;

    @Mock
    private TransaccionRepository transaccionRepository;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AccountServiceImpl accountService;

    private CuentaBancaria cuentaBancaria;
    private CreateAccountRequest createAccountRequest;

    @BeforeEach
    void setUp() {
        cuentaBancaria = CuentaBancaria.builder()
                .id(1L)
                .titular("Juan Pérez")
                .saldo(BigDecimal.ZERO)
                .fechaCreacion(LocalDateTime.now())
                .build();

        createAccountRequest = CreateAccountRequest.builder()
                .titular("Juan Pérez")
                .build();
    }

    @Test
    @DisplayName("Debe crear una cuenta bancaria con saldo inicial en cero")
    void shouldCreateAccountWithZeroBalance() {
        AccountResponse expectedResponse = AccountResponse.builder()
                .id(1L)
                .titular("Juan Pérez")
                .saldo(BigDecimal.ZERO)
                .fechaCreacion(LocalDateTime.now())
                .build();

        when(cuentaBancariaRepository.save(any(CuentaBancaria.class))).thenReturn(cuentaBancaria);
        when(accountMapper.toAccountResponse(cuentaBancaria)).thenReturn(expectedResponse);

        AccountResponse result = accountService.createAccount(createAccountRequest);

        ArgumentCaptor<CuentaBancaria> captor = ArgumentCaptor.forClass(CuentaBancaria.class);
        verify(cuentaBancariaRepository).save(captor.capture());

        CuentaBancaria savedAccount = captor.getValue();
        assertThat(savedAccount.getTitular()).isEqualTo("Juan Pérez");
        assertThat(savedAccount.getSaldo()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getSaldo()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getTitular()).isEqualTo("Juan Pérez");
    }

    @Test
    @DisplayName("Debe realizar un retiro exitosamente cuando hay saldo suficiente")
    void shouldWithdrawSuccessfullyWhenSufficientFunds() {
        cuentaBancaria.setSaldo(new BigDecimal("500.00"));
        TransactionRequest request = TransactionRequest.builder()
                .monto(new BigDecimal("200.00"))
                .build();

        Transaccion transaccion = Transaccion.builder()
                .id(1L)
                .cuentaBancaria(cuentaBancaria)
                .tipo(TipoTransaccion.RETIRO)
                .monto(new BigDecimal("200.00"))
                .fecha(LocalDateTime.now())
                .build();

        TransactionResponse expectedResponse = TransactionResponse.builder()
                .transaccionId(1L)
                .cuentaId(1L)
                .titular("Juan Pérez")
                .tipo(TipoTransaccion.RETIRO)
                .monto(new BigDecimal("200.00"))
                .saldo(new BigDecimal("300.00"))
                .fecha(LocalDateTime.now())
                .build();

        when(cuentaBancariaRepository.findById(1L)).thenReturn(Optional.of(cuentaBancaria));
        when(transaccionRepository.save(any(Transaccion.class))).thenReturn(transaccion);
        when(cuentaBancariaRepository.save(cuentaBancaria)).thenReturn(cuentaBancaria);
        when(accountMapper.toTransactionResponse(transaccion)).thenReturn(expectedResponse);

        TransactionResponse result = accountService.withdraw(1L, request);

        assertThat(result.getSaldo()).isEqualByComparingTo("300.00");
        assertThat(result.getTipo()).isEqualTo(TipoTransaccion.RETIRO);
        assertThat(cuentaBancaria.getSaldo()).isEqualByComparingTo("300.00");
        verify(transaccionRepository).save(any(Transaccion.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el saldo es insuficiente para retiro")
    void shouldThrowExceptionWhenInsufficientFundsForWithdraw() {
        cuentaBancaria.setSaldo(new BigDecimal("100.00"));
        TransactionRequest request = TransactionRequest.builder()
                .monto(new BigDecimal("200.00"))
                .build();

        when(cuentaBancariaRepository.findById(1L)).thenReturn(Optional.of(cuentaBancaria));

        assertThatThrownBy(() -> accountService.withdraw(1L, request))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessageContaining("Saldo insuficiente");

        verify(transaccionRepository, never()).save(any(Transaccion.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la cuenta no existe al retirar")
    void shouldThrowExceptionWhenAccountNotFoundOnWithdraw() {
        TransactionRequest request = TransactionRequest.builder()
                .monto(new BigDecimal("100.00"))
                .build();

        when(cuentaBancariaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.withdraw(99L, request))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("Cuenta bancaria no encontrada");
    }
}

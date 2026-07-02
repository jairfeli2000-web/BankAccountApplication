package com.bank.account.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Respuesta con el saldo de una cuenta bancaria")
public class BalanceResponse {

    @Schema(description = "Identificador de la cuenta", example = "1")
    private Long id;

    @Schema(description = "Nombre del titular", example = "Juan Pérez")
    private String titular;

    @Schema(description = "Saldo actual de la cuenta", example = "250.75")
    private BigDecimal saldo;
}

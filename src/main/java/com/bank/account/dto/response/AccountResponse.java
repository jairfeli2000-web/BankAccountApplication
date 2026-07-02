package com.bank.account.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Respuesta con los datos de una cuenta bancaria")
public class AccountResponse {

    @Schema(description = "Identificador de la cuenta", example = "1")
    private Long id;

    @Schema(description = "Nombre del titular", example = "Juan Pérez")
    private String titular;

    @Schema(description = "Saldo actual de la cuenta", example = "0.00")
    private BigDecimal saldo;

    @Schema(description = "Fecha de creación de la cuenta")
    private LocalDateTime fechaCreacion;
}

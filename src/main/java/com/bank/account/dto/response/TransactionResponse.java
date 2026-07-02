package com.bank.account.dto.response;

import com.bank.account.enums.TipoTransaccion;
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
@Schema(description = "Respuesta de una transacción realizada")
public class TransactionResponse {

    @Schema(description = "Identificador de la transacción", example = "1")
    private Long transaccionId;

    @Schema(description = "Identificador de la cuenta", example = "1")
    private Long cuentaId;

    @Schema(description = "Nombre del titular", example = "Juan Pérez")
    private String titular;

    @Schema(description = "Tipo de transacción", example = "DEPOSITO")
    private TipoTransaccion tipo;

    @Schema(description = "Monto de la transacción", example = "150.50")
    private BigDecimal monto;

    @Schema(description = "Saldo resultante después de la transacción", example = "150.50")
    private BigDecimal saldo;

    @Schema(description = "Fecha de la transacción")
    private LocalDateTime fecha;
}

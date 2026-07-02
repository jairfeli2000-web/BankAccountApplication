package com.bank.account.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Respuesta estándar de error")
public class ErrorResponse {

    @Schema(description = "Código HTTP del error", example = "400")
    private int status;

    @Schema(description = "Mensaje descriptivo del error", example = "Error de validación")
    private String message;

    @Schema(description = "Detalle de errores por campo")
    private Map<String, String> errors;

    @Schema(description = "Fecha y hora del error")
    private LocalDateTime timestamp;
}

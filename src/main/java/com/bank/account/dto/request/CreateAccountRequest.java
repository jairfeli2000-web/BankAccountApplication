package com.bank.account.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Solicitud para crear una nueva cuenta bancaria")
public class CreateAccountRequest {

    @NotBlank(message = "El titular es obligatorio")
    @Size(min = 1, max = 100, message = "El titular debe tener entre 1 y 100 caracteres")
    @Schema(description = "Nombre del titular de la cuenta", example = "Juan Pérez")
    private String titular;
}

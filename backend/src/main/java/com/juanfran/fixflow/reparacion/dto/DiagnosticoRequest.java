package com.juanfran.fixflow.reparacion.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record DiagnosticoRequest(
        @NotBlank(message = "Escribe el diagnóstico") @Size(max = 4000) String diagnostico,
        @NotNull(message = "Indica el presupuesto")
        @PositiveOrZero(message = "El presupuesto no puede ser negativo")
        @Digits(integer = 8, fraction = 2) BigDecimal presupuesto
) {
}

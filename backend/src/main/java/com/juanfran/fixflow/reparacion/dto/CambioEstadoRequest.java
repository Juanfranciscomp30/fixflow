package com.juanfran.fixflow.reparacion.dto;

import com.juanfran.fixflow.reparacion.EstadoReparacion;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** precioFinal solo se usa al pasar a ENTREGADO. */
public record CambioEstadoRequest(
        @NotNull(message = "Indica el nuevo estado") EstadoReparacion estado,
        @Size(max = 1000) String comentario,
        @PositiveOrZero @Digits(integer = 8, fraction = 2) BigDecimal precioFinal
) {
}

package com.juanfran.fixflow.reparacion.dto;

import jakarta.validation.constraints.NotNull;

public record RespuestaPresupuestoRequest(
        @NotNull(message = "Indica si el cliente acepta el presupuesto") Boolean aceptado
) {
}

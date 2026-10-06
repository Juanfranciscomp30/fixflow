package com.juanfran.fixflow.reparacion.dto;

import jakarta.validation.constraints.NotNull;

public record AsignarTecnicoRequest(
    @NotNull(message = "Indica el técnico") Long tecnicoId
) {
}

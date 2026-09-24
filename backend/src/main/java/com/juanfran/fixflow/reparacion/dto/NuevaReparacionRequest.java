package com.juanfran.fixflow.reparacion.dto;

import com.juanfran.fixflow.equipo.TipoEquipo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Lo que se rellena en el mostrador cuando entra un equipo. */
public record NuevaReparacionRequest(
        @NotNull @Valid DatosCliente cliente,
        @NotNull @Valid DatosEquipo equipo,
        @NotBlank(message = "Describe la avería") @Size(max = 2000) String averiaDescrita
) {

    public record DatosCliente(
            @NotBlank(message = "El nombre es obligatorio") @Size(max = 100) String nombre,
            @NotBlank(message = "El teléfono es obligatorio")
            @Pattern(regexp = "^[+0-9 ]{9,20}$", message = "Teléfono no válido") String telefono,
            @Email(message = "Email no válido") @Size(max = 150) String email
    ) {
    }

    public record DatosEquipo(
            @NotNull(message = "Indica el tipo de equipo") TipoEquipo tipo,
            @NotBlank(message = "La marca es obligatoria") @Size(max = 50) String marca,
            @Size(max = 100) String modelo,
            @Size(max = 100) String numeroSerie
    ) {
    }
}

package com.juanfran.fixflow.reparacion;

import java.util.EnumSet;
import java.util.Set;

/**
 * Estados por los que pasa una reparación y qué saltos están permitidos.
 * La regla vive aquí para que ni el controlador ni el frontend puedan
 * saltarse pasos (por ejemplo, reparar sin que el cliente acepte el presupuesto).
 */
public enum EstadoReparacion {

    RECIBIDO,
    DIAGNOSTICO,
    ESPERANDO_APROBACION,
    EN_REPARACION,
    LISTO,
    ENTREGADO;

    public Set<EstadoReparacion> siguientesPermitidos() {
        return switch (this) {
            case RECIBIDO             -> EnumSet.of(DIAGNOSTICO);
            case DIAGNOSTICO          -> EnumSet.of(ESPERANDO_APROBACION);
            // Aceptado -> se repara. Rechazado -> se deja listo para devolver sin reparar.
            case ESPERANDO_APROBACION -> EnumSet.of(EN_REPARACION, LISTO);
            case EN_REPARACION        -> EnumSet.of(LISTO);
            case LISTO                -> EnumSet.of(ENTREGADO);
            case ENTREGADO            -> EnumSet.noneOf(EstadoReparacion.class);
        };
    }

    public boolean puedePasarA(EstadoReparacion destino) {
        return siguientesPermitidos().contains(destino);
    }
}

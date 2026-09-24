package com.juanfran.fixflow.reparacion.dto;

import com.juanfran.fixflow.equipo.Equipo;
import com.juanfran.fixflow.equipo.TipoEquipo;
import com.juanfran.fixflow.reparacion.EstadoReparacion;
import com.juanfran.fixflow.reparacion.Reparacion;

import java.time.Instant;

/** Una fila del listado (y, más adelante, una tarjeta del Kanban). */
public record ReparacionResumen(
        Long id,
        String codigo,
        EstadoReparacion estado,
        String clienteNombre,
        TipoEquipo tipoEquipo,
        String equipo,
        String averiaDescrita,
        String tecnicoNombre,
        Instant fechaEntrada
) {

    public static ReparacionResumen de(Reparacion r) {
        Equipo e = r.getEquipo();
        return new ReparacionResumen(
                r.getId(),
                r.getCodigo(),
                r.getEstado(),
                e.getCliente().getNombre(),
                e.getTipo(),
                e.getModelo() == null ? e.getMarca() : e.getMarca() + " " + e.getModelo(),
                r.getAveriaDescrita(),
                r.getTecnico() == null ? null : r.getTecnico().getNombre(),
                r.getFechaEntrada());
    }
}

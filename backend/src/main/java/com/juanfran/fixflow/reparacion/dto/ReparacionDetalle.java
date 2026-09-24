package com.juanfran.fixflow.reparacion.dto;

import com.juanfran.fixflow.cliente.Cliente;
import com.juanfran.fixflow.equipo.Equipo;
import com.juanfran.fixflow.equipo.TipoEquipo;
import com.juanfran.fixflow.reparacion.EstadoReparacion;
import com.juanfran.fixflow.reparacion.HistorialEstado;
import com.juanfran.fixflow.reparacion.Reparacion;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

/** Ficha completa de una reparación, con su historial y los estados a los que puede pasar. */
public record ReparacionDetalle(
        Long id,
        String codigo,
        EstadoReparacion estado,
        Set<EstadoReparacion> siguientesEstados,
        ClienteDto cliente,
        EquipoDto equipo,
        String averiaDescrita,
        String diagnostico,
        BigDecimal presupuesto,
        Boolean presupuestoAceptado,
        BigDecimal precioFinal,
        String tecnicoNombre,
        Instant fechaEntrada,
        Instant fechaListo,
        Instant fechaEntrega,
        List<HistorialDto> historial
) {

    public record ClienteDto(Long id, String nombre, String telefono, String email) {
        static ClienteDto de(Cliente c) {
            return new ClienteDto(c.getId(), c.getNombre(), c.getTelefono(), c.getEmail());
        }
    }

    public record EquipoDto(Long id, TipoEquipo tipo, String marca, String modelo, String numeroSerie) {
        static EquipoDto de(Equipo e) {
            return new EquipoDto(e.getId(), e.getTipo(), e.getMarca(), e.getModelo(), e.getNumeroSerie());
        }
    }

    public record HistorialDto(EstadoReparacion estadoAnterior, EstadoReparacion estadoNuevo,
                               String usuarioNombre, String comentario, Instant fecha) {
        static HistorialDto de(HistorialEstado h) {
            return new HistorialDto(h.getEstadoAnterior(), h.getEstadoNuevo(),
                    h.getUsuario() == null ? null : h.getUsuario().getNombre(),
                    h.getComentario(), h.getFecha());
        }
    }

    public static ReparacionDetalle de(Reparacion r, List<HistorialEstado> historial) {
        return new ReparacionDetalle(
                r.getId(),
                r.getCodigo(),
                r.getEstado(),
                r.getEstado().siguientesPermitidos(),
                ClienteDto.de(r.getEquipo().getCliente()),
                EquipoDto.de(r.getEquipo()),
                r.getAveriaDescrita(),
                r.getDiagnostico(),
                r.getPresupuesto(),
                r.getPresupuestoAceptado(),
                r.getPrecioFinal(),
                r.getTecnico() == null ? null : r.getTecnico().getNombre(),
                r.getFechaEntrada(),
                r.getFechaListo(),
                r.getFechaEntrega(),
                historial.stream().map(HistorialDto::de).toList());
    }
}

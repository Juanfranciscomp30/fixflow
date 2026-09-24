package com.juanfran.fixflow.reparacion;

import com.juanfran.fixflow.cliente.Cliente;
import com.juanfran.fixflow.cliente.ClienteRepository;
import com.juanfran.fixflow.common.RecursoNoEncontradoException;
import com.juanfran.fixflow.common.ReglaDeNegocioException;
import com.juanfran.fixflow.equipo.Equipo;
import com.juanfran.fixflow.equipo.EquipoRepository;
import com.juanfran.fixflow.reparacion.dto.*;
import com.juanfran.fixflow.usuario.Usuario;
import com.juanfran.fixflow.usuario.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Year;
import java.util.List;

@Service
@Transactional
public class ReparacionService {

    private final ReparacionRepository reparaciones;
    private final HistorialEstadoRepository historial;
    private final ClienteRepository clientes;
    private final EquipoRepository equipos;
    private final UsuarioRepository usuarios;
    private final Clock reloj;

    public ReparacionService(ReparacionRepository reparaciones, HistorialEstadoRepository historial,
                             ClienteRepository clientes, EquipoRepository equipos,
                             UsuarioRepository usuarios, Clock reloj) {
        this.reparaciones = reparaciones;
        this.historial = historial;
        this.clientes = clientes;
        this.equipos = equipos;
        this.usuarios = usuarios;
        this.reloj = reloj;
    }

    /** Alta en el mostrador: cliente + equipo + avería en un solo paso. */
    public ReparacionDetalle recibir(NuevaReparacionRequest req, Long usuarioId) {
        var datosCliente = req.cliente();
        Cliente cliente = clientes.save(new Cliente(
                datosCliente.nombre().trim(), datosCliente.telefono().trim(), vacioANull(datosCliente.email())));

        var datosEquipo = req.equipo();
        Equipo equipo = equipos.save(new Equipo(cliente, datosEquipo.tipo(), datosEquipo.marca().trim(),
                vacioANull(datosEquipo.modelo()), vacioANull(datosEquipo.numeroSerie())));

        Reparacion reparacion = reparaciones.save(
                new Reparacion(generarCodigo(), equipo, req.averiaDescrita().trim(), null));
        historial.save(new HistorialEstado(reparacion, null, EstadoReparacion.RECIBIDO,
                usuario(usuarioId), "Equipo recibido en mostrador", reloj.instant()));
        return detalle(reparacion);
    }

    @Transactional(readOnly = true)
    public List<ReparacionResumen> listar(EstadoReparacion estado) {
        List<Reparacion> resultado = estado == null
                ? reparaciones.findAllByOrderByFechaEntradaDesc()
                : reparaciones.findByEstadoOrderByFechaEntradaDesc(estado);
        return resultado.stream().map(ReparacionResumen::de).toList();
    }

    @Transactional(readOnly = true)
    public ReparacionDetalle obtener(Long id) {
        return detalle(buscar(id));
    }

    public ReparacionDetalle registrarDiagnostico(Long id, DiagnosticoRequest req) {
        Reparacion reparacion = buscar(id);
        if (reparacion.getEstado() != EstadoReparacion.RECIBIDO
                && reparacion.getEstado() != EstadoReparacion.DIAGNOSTICO) {
            throw new ReglaDeNegocioException("El diagnóstico solo se puede modificar antes de enviar el presupuesto");
        }
        reparacion.registrarDiagnostico(req.diagnostico().trim());
        reparacion.fijarPresupuesto(req.presupuesto());
        return detalle(reparacion);
    }

    public ReparacionDetalle cambiarEstado(Long id, CambioEstadoRequest req, Long usuarioId) {
        Reparacion reparacion = buscar(id);
        cambiar(reparacion, req.estado(), usuarioId, vacioANull(req.comentario()));
        if (req.estado() == EstadoReparacion.ENTREGADO) {
            reparacion.fijarPrecioFinal(req.precioFinal());
        }
        return detalle(reparacion);
    }

    /** El cliente acepta o rechaza el presupuesto y la reparación avanza sola. */
    public ReparacionDetalle responderPresupuesto(Long id, RespuestaPresupuestoRequest req, Long usuarioId) {
        Reparacion reparacion = buscar(id);
        reparacion.responderPresupuesto(req.aceptado());
        if (req.aceptado()) {
            cambiar(reparacion, EstadoReparacion.EN_REPARACION, usuarioId, "Presupuesto aceptado por el cliente");
        } else {
            cambiar(reparacion, EstadoReparacion.LISTO, usuarioId, "Presupuesto rechazado: se devuelve sin reparar");
        }
        return detalle(reparacion);
    }

    /** FX-2026-00042: año de entrada + número correlativo de la secuencia. */
    String generarCodigo() {
        return "FX-%d-%05d".formatted(Year.now(reloj).getValue(), reparaciones.siguienteNumeroCodigo());
    }

    // Todo cambio de estado pasa por aquí para que siempre quede en el historial
    private void cambiar(Reparacion reparacion, EstadoReparacion nuevo, Long usuarioId, String comentario) {
        EstadoReparacion anterior = reparacion.getEstado();
        reparacion.cambiarEstado(nuevo, reloj.instant());
        historial.save(new HistorialEstado(reparacion, anterior, nuevo, usuario(usuarioId), comentario, reloj.instant()));
    }

    private ReparacionDetalle detalle(Reparacion reparacion) {
        return ReparacionDetalle.de(reparacion, historial.findByReparacionIdOrderByFechaAsc(reparacion.getId()));
    }

    private Reparacion buscar(Long id) {
        return reparaciones.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la reparación " + id));
    }

    // Referencia sin consulta: solo hace falta el id para la clave foránea
    private Usuario usuario(Long usuarioId) {
        return usuarioId == null ? null : usuarios.getReferenceById(usuarioId);
    }

    private static String vacioANull(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}

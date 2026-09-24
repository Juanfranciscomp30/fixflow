package com.juanfran.fixflow.reparacion;

import com.juanfran.fixflow.cliente.Cliente;
import com.juanfran.fixflow.cliente.ClienteRepository;
import com.juanfran.fixflow.common.RecursoNoEncontradoException;
import com.juanfran.fixflow.common.ReglaDeNegocioException;
import com.juanfran.fixflow.equipo.Equipo;
import com.juanfran.fixflow.equipo.EquipoRepository;
import com.juanfran.fixflow.equipo.TipoEquipo;
import com.juanfran.fixflow.reparacion.dto.CambioEstadoRequest;
import com.juanfran.fixflow.reparacion.dto.DiagnosticoRequest;
import com.juanfran.fixflow.reparacion.dto.NuevaReparacionRequest;
import com.juanfran.fixflow.reparacion.dto.NuevaReparacionRequest.DatosCliente;
import com.juanfran.fixflow.reparacion.dto.NuevaReparacionRequest.DatosEquipo;
import com.juanfran.fixflow.reparacion.dto.RespuestaPresupuestoRequest;
import com.juanfran.fixflow.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReparacionServiceTest {

    private static final Instant AHORA = Instant.parse("2026-03-15T10:00:00Z");

    private final ReparacionRepository reparaciones = mock(ReparacionRepository.class);
    private final HistorialEstadoRepository historial = mock(HistorialEstadoRepository.class);
    private final ClienteRepository clientes = mock(ClienteRepository.class);
    private final EquipoRepository equipos = mock(EquipoRepository.class);
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);

    private ReparacionService service;

    @BeforeEach
    void setUp() {
        service = new ReparacionService(reparaciones, historial, clientes, equipos, usuarios,
                Clock.fixed(AHORA, ZoneOffset.UTC));
        // Los save devuelven la misma entidad que reciben
        when(clientes.save(any())).thenAnswer(i -> i.getArgument(0));
        when(equipos.save(any())).thenAnswer(i -> i.getArgument(0));
        when(reparaciones.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void elCodigoLlevaElAnoYElNumeroConCeros() {
        when(reparaciones.siguienteNumeroCodigo()).thenReturn(42L);

        assertThat(service.generarCodigo()).isEqualTo("FX-2026-00042");
    }

    @Test
    void recibirCreaClienteEquipoYReparacionLimpiandoCamposVacios() {
        when(reparaciones.siguienteNumeroCodigo()).thenReturn(22L);
        var req = new NuevaReparacionRequest(
                new DatosCliente("  Ana López ", "600111222", "   "),
                new DatosEquipo(TipoEquipo.MOVIL, "Samsung", "Galaxy S21", ""),
                "Pantalla rota");

        var detalle = service.recibir(req, 2L);

        assertThat(detalle.codigo()).isEqualTo("FX-2026-00022");
        assertThat(detalle.estado()).isEqualTo(EstadoReparacion.RECIBIDO);
        assertThat(detalle.cliente().nombre()).isEqualTo("Ana López");
        assertThat(detalle.cliente().email()).isNull();
        assertThat(detalle.equipo().numeroSerie()).isNull();
        verify(historial).save(any(HistorialEstado.class));
    }

    @Test
    void cadaCambioDeEstadoQuedaEnElHistorial() {
        Reparacion reparacion = reparacionEn(EstadoReparacion.RECIBIDO);

        service.cambiarEstado(1L, new CambioEstadoRequest(EstadoReparacion.DIAGNOSTICO, "  ", null), 3L);

        var linea = ArgumentCaptor.forClass(HistorialEstado.class);
        verify(historial).save(linea.capture());
        assertThat(linea.getValue().getEstadoAnterior()).isEqualTo(EstadoReparacion.RECIBIDO);
        assertThat(linea.getValue().getEstadoNuevo()).isEqualTo(EstadoReparacion.DIAGNOSTICO);
        assertThat(linea.getValue().getComentario()).isNull();
        assertThat(linea.getValue().getFecha()).isEqualTo(AHORA);
        verify(usuarios).getReferenceById(3L);
        assertThat(reparacion.getEstado()).isEqualTo(EstadoReparacion.DIAGNOSTICO);
    }

    @Test
    void siElClienteAceptaPasaAReparacion() {
        Reparacion reparacion = reparacionEsperandoAprobacion();

        service.responderPresupuesto(1L, new RespuestaPresupuestoRequest(true), 3L);

        assertThat(reparacion.getEstado()).isEqualTo(EstadoReparacion.EN_REPARACION);
        assertThat(reparacion.getPresupuestoAceptado()).isTrue();
    }

    @Test
    void siElClienteRechazaQuedaListoSinReparar() {
        Reparacion reparacion = reparacionEsperandoAprobacion();

        service.responderPresupuesto(1L, new RespuestaPresupuestoRequest(false), 3L);

        assertThat(reparacion.getEstado()).isEqualTo(EstadoReparacion.LISTO);
        assertThat(reparacion.getFechaListo()).isEqualTo(AHORA);
    }

    @Test
    void alEntregarSeGuardaElPrecioFinal() {
        Reparacion reparacion = reparacionEsperandoAprobacion();
        service.responderPresupuesto(1L, new RespuestaPresupuestoRequest(false), 3L);

        service.cambiarEstado(1L, new CambioEstadoRequest(EstadoReparacion.ENTREGADO, null, new BigDecimal("15.00")), 3L);

        assertThat(reparacion.getEstado()).isEqualTo(EstadoReparacion.ENTREGADO);
        assertThat(reparacion.getPrecioFinal()).isEqualByComparingTo("15.00");
    }

    @Test
    void elDiagnosticoNoSeCambiaDespuesDeEnviarElPresupuesto() {
        reparacionEsperandoAprobacion();

        assertThatThrownBy(() -> service.registrarDiagnostico(1L, new DiagnosticoRequest("Otra cosa", BigDecimal.ONE)))
                .isInstanceOf(ReglaDeNegocioException.class);
    }

    @Test
    void unaReparacionQueNoExisteDa404() {
        when(reparaciones.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtener(99L)).isInstanceOf(RecursoNoEncontradoException.class);
    }

    private Reparacion reparacionEn(EstadoReparacion estado) {
        Equipo equipo = new Equipo(new Cliente("Ana López", "600111222", null), TipoEquipo.PORTATIL, "HP", null, null);
        Reparacion reparacion = new Reparacion("FX-2026-00001", equipo, "No enciende", null);
        when(reparaciones.findById(1L)).thenReturn(Optional.of(reparacion));
        return reparacion;
    }

    private Reparacion reparacionEsperandoAprobacion() {
        Reparacion reparacion = reparacionEn(EstadoReparacion.RECIBIDO);
        reparacion.cambiarEstado(EstadoReparacion.DIAGNOSTICO, AHORA);
        reparacion.registrarDiagnostico("Batería agotada");
        reparacion.fijarPresupuesto(new BigDecimal("99.00"));
        reparacion.cambiarEstado(EstadoReparacion.ESPERANDO_APROBACION, AHORA);
        return reparacion;
    }
}

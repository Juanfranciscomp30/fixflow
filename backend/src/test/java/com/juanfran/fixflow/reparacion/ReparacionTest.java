package com.juanfran.fixflow.reparacion;

import com.juanfran.fixflow.common.ReglaDeNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static com.juanfran.fixflow.reparacion.EstadoReparacion.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReparacionTest {

    private static final Instant AHORA = Instant.parse("2026-09-24T10:00:00Z");

    private Reparacion reparacion;

    @BeforeEach
    void setUp() {
        reparacion = new Reparacion("FX-2026-00001", null, "No enciende", null);
    }

    @Test
    void empiezaEnRecibido() {
        assertThat(reparacion.getEstado()).isEqualTo(RECIBIDO);
    }

    @Test
    void noPideAprobacionSinPresupuesto() {
        reparacion.cambiarEstado(DIAGNOSTICO, AHORA);

        assertThatThrownBy(() -> reparacion.cambiarEstado(ESPERANDO_APROBACION, AHORA))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessageContaining("presupuesto");
    }

    @Test
    void noSeReparaSinQueElClienteAcepte() {
        llevarAEsperandoAprobacion();

        assertThatThrownBy(() -> reparacion.cambiarEstado(EN_REPARACION, AHORA))
                .isInstanceOf(ReglaDeNegocioException.class);
    }

    @Test
    void flujoCompletoConPresupuestoAceptadoGuardaLasFechas() {
        llevarAEsperandoAprobacion();
        reparacion.responderPresupuesto(true);

        reparacion.cambiarEstado(EN_REPARACION, AHORA);
        reparacion.cambiarEstado(LISTO, AHORA.plusSeconds(3600));
        reparacion.cambiarEstado(ENTREGADO, AHORA.plusSeconds(7200));

        assertThat(reparacion.getEstado()).isEqualTo(ENTREGADO);
        assertThat(reparacion.getFechaListo()).isEqualTo(AHORA.plusSeconds(3600));
        assertThat(reparacion.getFechaEntrega()).isEqualTo(AHORA.plusSeconds(7200));
    }

    @Test
    void presupuestoRechazadoSeDevuelveSinReparar() {
        llevarAEsperandoAprobacion();
        reparacion.responderPresupuesto(false);

        reparacion.cambiarEstado(LISTO, AHORA);

        assertThat(reparacion.getEstado()).isEqualTo(LISTO);
    }

    @Test
    void noSeDevuelveSinRepararSiElClienteNoHaContestado() {
        llevarAEsperandoAprobacion();

        assertThatThrownBy(() -> reparacion.cambiarEstado(LISTO, AHORA))
                .isInstanceOf(ReglaDeNegocioException.class);
    }

    @Test
    void soloSeRespondeAlPresupuestoCuandoSeEstaEsperandoAprobacion() {
        assertThatThrownBy(() -> reparacion.responderPresupuesto(true))
                .isInstanceOf(ReglaDeNegocioException.class);
    }

    @Test
    void rechazaSaltosDeEstado() {
        assertThatThrownBy(() -> reparacion.cambiarEstado(ENTREGADO, AHORA))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessageContaining("RECIBIDO")
                .hasMessageContaining("ENTREGADO");
    }

    private void llevarAEsperandoAprobacion() {
        reparacion.cambiarEstado(DIAGNOSTICO, AHORA);
        reparacion.fijarPresupuesto(new BigDecimal("89.00"));
        reparacion.cambiarEstado(ESPERANDO_APROBACION, AHORA);
    }
}

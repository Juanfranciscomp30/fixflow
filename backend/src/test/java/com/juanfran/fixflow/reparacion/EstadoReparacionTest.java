package com.juanfran.fixflow.reparacion;

import org.junit.jupiter.api.Test;

import static com.juanfran.fixflow.reparacion.EstadoReparacion.*;
import static org.junit.jupiter.api.Assertions.*;

class EstadoReparacionTest {

    @Test
    void flujoNormalCompletoEsValido() {
        assertTrue(RECIBIDO.puedePasarA(DIAGNOSTICO));
        assertTrue(DIAGNOSTICO.puedePasarA(ESPERANDO_APROBACION));
        assertTrue(ESPERANDO_APROBACION.puedePasarA(EN_REPARACION));
        assertTrue(EN_REPARACION.puedePasarA(LISTO));
        assertTrue(LISTO.puedePasarA(ENTREGADO));
    }

    @Test
    void presupuestoRechazadoPasaDirectamenteAListo() {
        assertTrue(ESPERANDO_APROBACION.puedePasarA(LISTO));
    }

    @Test
    void noSePuedeRepararSinAprobacion() {
        assertFalse(DIAGNOSTICO.puedePasarA(EN_REPARACION));
        assertFalse(RECIBIDO.puedePasarA(EN_REPARACION));
    }

    @Test
    void noSePuedeRetrocederNiSaltarPasos() {
        assertFalse(LISTO.puedePasarA(EN_REPARACION));
        assertFalse(RECIBIDO.puedePasarA(ENTREGADO));
    }

    @Test
    void entregadoEsEstadoFinal() {
        assertTrue(ENTREGADO.siguientesPermitidos().isEmpty());
    }
}

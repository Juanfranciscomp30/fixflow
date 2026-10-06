package com.juanfran.fixflow.metricas;

import com.juanfran.fixflow.reparacion.EstadoReparacion;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MetricasServiceTest {

    private final MetricasRepository repo = mock(MetricasRepository.class);
    private final MetricasService service = new MetricasService(repo);

    @Test
    void rellenaLosEstadosSinReparacionesYCalculaLosTotales() {
        when(repo.totalesPorEstado()).thenReturn(List.of(
                total("RECIBIDO", 2), total("LISTO", 1), total("ENTREGADO", 10)));
        when(repo.ingresosPorMes()).thenReturn(List.of(
                ingreso("2026-09", "320.00"), ingreso("2026-10", "145.50")));
        when(repo.cargaPorTecnico()).thenReturn(List.of());
        when(repo.tiempoMedioReparacionHoras()).thenReturn(52.5);
        when(repo.ingresosUltimos30Dias()).thenReturn(new BigDecimal("145.50"));

        MetricasDto m = service.calcular();

        assertThat(m.porEstado()).hasSize(EstadoReparacion.values().length);
        assertThat(m.porEstado()).contains(new MetricasDto.EstadoTotal(EstadoReparacion.DIAGNOSTICO, 0));
        assertThat(m.abiertas()).isEqualTo(3);   // 2 recibidas + 1 lista, sin contar entregadas
        assertThat(m.listasParaRecoger()).isEqualTo(1);
        assertThat(m.ingresosUltimos30Dias()).isEqualByComparingTo("145.50");
        assertThat(m.ingresosPorMes()).extracting(MetricasDto.IngresoMes::mes).containsExactly("2026-09", "2026-10");
        assertThat(m.tiempoMedioHoras()).isEqualTo(52.5);
    }

    private static MetricasRepository.TotalPorEstado total(String estado, long total) {
        return new MetricasRepository.TotalPorEstado() {
            public String getEstado() { return estado; }
            public Long getTotal() { return total; }
        };
    }

    private static MetricasRepository.IngresoMensual ingreso(String mes, String total) {
        return new MetricasRepository.IngresoMensual() {
            public String getMes() { return mes; }
            public BigDecimal getTotal() { return new BigDecimal(total); }
        };
    }
}

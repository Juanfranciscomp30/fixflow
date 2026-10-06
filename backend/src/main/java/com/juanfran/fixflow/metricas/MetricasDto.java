package com.juanfran.fixflow.metricas;

import com.juanfran.fixflow.reparacion.EstadoReparacion;

import java.math.BigDecimal;
import java.util.List;

/** Todo lo que pinta el dashboard en una sola respuesta. */
public record MetricasDto(
        double tiempoMedioHoras,
        long abiertas,
        long listasParaRecoger,
        BigDecimal ingresosUltimos30Dias,
        List<EstadoTotal> porEstado,
        List<IngresoMes> ingresosPorMes,
        List<CargaTecnico> cargaPorTecnico
) {

    public record EstadoTotal(EstadoReparacion estado, long total) {
    }

    public record IngresoMes(String mes, BigDecimal total) {
    }

    public record CargaTecnico(String tecnico, long abiertas, long entregadas) {
    }
}

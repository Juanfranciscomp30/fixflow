package com.juanfran.fixflow.metricas;

import com.juanfran.fixflow.metricas.MetricasDto.CargaTecnico;
import com.juanfran.fixflow.metricas.MetricasDto.EstadoTotal;
import com.juanfran.fixflow.metricas.MetricasDto.IngresoMes;
import com.juanfran.fixflow.reparacion.EstadoReparacion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class MetricasService {

    private final MetricasRepository repo;

    public MetricasService(MetricasRepository repo) {
        this.repo = repo;
    }

    public MetricasDto calcular() {
        // La consulta solo devuelve los estados que tienen reparaciones; rellenamos el resto con 0
        Map<String, Long> totales = repo.totalesPorEstado().stream()
                .collect(Collectors.toMap(MetricasRepository.TotalPorEstado::getEstado,
                        MetricasRepository.TotalPorEstado::getTotal));
        List<EstadoTotal> porEstado = Arrays.stream(EstadoReparacion.values())
                .map(e -> new EstadoTotal(e, totales.getOrDefault(e.name(), 0L)))
                .toList();

        long abiertas = porEstado.stream()
                .filter(t -> t.estado() != EstadoReparacion.ENTREGADO)
                .mapToLong(EstadoTotal::total)
                .sum();
        long listas = totales.getOrDefault(EstadoReparacion.LISTO.name(), 0L);

        List<IngresoMes> ingresos = repo.ingresosPorMes().stream()
                .map(i -> new IngresoMes(i.getMes(), i.getTotal()))
                .toList();

        List<CargaTecnico> carga = repo.cargaPorTecnico().stream()
                .map(c -> new CargaTecnico(c.getTecnico(), c.getAbiertas(), c.getEntregadas()))
                .toList();

        return new MetricasDto(repo.tiempoMedioReparacionHoras(), abiertas, listas,
                repo.ingresosUltimos30Dias(), porEstado, ingresos, carga);
    }
}

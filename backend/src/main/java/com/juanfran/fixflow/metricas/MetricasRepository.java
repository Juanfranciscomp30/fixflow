package com.juanfran.fixflow.metricas;

import com.juanfran.fixflow.reparacion.Reparacion;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Consultas de agregación para el dashboard. Van en SQL nativo porque son GROUP BY,
 * funciones de fecha y FILTER de Postgres: más claras así que en JPQL.
 * Cada consulta devuelve una "projection" (interfaz): Spring rellena los getters
 * con las columnas que tienen el mismo alias.
 */
public interface MetricasRepository extends Repository<Reparacion, Long> {

    interface TotalPorEstado {
        String getEstado();
        Long getTotal();
    }

    interface IngresoMensual {
        String getMes();
        BigDecimal getTotal();
    }

    interface CargaTecnico {
        String getTecnico();
        Long getAbiertas();
        Long getEntregadas();
    }

    /** Horas de media desde que entra el equipo hasta que está listo. */
    @Query(value = """
            SELECT COALESCE(AVG(EXTRACT(EPOCH FROM (fecha_listo - fecha_entrada)) / 3600), 0)::float8
            FROM reparacion
            WHERE fecha_listo IS NOT NULL
            """, nativeQuery = true)
    double tiempoMedioReparacionHoras();

    /** Ventana móvil en vez de mes natural: el día 1 de cada mes no aparece todo a cero. */
    @Query(value = """
            SELECT COALESCE(SUM(precio_final), 0)::numeric
            FROM reparacion
            WHERE estado = 'ENTREGADO' AND fecha_entrega >= now() - INTERVAL '30 days'
            """, nativeQuery = true)
    BigDecimal ingresosUltimos30Dias();

    @Query(value = """
            SELECT estado AS estado, COUNT(*)::bigint AS total
            FROM reparacion
            GROUP BY estado
            """, nativeQuery = true)
    List<TotalPorEstado> totalesPorEstado();

    /** Lo cobrado en los últimos 12 meses (incluido el actual), agrupado por mes de entrega. */
    @Query(value = """
            SELECT to_char(date_trunc('month', fecha_entrega), 'YYYY-MM') AS mes,
                   SUM(precio_final)::numeric AS total
            FROM reparacion
            WHERE estado = 'ENTREGADO'
              AND precio_final IS NOT NULL
              AND fecha_entrega >= date_trunc('month', now()) - INTERVAL '11 months'
            GROUP BY 1
            ORDER BY 1
            """, nativeQuery = true)
    List<IngresoMensual> ingresosPorMes();

    /** LEFT JOIN para que también salga el técnico que no tiene nada asignado. */
    @Query(value = """
            SELECT u.nombre AS tecnico,
                   (COUNT(r.id) FILTER (WHERE r.estado <> 'ENTREGADO'))::bigint AS abiertas,
                   (COUNT(r.id) FILTER (WHERE r.estado = 'ENTREGADO'))::bigint AS entregadas
            FROM usuario u
            LEFT JOIN reparacion r ON r.tecnico_id = u.id
            WHERE u.rol = 'TECNICO' AND u.activo
            GROUP BY u.id, u.nombre
            ORDER BY u.nombre
            """, nativeQuery = true)
    List<CargaTecnico> cargaPorTecnico();
}

package com.juanfran.fixflow.reparacion;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReparacionRepository extends JpaRepository<Reparacion, Long> {

    Optional<Reparacion> findByCodigo(String codigo);

    List<Reparacion> findByTecnicoId(Long tecnicoId);

    List<Reparacion> findByEstado(EstadoReparacion estado);

    /** Siguiente número de la secuencia de Postgres para montar el código FX-AAAA-NNNNN. */
    @Query(value = "SELECT nextval('reparacion_codigo_seq')", nativeQuery = true)
    long siguienteNumeroCodigo();

    // El EntityGraph trae equipo, cliente y técnico en la misma consulta (evita el problema N+1)
    @EntityGraph(attributePaths = {"equipo", "equipo.cliente", "tecnico"})
    List<Reparacion> findAllByOrderByFechaEntradaDesc();

    @EntityGraph(attributePaths = {"equipo", "equipo.cliente", "tecnico"})
    List<Reparacion> findByEstadoOrderByFechaEntradaDesc(EstadoReparacion estado);
}

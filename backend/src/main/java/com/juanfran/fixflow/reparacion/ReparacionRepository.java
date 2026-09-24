package com.juanfran.fixflow.reparacion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReparacionRepository extends JpaRepository<Reparacion, Long> {

    Optional<Reparacion> findByCodigo(String codigo);

    List<Reparacion> findByTecnicoId(Long tecnicoId);

    List<Reparacion> findByEstado(EstadoReparacion estado);
}

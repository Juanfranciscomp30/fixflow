package com.juanfran.fixflow.equipo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EquipoRepository extends JpaRepository<Equipo, Long> {

    List<Equipo> findByClienteId(Long clienteId);
}

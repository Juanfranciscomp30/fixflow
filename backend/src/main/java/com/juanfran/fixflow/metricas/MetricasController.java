package com.juanfran.fixflow.metricas;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/metricas")
@PreAuthorize("hasRole('ADMIN')")   // Los números del negocio solo los ve el administrador
public class MetricasController {

    private final MetricasService service;

    public MetricasController(MetricasService service) {
        this.service = service;
    }

    @GetMapping
    public MetricasDto metricas() {
        return service.calcular();
    }
}

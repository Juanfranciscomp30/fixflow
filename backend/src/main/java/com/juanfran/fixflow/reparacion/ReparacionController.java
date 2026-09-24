package com.juanfran.fixflow.reparacion;

import com.juanfran.fixflow.reparacion.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/reparaciones")
public class ReparacionController {

    private final ReparacionService service;

    public ReparacionController(ReparacionService service) {
        this.service = service;
    }

    @GetMapping
    public List<ReparacionResumen> listar(@RequestParam(required = false) EstadoReparacion estado) {
        return service.listar(estado);
    }

    @GetMapping("/{id}")
    public ReparacionDetalle obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    public ResponseEntity<ReparacionDetalle> recibir(@Valid @RequestBody NuevaReparacionRequest req,
                                                     @AuthenticationPrincipal Jwt jwt) {
        ReparacionDetalle creada = service.recibir(req, usuarioId(jwt));
        return ResponseEntity.created(URI.create("/api/reparaciones/" + creada.id())).body(creada);
    }

    @PutMapping("/{id}/diagnostico")
    public ReparacionDetalle registrarDiagnostico(@PathVariable Long id, @Valid @RequestBody DiagnosticoRequest req) {
        return service.registrarDiagnostico(id, req);
    }

    @PatchMapping("/{id}/estado")
    public ReparacionDetalle cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambioEstadoRequest req,
                                           @AuthenticationPrincipal Jwt jwt) {
        return service.cambiarEstado(id, req, usuarioId(jwt));
    }

    @PostMapping("/{id}/respuesta-presupuesto")
    public ReparacionDetalle responderPresupuesto(@PathVariable Long id,
                                                  @Valid @RequestBody RespuestaPresupuestoRequest req,
                                                  @AuthenticationPrincipal Jwt jwt) {
        return service.responderPresupuesto(id, req, usuarioId(jwt));
    }

    // El subject del token es el id del usuario (ver JwtService)
    private static Long usuarioId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}

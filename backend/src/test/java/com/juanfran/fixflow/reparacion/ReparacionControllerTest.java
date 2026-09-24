package com.juanfran.fixflow.reparacion;

import com.juanfran.fixflow.common.RecursoNoEncontradoException;
import com.juanfran.fixflow.common.ReglaDeNegocioException;
import com.juanfran.fixflow.common.config.CorsConfig;
import com.juanfran.fixflow.common.config.SecurityConfig;
import com.juanfran.fixflow.reparacion.dto.ReparacionDetalle;
import com.juanfran.fixflow.reparacion.dto.ReparacionResumen;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReparacionController.class)
@Import({SecurityConfig.class, CorsConfig.class})
class ReparacionControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ReparacionService service;

    // Lo necesita la configuración de seguridad; en los tests el token se simula con jwt()
    @MockitoBean
    private JwtDecoder jwtDecoder;

    private static final String NUEVA_REPARACION = """
            {
              "cliente": { "nombre": "Ana López", "telefono": "600111222" },
              "equipo":  { "tipo": "PORTATIL", "marca": "HP", "modelo": "Pavilion 15" },
              "averiaDescrita": "No enciende"
            }
            """;

    /** Técnico con id 2, como Antonio Ruiz en los datos de demo. */
    private static RequestPostProcessor tecnico() {
        return jwt().jwt(j -> j.subject("2").claim("rol", "TECNICO"));
    }

    @Test
    void sinTokenDevuelve401() throws Exception {
        mvc.perform(get("/api/reparaciones")).andExpect(status().isUnauthorized());
    }

    @Test
    void listaFiltrandoPorEstado() throws Exception {
        when(service.listar(eq(EstadoReparacion.LISTO), any())).thenReturn(List.of(new ReparacionResumen(
                14L, "FX-2026-00014", EstadoReparacion.LISTO, "José Luis Fernández", null, "Samsung Galaxy S21",
                "No le funciona el altavoz", "Marta Sánchez", Instant.parse("2026-03-15T10:00:00Z"))));

        mvc.perform(get("/api/reparaciones").param("estado", "LISTO").with(tecnico()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("FX-2026-00014"))
                .andExpect(jsonPath("$[0].estado").value("LISTO"));
    }

    @Test
    void recibirDevuelve201YUsaElUsuarioDelToken() throws Exception {
        when(service.recibir(any(), eq(2L))).thenReturn(detalle(22L));

        mvc.perform(post("/api/reparaciones").with(tecnico())
                        .contentType(MediaType.APPLICATION_JSON).content(NUEVA_REPARACION))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/reparaciones/22"))
                .andExpect(jsonPath("$.codigo").value("FX-2026-00022"));

        verify(service).recibir(any(), eq(2L));
    }

    @Test
    void datosIncompletosDevuelven400() throws Exception {
        String sinAveria = """
                { "cliente": { "nombre": "", "telefono": "600111222" },
                  "equipo":  { "tipo": "PORTATIL", "marca": "HP" } }
                """;

        mvc.perform(post("/api/reparaciones").with(tecnico())
                        .contentType(MediaType.APPLICATION_JSON).content(sinAveria))
                .andExpect(status().isBadRequest());
    }

    @Test
    void reparacionInexistenteDevuelve404() throws Exception {
        when(service.obtener(99L)).thenThrow(new RecursoNoEncontradoException("No existe la reparación 99"));

        mvc.perform(get("/api/reparaciones/99").with(tecnico()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No existe la reparación 99"));
    }

    @Test
    void transicionNoPermitidaDevuelve409() throws Exception {
        when(service.cambiarEstado(eq(1L), any(), eq(2L)))
                .thenThrow(new ReglaDeNegocioException("No se puede pasar de RECIBIDO a LISTO"));

        mvc.perform(patch("/api/reparaciones/1/estado").with(tecnico())
                        .contentType(MediaType.APPLICATION_JSON).content("{ \"estado\": \"LISTO\" }"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("No se puede pasar de RECIBIDO a LISTO"));
    }

    private static ReparacionDetalle detalle(Long id) {
        return new ReparacionDetalle(id, "FX-2026-000" + id, EstadoReparacion.RECIBIDO,
                Set.of(EstadoReparacion.DIAGNOSTICO), null, null, "No enciende", null, null, null, null,
                null, Instant.parse("2026-03-15T10:00:00Z"), null, null, List.of());
    }
}

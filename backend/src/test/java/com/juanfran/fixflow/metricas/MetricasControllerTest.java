package com.juanfran.fixflow.metricas;

import com.juanfran.fixflow.common.config.CorsConfig;
import com.juanfran.fixflow.common.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MetricasController.class)
@Import({SecurityConfig.class, CorsConfig.class})
class MetricasControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private MetricasService service;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void elAdminVeLasMetricas() throws Exception {
        when(service.calcular()).thenReturn(new MetricasDto(48.0, 8, 1, new BigDecimal("145.00"),
                List.of(), List.of(), List.of()));

        mvc.perform(get("/api/metricas")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.abiertas").value(8));
    }

    @Test
    void unTecnicoNoVeLasMetricas() throws Exception {
        mvc.perform(get("/api/metricas")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_TECNICO"))))
                .andExpect(status().isForbidden());
    }
}

package com.juanfran.fixflow.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Configuración del token: app.jwt.secret y app.jwt.expiracion en application.yml.
 * El secreto se valida al arrancar para no desplegar nunca con una clave débil.
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, Duration expiracion) {

    public JwtProperties {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("app.jwt.secret debe tener al menos 32 caracteres (256 bits) para HS256");
        }
        if (expiracion == null) {
            expiracion = Duration.ofHours(8);
        }
    }
}

package com.juanfran.fixflow.auth;

import com.juanfran.fixflow.usuario.Rol;
import org.springframework.security.oauth2.jwt.Jwt;

public record UsuarioActual(Long id, String nombre, Rol rol) {

    /** Saca del token quién está haciendo la petición. */
    public static UsuarioActual desde(Jwt jwt) {
        return new UsuarioActual(
            Long.valueOf(jwt.getSubject()),
            jwt.getClaimAsString("nombre"),
            Rol.valueOf(jwt.getClaimAsString("rol")));
    }

    public boolean esAdmin() {
        return rol == Rol.ADMIN;
    }
}

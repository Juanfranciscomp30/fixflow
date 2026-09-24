package com.juanfran.fixflow.auth;

import com.juanfran.fixflow.usuario.Rol;

public record LoginResponse(
        String token,
        String tipo,
        long expiraEnSegundos,
        String nombre,
        Rol rol
) {
}

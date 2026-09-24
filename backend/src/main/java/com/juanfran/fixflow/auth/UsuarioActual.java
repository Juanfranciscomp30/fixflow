package com.juanfran.fixflow.auth;

import com.juanfran.fixflow.usuario.Rol;

public record UsuarioActual(Long id, String nombre, Rol rol) {
}

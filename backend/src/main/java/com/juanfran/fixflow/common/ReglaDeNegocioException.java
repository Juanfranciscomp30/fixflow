package com.juanfran.fixflow.common;

/** Se lanza cuando una operación incumple una regla del taller. Se traduce a HTTP 409. */
public class ReglaDeNegocioException extends RuntimeException {

    public ReglaDeNegocioException(String mensaje) {
        super(mensaje);
    }
}

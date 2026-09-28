package com.rutaia.backend.exception;

/**
 * Se lanza cuando la peticion choca con datos existentes
 * (correo duplicado, recomendacion ya calificada...).
 * El manejador global la convierte en HTTP 409 Conflict.
 */
public class ConflictoException extends RuntimeException {

    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}

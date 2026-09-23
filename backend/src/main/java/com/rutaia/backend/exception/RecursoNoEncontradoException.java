package com.rutaia.backend.exception;

/**
 * Se lanza cuando se busca algo que no existe (un estudiante, un curso...).
 * El manejador global la convierte en HTTP 404 Not Found.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}

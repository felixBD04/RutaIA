package com.rutaia.backend.exception;

/**
 * Se lanza cuando n8n responde, pero su respuesta no cumple las reglas de negocio:
 * texto vacio, fuentes que no existen en PostgreSQL, cursos inactivos, etc.
 * La consulta termina en estado ERROR.
 */
public class RespuestaInvalidaException extends RuntimeException {

    public RespuestaInvalidaException(String mensaje) {
        super(mensaje);
    }
}

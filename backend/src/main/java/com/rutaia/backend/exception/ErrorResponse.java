package com.rutaia.backend.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Formato unico de error para toda la API.
 * Asi el frontend siempre sabe que esperar cuando algo falla.
 */
@JsonInclude(JsonInclude.Include.NON_NULL) // no muestra "errores" si viene vacio
public record ErrorResponse(
        LocalDateTime fecha,
        int estado,
        String error,
        String mensaje,
        String ruta,
        Map<String, String> errores
) {
}

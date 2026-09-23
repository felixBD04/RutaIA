package com.rutaia.backend.dto.calificacion;

import com.rutaia.backend.model.Calificacion;

import java.time.LocalDateTime;

/**
 * Datos que la API devuelve sobre una calificacion.
 */
public record CalificacionResponse(
        Long id,
        Long recomendacionId,
        Integer puntuacion,
        String comentario,
        LocalDateTime fecha
) {

    public static CalificacionResponse desde(Calificacion calificacion) {
        return new CalificacionResponse(
                calificacion.getId(),
                calificacion.getRecomendacion().getId(),
                calificacion.getPuntuacion().intValue(),
                calificacion.getComentario(),
                calificacion.getFecha()
        );
    }
}

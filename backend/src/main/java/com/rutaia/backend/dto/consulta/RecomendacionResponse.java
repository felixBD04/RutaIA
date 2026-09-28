package com.rutaia.backend.dto.consulta;

import com.rutaia.backend.dto.calificacion.CalificacionResponse;
import com.rutaia.backend.model.Recomendacion;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Recomendacion generada para una consulta, con sus fuentes y su calificacion.
 * "calificacion" es null mientras el estudiante no la haya calificado:
 * asi el frontend sabe si debe mostrar el formulario de calificacion.
 */
public record RecomendacionResponse(
        Long id,
        String respuesta,
        LocalDateTime fechaGeneracion,
        List<FuenteResponse> fuentes,
        CalificacionResponse calificacion
) {

    public static RecomendacionResponse desde(Recomendacion recomendacion) {
        List<FuenteResponse> fuentes = recomendacion.getFuentes().stream()
                .map(f -> FuenteResponse.desde(f, recomendacion.getRespuesta()))
                .toList();
        return new RecomendacionResponse(
                recomendacion.getId(),
                recomendacion.getRespuesta(),
                recomendacion.getFechaGeneracion(),
                fuentes,
                recomendacion.getCalificacion() == null ? null : CalificacionResponse.desde(recomendacion.getCalificacion())
        );
    }
}

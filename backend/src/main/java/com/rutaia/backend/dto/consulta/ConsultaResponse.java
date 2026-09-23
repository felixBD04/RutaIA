package com.rutaia.backend.dto.consulta;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rutaia.backend.model.Consulta;
import com.rutaia.backend.model.enums.EstadoConsulta;

import java.time.LocalDateTime;

/**
 * Respuesta completa de una consulta (RF 13 y RF 16):
 * pregunta, estado, fecha, recomendacion y fuentes con su similitud.
 */
public record ConsultaResponse(
        Long id,
        Long estudianteId,
        String estudianteNombre,
        String pregunta,
        EstadoConsulta estado,
        LocalDateTime fechaConsulta,
        RecomendacionResponse recomendacion,

        // Solo aparece cuando hay algo que explicarle al usuario (por ejemplo, un ERROR)
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String mensaje
) {

    public static ConsultaResponse desde(Consulta consulta, String mensaje) {
        return new ConsultaResponse(
                consulta.getId(),
                consulta.getEstudiante().getId(),
                consulta.getEstudiante().getNombreCompleto(),
                consulta.getPregunta(),
                consulta.getEstado(),
                consulta.getFechaConsulta(),
                consulta.getRecomendacion() == null ? null : RecomendacionResponse.desde(consulta.getRecomendacion()),
                mensaje
        );
    }
}

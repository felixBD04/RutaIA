package com.rutaia.backend.dto.estudiante;

import com.rutaia.backend.model.Estudiante;
import com.rutaia.backend.model.enums.NivelExperiencia;

import java.time.LocalDateTime;

/**
 * Datos que la API devuelve sobre un estudiante.
 */
public record EstudianteResponse(
        Long id,
        String nombreCompleto,
        String correo,
        NivelExperiencia nivelExperiencia,
        String areaInteres,
        LocalDateTime fechaRegistro
) {

    /** Convierte la entidad (base de datos) en el DTO (API). */
    public static EstudianteResponse desde(Estudiante estudiante) {
        return new EstudianteResponse(
                estudiante.getId(),
                estudiante.getNombreCompleto(),
                estudiante.getCorreo(),
                estudiante.getNivelExperiencia(),
                estudiante.getAreaInteres(),
                estudiante.getFechaRegistro()
        );
    }
}

package com.rutaia.backend.dto.curso;

import com.rutaia.backend.model.enums.NivelCurso;
import jakarta.validation.constraints.*;

/**
 * Datos para registrar o actualizar un curso (RF 03).
 * Reglas de negocio: nombre y descripcion obligatorios, duracion mayor que cero.
 */
public record CursoRequest(

        @NotBlank(message = "El nombre del curso es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
        String nombre,

        @NotBlank(message = "La descripción del curso es obligatoria")
        @Size(min = 30, message = "La descripción debe tener al menos 30 caracteres para permitir búsquedas semánticas útiles")
        String descripcion,

        @NotBlank(message = "La categoría es obligatoria")
        @Size(max = 80, message = "La categoría no puede superar 80 caracteres")
        String categoria,

        @NotNull(message = "El nivel es obligatorio (Básico, Intermedio o Avanzado)")
        NivelCurso nivel,

        @NotNull(message = "La duración es obligatoria")
        @Positive(message = "La duración debe ser mayor que cero")
        Integer duracionHoras
) {
}

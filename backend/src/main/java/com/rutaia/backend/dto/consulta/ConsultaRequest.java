package com.rutaia.backend.dto.consulta;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Datos para registrar una consulta (RF 06).
 */
public record ConsultaRequest(

        @NotNull(message = "El id del estudiante es obligatorio")
        @Positive(message = "El id del estudiante debe ser un número positivo")
        Long estudianteId,

        @NotBlank(message = "La pregunta no puede estar vacía")
        @Size(max = 1000, message = "La pregunta no puede superar 1000 caracteres")
        String pregunta
) {
}

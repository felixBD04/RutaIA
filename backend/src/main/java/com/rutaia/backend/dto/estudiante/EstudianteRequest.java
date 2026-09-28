package com.rutaia.backend.dto.estudiante;

import com.rutaia.backend.model.enums.NivelExperiencia;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Datos que el cliente envia para registrar un estudiante (RF 01).
 * No incluye id ni fecha: esos los decide el servidor.
 */
public record EstudianteRequest(

        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
        String nombreCompleto,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 150, message = "El correo no puede superar 150 caracteres")
        String correo,

        @NotNull(message = "El nivel de experiencia es obligatorio (Principiante, Intermedio o Avanzado)")
        NivelExperiencia nivelExperiencia,

        @NotBlank(message = "El área de interés es obligatoria")
        @Size(max = 100, message = "El área de interés no puede superar 100 caracteres")
        String areaInteres
) {
}

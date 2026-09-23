package com.rutaia.backend.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Niveles de experiencia permitidos para un estudiante (RF 01).
 * Se guardan en la base como texto: PRINCIPIANTE, INTERMEDIO, AVANZADO.
 */
public enum NivelExperiencia {
    PRINCIPIANTE,
    INTERMEDIO,
    AVANZADO;

    /**
     * Permite recibir el valor en el JSON sin importar mayusculas o minusculas:
     * "Principiante", "principiante" y "PRINCIPIANTE" funcionan igual.
     */
    @JsonCreator
    public static NivelExperiencia desdeTexto(String valor) {
        if (valor == null) {
            return null;
        }
        for (NivelExperiencia nivel : values()) {
            if (nivel.name().equalsIgnoreCase(valor.trim())) {
                return nivel;
            }
        }
        throw new IllegalArgumentException(
                "Nivel de experiencia inválido: '" + valor
                        + "'. Valores permitidos: Principiante, Intermedio, Avanzado");
    }
}

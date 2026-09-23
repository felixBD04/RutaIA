package com.rutaia.backend.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.text.Normalizer;

/**
 * Niveles permitidos para un curso (RF 03).
 * Se guardan en la base como texto: BASICO, INTERMEDIO, AVANZADO.
 */
public enum NivelCurso {
    BASICO,
    INTERMEDIO,
    AVANZADO;

    /**
     * Acepta el valor sin importar mayusculas ni tildes:
     * "Básico", "basico" y "BASICO" funcionan igual.
     */
    @JsonCreator
    public static NivelCurso desdeTexto(String valor) {
        if (valor == null) {
            return null;
        }
        String normalizado = Normalizer.normalize(valor.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", ""); // elimina las tildes
        for (NivelCurso nivel : values()) {
            if (nivel.name().equalsIgnoreCase(normalizado)) {
                return nivel;
            }
        }
        throw new IllegalArgumentException(
                "Nivel de curso inválido: '" + valor
                        + "'. Valores permitidos: Básico, Intermedio, Avanzado");
    }
}

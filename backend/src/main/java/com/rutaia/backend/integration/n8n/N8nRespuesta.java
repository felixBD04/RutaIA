package com.rutaia.backend.integration.n8n;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Respuesta que devuelve el flujo RAG de n8n.
 * ignoreUnknown: si n8n agrega campos nuevos (como "busqueda"), no se rompe nada.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record N8nRespuesta(
        Long consultaId,
        String pregunta,
        String estado,
        String respuesta,
        List<Fuente> fuentes
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Fuente(
            Integer posicion,
            Long cursoId,
            String nombre,
            Double similitud
    ) {
    }
}

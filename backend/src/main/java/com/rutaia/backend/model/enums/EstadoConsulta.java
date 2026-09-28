package com.rutaia.backend.model.enums;

/**
 * Estados de una consulta (RF 07).
 * PENDIENTE: guardada, esperando la respuesta de n8n.
 * RESPONDIDA: se genero una recomendacion con cursos.
 * SIN_RESULTADOS: ningun curso supero el umbral de relevancia.
 * ERROR: fallo n8n, Qdrant, OpenRouter o la respuesta no fue valida.
 */
public enum EstadoConsulta {
    PENDIENTE,
    RESPONDIDA,
    SIN_RESULTADOS,
    ERROR
}

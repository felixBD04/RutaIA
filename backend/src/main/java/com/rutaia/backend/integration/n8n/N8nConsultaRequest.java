package com.rutaia.backend.integration.n8n;

/**
 * Cuerpo que Spring Boot envia al webhook de n8n (RF 08).
 */
public record N8nConsultaRequest(
        Long consultaId,
        String pregunta,
        String nivelExperiencia,
        String areaInteres
) {
}

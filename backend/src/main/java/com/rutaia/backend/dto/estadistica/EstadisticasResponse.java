package com.rutaia.backend.dto.estadistica;

import java.util.List;
import java.util.Map;

/**
 * Estadisticas basicas del sistema (RF 18).
 */
public record EstadisticasResponse(
        long totalEstudiantes,
        long totalCursosActivos,
        long totalConsultas,
        Map<String, Long> consultasPorEstado,
        CursoRecomendado cursoMasRecomendado,
        List<CursoRecomendado> topCursosRecomendados,
        long totalCalificaciones,
        Double promedioCalificacion
) {

    public record CursoRecomendado(Long cursoId, String nombre, Long vecesRecomendado) {
    }
}

package com.rutaia.backend.controller;

import com.rutaia.backend.dto.estadistica.EstadisticasResponse;
import com.rutaia.backend.service.EstadisticaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Estadisticas basicas del sistema (RF 18).
 */
@RestController
@RequestMapping("/api/estadisticas")
@RequiredArgsConstructor
@Tag(name = "Estadísticas", description = "Indicadores de uso del sistema")
public class EstadisticaController {

    private final EstadisticaService estadisticaService;

    @GetMapping
    @Operation(summary = "Obtener estadísticas",
            description = "Total de consultas, consultas por estado, cursos más recomendados y promedio de calificaciones")
    public EstadisticasResponse obtener() {
        return estadisticaService.obtener();
    }
}

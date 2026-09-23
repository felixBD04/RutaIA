package com.rutaia.backend.controller;

import com.rutaia.backend.dto.calificacion.CalificacionRequest;
import com.rutaia.backend.dto.calificacion.CalificacionResponse;
import com.rutaia.backend.service.CalificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Calificacion de recomendaciones (RF 17).
 */
@RestController
@RequestMapping("/api/recomendaciones")
@RequiredArgsConstructor
@Tag(name = "Calificaciones", description = "Valoración de las recomendaciones recibidas")
public class CalificacionController {

    private final CalificacionService calificacionService;

    @PostMapping("/{id}/calificacion")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Calificar una recomendación",
            description = "Puntuación de 1 a 5 y comentario opcional. Solo se permite una calificación por recomendación.")
    public CalificacionResponse calificar(@PathVariable Long id, @Valid @RequestBody CalificacionRequest request) {
        return calificacionService.calificar(id, request);
    }
}

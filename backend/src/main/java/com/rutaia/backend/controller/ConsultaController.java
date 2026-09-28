package com.rutaia.backend.controller;

import com.rutaia.backend.dto.consulta.ConsultaRequest;
import com.rutaia.backend.dto.consulta.ConsultaResponse;
import com.rutaia.backend.service.ConsultaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints de consultas: el punto de entrada al motor de recomendaciones.
 */
@RestController
@RequestMapping("/api/consultas")
@RequiredArgsConstructor
@Tag(name = "Consultas", description = "Preguntas de los estudiantes y recomendaciones generadas con RAG")
public class ConsultaController {

    private final ConsultaService consultaService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Realizar una consulta",
            description = "Registra la pregunta, la envía al flujo RAG de n8n y devuelve la recomendación. "
                    + "El estado final puede ser RESPONDIDA, SIN_RESULTADOS o ERROR.")
    public ConsultaResponse realizarConsulta(@Valid @RequestBody ConsultaRequest request) {
        return consultaService.realizarConsulta(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ver el detalle completo de una consulta")
    public ConsultaResponse detalle(@PathVariable Long id) {
        return consultaService.obtenerDetalle(id);
    }
}

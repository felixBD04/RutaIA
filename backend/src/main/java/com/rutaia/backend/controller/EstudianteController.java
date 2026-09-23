package com.rutaia.backend.controller;

import com.rutaia.backend.dto.consulta.ConsultaResponse;
import com.rutaia.backend.dto.estudiante.EstudianteRequest;
import com.rutaia.backend.dto.estudiante.EstudianteResponse;
import com.rutaia.backend.service.ConsultaService;
import com.rutaia.backend.service.EstudianteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints REST de estudiantes.
 * El controlador NO tiene logica de negocio: recibe, delega al servicio y responde.
 */
@RestController
@RequestMapping("/api/estudiantes")
@RequiredArgsConstructor
@Tag(name = "Estudiantes", description = "Registro, consulta e historial de estudiantes")
public class EstudianteController {

    private final EstudianteService estudianteService;
    private final ConsultaService consultaService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // 201 Created, no 200
    @Operation(summary = "Registrar un estudiante",
            description = "Niveles permitidos: Principiante, Intermedio, Avanzado. El correo no puede repetirse.")
    public EstudianteResponse registrar(@Valid @RequestBody EstudianteRequest request) {
        return estudianteService.registrar(request);
    }

    @GetMapping
    @Operation(summary = "Listar estudiantes registrados")
    public List<EstudianteResponse> listar() {
        return estudianteService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar un estudiante por id")
    public EstudianteResponse buscarPorId(@PathVariable Long id) {
        return estudianteService.buscarPorId(id);
    }

    @GetMapping("/{id}/historial")
    @Operation(summary = "Historial de consultas de un estudiante",
            description = "Consultas de la más reciente a la más antigua, con su estado, recomendación y fuentes")
    public List<ConsultaResponse> historial(@PathVariable Long id) {
        return consultaService.historial(id);
    }
}

package com.rutaia.backend.controller;

import com.rutaia.backend.dto.curso.CursoResponse;
import com.rutaia.backend.model.enums.NivelCurso;
import com.rutaia.backend.service.CursoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Catalogo publico de cursos (RF 04). Solo muestra cursos activos.
 */
@RestController
@RequestMapping("/api/cursos")
@RequiredArgsConstructor
@Tag(name = "Catálogo de cursos", description = "Consulta del catálogo para estudiantes (solo cursos activos)")
public class CatalogoController {

    private final CursoService cursoService;

    @GetMapping
    @Operation(summary = "Listar cursos activos",
            description = "Filtros opcionales por categoría y nivel (Básico, Intermedio, Avanzado)")
    public List<CursoResponse> catalogo(
            @Parameter(description = "Categoría, por ejemplo: Desarrollo Web")
            @RequestParam(required = false) String categoria,
            @Parameter(description = "Nivel: Básico, Intermedio o Avanzado")
            @RequestParam(required = false) String nivel) {

        // Se recibe como texto para aceptar "Básico", "basico", "BASICO"...
        NivelCurso nivelCurso = (nivel == null || nivel.isBlank()) ? null : NivelCurso.desdeTexto(nivel);
        return cursoService.catalogo(categoria, nivelCurso);
    }

    @GetMapping("/categorias")
    @Operation(summary = "Listar las categorías disponibles", description = "Útil para llenar el filtro del frontend")
    public List<String> categorias() {
        return cursoService.categorias();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ver el detalle de un curso activo")
    public CursoResponse detalle(@PathVariable Long id) {
        return cursoService.buscarActivoPorId(id);
    }
}

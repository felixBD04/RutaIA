package com.rutaia.backend.controller;

import com.rutaia.backend.dto.curso.CursoRequest;
import com.rutaia.backend.dto.curso.CursoResponse;
import com.rutaia.backend.service.CursoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Operaciones administrativas de cursos (RF 03).
 * Sin autenticacion por ahora: se prueban desde Swagger o Postman.
 */
@RestController
@RequestMapping("/api/admin/cursos")
@RequiredArgsConstructor
@Tag(name = "Administración de cursos", description = "Registrar, consultar, actualizar y desactivar cursos")
public class CursoAdminController {

    private final CursoService cursoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar un curso")
    public CursoResponse registrar(@Valid @RequestBody CursoRequest request) {
        return cursoService.registrar(request);
    }

    @GetMapping
    @Operation(summary = "Listar todos los cursos (activos e inactivos)")
    public List<CursoResponse> listarTodos() {
        return cursoService.listarTodos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar un curso por id (activo o inactivo)")
    public CursoResponse buscarPorId(@PathVariable Long id) {
        return cursoService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar todos los datos de un curso")
    public CursoResponse actualizar(@PathVariable Long id, @Valid @RequestBody CursoRequest request) {
        return cursoService.actualizar(id, request);
    }

    @PatchMapping("/{id}/desactivar")
    @Operation(summary = "Desactivar un curso", description = "Deja de mostrarse en el catálogo y de recomendarse")
    public CursoResponse desactivar(@PathVariable Long id) {
        return cursoService.desactivar(id);
    }

    @PatchMapping("/{id}/activar")
    @Operation(summary = "Volver a activar un curso")
    public CursoResponse activar(@PathVariable Long id) {
        return cursoService.activar(id);
    }
}

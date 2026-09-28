package com.rutaia.backend.dto.curso;

import com.rutaia.backend.model.Curso;
import com.rutaia.backend.model.enums.NivelCurso;

import java.time.LocalDateTime;

/**
 * Datos que la API devuelve sobre un curso.
 */
public record CursoResponse(
        Long id,
        String nombre,
        String descripcion,
        String categoria,
        NivelCurso nivel,
        Integer duracionHoras,
        Boolean activo,
        LocalDateTime fechaCreacion
) {

    public static CursoResponse desde(Curso curso) {
        return new CursoResponse(
                curso.getId(),
                curso.getNombre(),
                curso.getDescripcion(),
                curso.getCategoria(),
                curso.getNivel(),
                curso.getDuracionHoras(),
                curso.getActivo(),
                curso.getFechaCreacion()
        );
    }
}

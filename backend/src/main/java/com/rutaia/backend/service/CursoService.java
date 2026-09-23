package com.rutaia.backend.service;

import com.rutaia.backend.dto.curso.CursoRequest;
import com.rutaia.backend.dto.curso.CursoResponse;
import com.rutaia.backend.exception.ConflictoException;
import com.rutaia.backend.exception.RecursoNoEncontradoException;
import com.rutaia.backend.model.Curso;
import com.rutaia.backend.model.enums.NivelCurso;
import com.rutaia.backend.repository.CursoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Logica de negocio de cursos: administracion (RF 03) y catalogo (RF 04).
 */
@Service
@RequiredArgsConstructor
public class CursoService {

    private final CursoRepository cursoRepository;

    // ================== ADMINISTRACION (RF 03) ==================

    @Transactional
    public CursoResponse registrar(CursoRequest request) {
        if (cursoRepository.existsByNombreIgnoreCase(request.nombre().trim())) {
            throw new ConflictoException("Ya existe un curso con el nombre '" + request.nombre().trim() + "'");
        }
        Curso curso = new Curso();
        aplicarDatos(curso, request);
        return CursoResponse.desde(cursoRepository.save(curso));
    }

    /** Lista todos los cursos, activos e inactivos (vista de administrador). */
    @Transactional(readOnly = true)
    public List<CursoResponse> listarTodos() {
        return cursoRepository.findAll().stream()
                .map(CursoResponse::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public CursoResponse buscarPorId(Long id) {
        return CursoResponse.desde(obtenerEntidad(id));
    }

    @Transactional
    public CursoResponse actualizar(Long id, CursoRequest request) {
        Curso curso = obtenerEntidad(id);
        if (cursoRepository.existsByNombreIgnoreCaseAndIdNot(request.nombre().trim(), id)) {
            throw new ConflictoException("Ya existe otro curso con el nombre '" + request.nombre().trim() + "'");
        }
        aplicarDatos(curso, request);
        // No hace falta llamar a save(): dentro de @Transactional, JPA detecta
        // los cambios de la entidad y ejecuta el UPDATE al terminar el metodo.
        return CursoResponse.desde(curso);
    }

    /** Desactivacion logica: el curso sigue en la base, pero ya no se muestra ni se recomienda. */
    @Transactional
    public CursoResponse desactivar(Long id) {
        Curso curso = obtenerEntidad(id);
        if (!curso.getActivo()) {
            throw new ConflictoException("El curso con id " + id + " ya está desactivado");
        }
        curso.setActivo(false);
        return CursoResponse.desde(curso);
    }

    @Transactional
    public CursoResponse activar(Long id) {
        Curso curso = obtenerEntidad(id);
        if (curso.getActivo()) {
            throw new ConflictoException("El curso con id " + id + " ya está activo");
        }
        curso.setActivo(true);
        return CursoResponse.desde(curso);
    }

    // ================== CATALOGO (RF 04) ==================

    /** Cursos activos, con filtros opcionales por categoria y nivel. */
    @Transactional(readOnly = true)
    public List<CursoResponse> catalogo(String categoria, NivelCurso nivel) {
        boolean hayCategoria = categoria != null && !categoria.isBlank();

        List<Curso> cursos;
        if (hayCategoria && nivel != null) {
            cursos = cursoRepository.findByActivoTrueAndCategoriaIgnoreCaseAndNivelOrderByNombreAsc(categoria.trim(), nivel);
        } else if (hayCategoria) {
            cursos = cursoRepository.findByActivoTrueAndCategoriaIgnoreCaseOrderByNombreAsc(categoria.trim());
        } else if (nivel != null) {
            cursos = cursoRepository.findByActivoTrueAndNivelOrderByNombreAsc(nivel);
        } else {
            cursos = cursoRepository.findByActivoTrueOrderByNombreAsc();
        }
        return cursos.stream().map(CursoResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public List<String> categorias() {
        return cursoRepository.findCategoriasActivas();
    }

    /** Detalle de un curso del catalogo: si esta inactivo, para el estudiante "no existe". */
    @Transactional(readOnly = true)
    public CursoResponse buscarActivoPorId(Long id) {
        Curso curso = obtenerEntidad(id);
        if (!curso.getActivo()) {
            throw new RecursoNoEncontradoException("No existe un curso activo con id " + id);
        }
        return CursoResponse.desde(curso);
    }

    // ================== AUXILIARES ==================

    @Transactional(readOnly = true)
    public Curso obtenerEntidad(Long id) {
        return cursoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un curso con id " + id));
    }

    private void aplicarDatos(Curso curso, CursoRequest request) {
        curso.setNombre(request.nombre().trim());
        curso.setDescripcion(request.descripcion().trim());
        curso.setCategoria(request.categoria().trim());
        curso.setNivel(request.nivel());
        curso.setDuracionHoras(request.duracionHoras());
    }
}

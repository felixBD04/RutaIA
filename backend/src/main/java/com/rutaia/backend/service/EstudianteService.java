package com.rutaia.backend.service;

import com.rutaia.backend.dto.estudiante.EstudianteRequest;
import com.rutaia.backend.dto.estudiante.EstudianteResponse;
import com.rutaia.backend.exception.ConflictoException;
import com.rutaia.backend.exception.RecursoNoEncontradoException;
import com.rutaia.backend.model.Estudiante;
import com.rutaia.backend.repository.EstudianteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Logica de negocio de estudiantes (RF 01 y RF 02).
 */
@Service
@RequiredArgsConstructor // Lombok crea el constructor: asi Spring inyecta el repositorio
public class EstudianteService {

    private final EstudianteRepository estudianteRepository;

    /** RF 01: registrar un estudiante impidiendo correos duplicados. */
    @Transactional
    public EstudianteResponse registrar(EstudianteRequest request) {
        String correo = request.correo().trim().toLowerCase();

        if (estudianteRepository.existsByCorreoIgnoreCase(correo)) {
            throw new ConflictoException("Ya existe un estudiante registrado con el correo " + correo);
        }

        Estudiante estudiante = new Estudiante();
        estudiante.setNombreCompleto(request.nombreCompleto().trim());
        estudiante.setCorreo(correo);
        estudiante.setNivelExperiencia(request.nivelExperiencia());
        estudiante.setAreaInteres(request.areaInteres().trim());

        return EstudianteResponse.desde(estudianteRepository.save(estudiante));
    }

    /** RF 02: listar estudiantes. */
    @Transactional(readOnly = true)
    public List<EstudianteResponse> listar() {
        return estudianteRepository.findAll().stream()
                .map(EstudianteResponse::desde)
                .toList();
    }

    /** RF 02: consultar un estudiante por id. */
    @Transactional(readOnly = true)
    public EstudianteResponse buscarPorId(Long id) {
        return EstudianteResponse.desde(obtenerEntidad(id));
    }

    /**
     * Devuelve la entidad o lanza 404.
     * Es publico porque otros servicios (consultas, historial) lo reutilizaran.
     */
    @Transactional(readOnly = true)
    public Estudiante obtenerEntidad(Long id) {
        return estudianteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un estudiante con id " + id));
    }
}

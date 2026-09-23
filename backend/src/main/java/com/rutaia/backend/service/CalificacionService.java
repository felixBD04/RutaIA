package com.rutaia.backend.service;

import com.rutaia.backend.dto.calificacion.CalificacionRequest;
import com.rutaia.backend.dto.calificacion.CalificacionResponse;
import com.rutaia.backend.exception.ConflictoException;
import com.rutaia.backend.exception.RecursoNoEncontradoException;
import com.rutaia.backend.model.Calificacion;
import com.rutaia.backend.model.Recomendacion;
import com.rutaia.backend.repository.CalificacionRepository;
import com.rutaia.backend.repository.RecomendacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Logica de negocio de calificaciones (RF 17).
 */
@Service
@RequiredArgsConstructor
public class CalificacionService {

    private final CalificacionRepository calificacionRepository;
    private final RecomendacionRepository recomendacionRepository;

    @Transactional
    public CalificacionResponse calificar(Long recomendacionId, CalificacionRequest request) {
        Recomendacion recomendacion = recomendacionRepository.findById(recomendacionId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una recomendación con id " + recomendacionId));

        // Capa 1: regla de negocio. Capa 2: la restriccion UNIQUE de la base
        // (si llegaran dos peticiones al mismo tiempo, el GlobalExceptionHandler responde 409).
        if (calificacionRepository.existsByRecomendacionId(recomendacionId)) {
            throw new ConflictoException("Esta recomendación ya fue calificada. Solo se permite una calificación.");
        }

        Calificacion calificacion = new Calificacion();
        calificacion.setRecomendacion(recomendacion);
        calificacion.setPuntuacion(request.puntuacion().shortValue());
        calificacion.setComentario(
                request.comentario() == null || request.comentario().isBlank() ? null : request.comentario().trim());

        return CalificacionResponse.desde(calificacionRepository.save(calificacion));
    }
}

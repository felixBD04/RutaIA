package com.rutaia.backend.service;

import com.rutaia.backend.dto.estadistica.EstadisticasResponse;
import com.rutaia.backend.dto.estadistica.EstadisticasResponse.CursoRecomendado;
import com.rutaia.backend.model.enums.EstadoConsulta;
import com.rutaia.backend.repository.CalificacionRepository;
import com.rutaia.backend.repository.ConsultaRepository;
import com.rutaia.backend.repository.CursoRepository;
import com.rutaia.backend.repository.EstudianteRepository;
import com.rutaia.backend.repository.RecomendacionRepository;
import com.rutaia.backend.repository.projection.ConteoPorEstado;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Estadisticas basicas (RF 18).
 * Todos los conteos y promedios los calcula PostgreSQL: Java solo arma la respuesta.
 */
@Service
@RequiredArgsConstructor
public class EstadisticaService {

    private static final int TOP_CURSOS = 5;

    private final EstudianteRepository estudianteRepository;
    private final CursoRepository cursoRepository;
    private final ConsultaRepository consultaRepository;
    private final RecomendacionRepository recomendacionRepository;
    private final CalificacionRepository calificacionRepository;

    @Transactional(readOnly = true)
    public EstadisticasResponse obtener() {
        // Consultas por estado: se inicializan todos en 0 para que siempre aparezcan los 4
        Map<String, Long> porEstado = new LinkedHashMap<>();
        for (EstadoConsulta estado : EstadoConsulta.values()) {
            porEstado.put(estado.name(), 0L);
        }
        for (ConteoPorEstado conteo : consultaRepository.contarPorEstado()) {
            porEstado.put(conteo.getEstado().name(), conteo.getTotal());
        }

        List<CursoRecomendado> top = recomendacionRepository
                .cursosMasRecomendados(PageRequest.of(0, TOP_CURSOS)).stream()
                .map(c -> new CursoRecomendado(c.getCursoId(), c.getNombre(), c.getTotal()))
                .toList();

        Double promedio = calificacionRepository.promedioPuntuacion();

        return new EstadisticasResponse(
                estudianteRepository.count(),
                cursoRepository.countByActivoTrue(),
                consultaRepository.count(),
                porEstado,
                top.isEmpty() ? null : top.getFirst(),
                top,
                calificacionRepository.count(),
                promedio == null ? null : Math.round(promedio * 100.0) / 100.0  // 2 decimales
        );
    }
}

package com.rutaia.backend.service;

import com.rutaia.backend.dto.consulta.ConsultaRequest;
import com.rutaia.backend.dto.consulta.ConsultaResponse;
import com.rutaia.backend.exception.RecursoNoEncontradoException;
import com.rutaia.backend.exception.RespuestaInvalidaException;
import com.rutaia.backend.integration.n8n.N8nClient;
import com.rutaia.backend.integration.n8n.N8nConsultaRequest;
import com.rutaia.backend.integration.n8n.N8nRespuesta;
import com.rutaia.backend.model.Consulta;
import com.rutaia.backend.model.Curso;
import com.rutaia.backend.model.Estudiante;
import com.rutaia.backend.model.Recomendacion;
import com.rutaia.backend.model.enums.EstadoConsulta;
import com.rutaia.backend.repository.ConsultaRepository;
import com.rutaia.backend.repository.CursoRepository;
import com.rutaia.backend.repository.RecomendacionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Orquesta el ciclo completo de una consulta (RF 06, 07, 08, 13, 14, 16).
 *
 * IMPORTANTE: la llamada a n8n se hace FUERA de cualquier transaccion.
 *  - Tx 1: guardar la consulta PENDIENTE y confirmarla de inmediato.
 *  - Llamada a n8n (puede tardar varios segundos o fallar).
 *  - Tx 2: guardar la recomendacion y el estado final.
 *  - Tx 3 (solo si algo falla): marcar la consulta como ERROR.
 */
@Slf4j
@Service
public class ConsultaService {

    private static final String MENSAJE_SERVICIO_NO_DISPONIBLE =
            "El servicio de recomendaciones no está disponible en este momento. Intenta de nuevo más tarde.";
    private static final String MENSAJE_SIN_RESULTADOS =
            "No encontramos cursos en nuestro catálogo relacionados con tu necesidad.";

    private final ConsultaRepository consultaRepository;
    private final RecomendacionRepository recomendacionRepository;
    private final CursoRepository cursoRepository;
    private final EstudianteService estudianteService;
    private final N8nClient n8nClient;
    private final TransactionTemplate tx;
    private final TransactionTemplate txLectura;

    public ConsultaService(ConsultaRepository consultaRepository,
                           RecomendacionRepository recomendacionRepository,
                           CursoRepository cursoRepository,
                           EstudianteService estudianteService,
                           N8nClient n8nClient,
                           PlatformTransactionManager transactionManager) {
        this.consultaRepository = consultaRepository;
        this.recomendacionRepository = recomendacionRepository;
        this.cursoRepository = cursoRepository;
        this.estudianteService = estudianteService;
        this.n8nClient = n8nClient;
        this.tx = new TransactionTemplate(transactionManager);
        this.txLectura = new TransactionTemplate(transactionManager);
        this.txLectura.setReadOnly(true);
    }

    // =====================================================================
    // RF 06 - 08, 13, 14: realizar una consulta
    // =====================================================================

    public ConsultaResponse realizarConsulta(ConsultaRequest request) {
        String pregunta = request.pregunta().trim();

        // ---- Tx 1: validar el estudiante y guardar la consulta como PENDIENTE ----
        // Si el estudiante no existe, lanza 404 y no se crea nada.
        Consulta pendiente = tx.execute(status -> {
            Estudiante estudiante = estudianteService.obtenerEntidad(request.estudianteId());
            Consulta consulta = new Consulta();
            consulta.setEstudiante(estudiante);
            consulta.setPregunta(pregunta);
            consulta.setEstado(EstadoConsulta.PENDIENTE);
            return consultaRepository.save(consulta);
        });
        Long consultaId = pendiente.getId();
        Estudiante estudiante = pendiente.getEstudiante();
        log.info("Consulta {} registrada como PENDIENTE", consultaId);

        // ---- Llamada a n8n, fuera de cualquier transaccion ----
        N8nRespuesta respuesta;
        try {
            respuesta = n8nClient.consultar(new N8nConsultaRequest(
                    consultaId,
                    pregunta,
                    estudiante.getNivelExperiencia().name(),
                    estudiante.getAreaInteres()));
        } catch (RestClientException ex) {
            // n8n caido, timeout, error de Qdrant u OpenRouter dentro del flujo...
            log.error("Fallo la comunicación con n8n para la consulta {}: {}", consultaId, ex.getMessage());
            return finalizarConError(consultaId, MENSAJE_SERVICIO_NO_DISPONIBLE);
        }

        // ---- Tx 2: validar la respuesta y guardar el resultado ----
        try {
            tx.executeWithoutResult(status -> guardarResultado(consultaId, respuesta));
        } catch (RespuestaInvalidaException ex) {
            // La Tx 2 hizo rollback completo: no queda ninguna recomendacion a medias
            log.warn("Respuesta inválida de n8n para la consulta {}: {}", consultaId, ex.getMessage());
            return finalizarConError(consultaId, ex.getMessage());
        }

        return obtenerDetalle(consultaId);
    }

    private void guardarResultado(Long consultaId, N8nRespuesta respuesta) {
        Consulta consulta = consultaRepository.findById(consultaId)
                .orElseThrow(() -> new RespuestaInvalidaException("La consulta " + consultaId + " ya no existe"));

        if (respuesta == null || respuesta.estado() == null) {
            throw new RespuestaInvalidaException("El servicio de recomendaciones devolvió una respuesta vacía");
        }

        switch (respuesta.estado()) {
            case "RESPONDIDA" -> guardarRecomendacion(consulta, respuesta);
            case "SIN_RESULTADOS" -> guardarSinResultados(consulta, respuesta);
            default -> throw new RespuestaInvalidaException(
                    "El servicio de recomendaciones reportó un error al procesar la consulta");
        }
        // No hace falta save(): la consulta esta dentro de la transaccion (dirty checking)
    }

    private void guardarRecomendacion(Consulta consulta, N8nRespuesta respuesta) {
        if (respuesta.respuesta() == null || respuesta.respuesta().isBlank()) {
            throw new RespuestaInvalidaException("El modelo de lenguaje no generó una recomendación");
        }
        List<N8nRespuesta.Fuente> fuentes = respuesta.fuentes();
        if (fuentes == null || fuentes.isEmpty()) {
            throw new RespuestaInvalidaException("La recomendación no incluye cursos como fuentes");
        }

        // Regla de negocio: las fuentes deben existir en la base relacional y estar activas.
        // Se buscan todos los cursos en una sola consulta.
        List<Long> ids = fuentes.stream().map(N8nRespuesta.Fuente::cursoId).toList();
        Map<Long, Curso> cursos = cursoRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Curso::getId, Function.identity()));

        Recomendacion recomendacion = new Recomendacion();
        recomendacion.setConsulta(consulta);
        recomendacion.setRespuesta(respuesta.respuesta().trim());

        for (int i = 0; i < fuentes.size(); i++) {
            N8nRespuesta.Fuente fuente = fuentes.get(i);
            Curso curso = cursos.get(fuente.cursoId());

            if (curso == null) {
                throw new RespuestaInvalidaException("El curso " + fuente.cursoId()
                        + " recuperado de Qdrant no existe en la base de datos. Ejecuta de nuevo la indexación.");
            }
            if (!curso.getActivo()) {
                throw new RespuestaInvalidaException("El curso '" + curso.getNombre()
                        + "' está inactivo, pero aún aparece en Qdrant. Ejecuta de nuevo la indexación.");
            }
            if (fuente.similitud() == null || fuente.similitud() < -1 || fuente.similitud() > 1) {
                throw new RespuestaInvalidaException("La similitud del curso " + curso.getId() + " no es válida");
            }

            int posicion = fuente.posicion() != null ? fuente.posicion() : i + 1;
            recomendacion.agregarFuente(curso, fuente.similitud(), posicion);
        }

        recomendacionRepository.save(recomendacion);  // guarda tambien las fuentes (cascade)
        consulta.setRecomendacion(recomendacion);
        consulta.setEstado(EstadoConsulta.RESPONDIDA);
    }

    private void guardarSinResultados(Consulta consulta, N8nRespuesta respuesta) {
        // Se guarda el mensaje como recomendacion (sin fuentes) para que quede en el historial
        Recomendacion recomendacion = new Recomendacion();
        recomendacion.setConsulta(consulta);
        recomendacion.setRespuesta(respuesta.respuesta() == null || respuesta.respuesta().isBlank()
                ? MENSAJE_SIN_RESULTADOS
                : respuesta.respuesta().trim());

        recomendacionRepository.save(recomendacion);
        consulta.setRecomendacion(recomendacion);
        consulta.setEstado(EstadoConsulta.SIN_RESULTADOS);
    }

    /** Tx 3: marca la consulta como ERROR y devuelve su detalle con un mensaje explicativo. */
    private ConsultaResponse finalizarConError(Long consultaId, String mensaje) {
        tx.executeWithoutResult(status ->
                consultaRepository.findById(consultaId)
                        .ifPresent(c -> c.setEstado(EstadoConsulta.ERROR)));
        return txLectura.execute(status -> ConsultaResponse.desde(buscarDetalle(consultaId), mensaje));
    }

    // =====================================================================
    // RF 16: detalle e historial
    // =====================================================================

    public ConsultaResponse obtenerDetalle(Long consultaId) {
        return txLectura.execute(status -> ConsultaResponse.desde(buscarDetalle(consultaId), null));
    }

    public List<ConsultaResponse> historial(Long estudianteId) {
        return txLectura.execute(status -> {
            estudianteService.obtenerEntidad(estudianteId); // 404 si el estudiante no existe
            return consultaRepository.findHistorialByEstudianteId(estudianteId).stream()
                    .map(c -> ConsultaResponse.desde(c, null))
                    .toList();
        });
    }

    private Consulta buscarDetalle(Long consultaId) {
        return consultaRepository.findDetalleById(consultaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una consulta con id " + consultaId));
    }
}

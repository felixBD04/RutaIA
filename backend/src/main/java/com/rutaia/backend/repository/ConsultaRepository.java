package com.rutaia.backend.repository;

import com.rutaia.backend.model.Consulta;
import com.rutaia.backend.repository.projection.ConteoPorEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a la tabla "consultas".
 *
 * JOIN FETCH trae en UNA sola consulta SQL la consulta, su estudiante,
 * su recomendacion, las fuentes y los cursos. Sin esto, Hibernate haria
 * una consulta extra por cada relacion (el famoso problema "N+1").
 */
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    @Query("""
            SELECT c FROM Consulta c
            JOIN FETCH c.estudiante
            LEFT JOIN FETCH c.recomendacion r
            LEFT JOIN FETCH r.calificacion
            LEFT JOIN FETCH r.fuentes f
            LEFT JOIN FETCH f.curso
            WHERE c.id = :id
            """)
    Optional<Consulta> findDetalleById(@Param("id") Long id);

    /** RF 16: historial de un estudiante, de la mas reciente a la mas antigua. */
    @Query("""
            SELECT DISTINCT c FROM Consulta c
            JOIN FETCH c.estudiante e
            LEFT JOIN FETCH c.recomendacion r
            LEFT JOIN FETCH r.calificacion
            LEFT JOIN FETCH r.fuentes f
            LEFT JOIN FETCH f.curso
            WHERE e.id = :estudianteId
            ORDER BY c.fechaConsulta DESC
            """)
    List<Consulta> findHistorialByEstudianteId(@Param("estudianteId") Long estudianteId);

    /** RF 18: cantidad de consultas por estado. La base de datos hace el conteo. */
    @Query("SELECT c.estado AS estado, COUNT(c) AS total FROM Consulta c GROUP BY c.estado")
    List<ConteoPorEstado> contarPorEstado();
}

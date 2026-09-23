package com.rutaia.backend.repository;

import com.rutaia.backend.model.Recomendacion;
import com.rutaia.backend.repository.projection.ConteoPorCurso;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Acceso a la tabla "recomendaciones".
 * Las fuentes se guardan automaticamente por el cascade de la entidad.
 */
public interface RecomendacionRepository extends JpaRepository<Recomendacion, Long> {

    /**
     * RF 18: cursos mas recomendados.
     *
     * Solo cuenta un curso cuando el modelo lo MENCIONO en su respuesta
     * (LOCATE busca el nombre del curso dentro del texto). Asi, un curso que
     * solo aparecio en el contexto por similitud, pero que el modelo descarto,
     * no infla la estadistica.
     *
     * Pageable permite pedir solo el top N (por ejemplo, los 5 primeros).
     */
    @Query("""
            SELECT c.id AS cursoId, c.nombre AS nombre, COUNT(f) AS total
            FROM RecomendacionFuente f
            JOIN f.curso c
            JOIN f.recomendacion r
            WHERE LOCATE(LOWER(c.nombre), LOWER(r.respuesta)) > 0
            GROUP BY c.id, c.nombre
            ORDER BY COUNT(f) DESC, c.nombre ASC
            """)
    List<ConteoPorCurso> cursosMasRecomendados(Pageable pageable);
}

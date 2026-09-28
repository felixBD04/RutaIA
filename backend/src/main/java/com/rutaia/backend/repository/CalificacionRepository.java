package com.rutaia.backend.repository;

import com.rutaia.backend.model.Calificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Acceso a la tabla "calificaciones".
 */
public interface CalificacionRepository extends JpaRepository<Calificacion, Long> {

    boolean existsByRecomendacionId(Long recomendacionId);

    /** RF 18: promedio de todas las calificaciones (null si no hay ninguna). */
    @Query("SELECT AVG(c.puntuacion) FROM Calificacion c")
    Double promedioPuntuacion();
}

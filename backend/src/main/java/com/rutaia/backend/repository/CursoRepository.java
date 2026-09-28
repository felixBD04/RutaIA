package com.rutaia.backend.repository;

import com.rutaia.backend.model.Curso;
import com.rutaia.backend.model.enums.NivelCurso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Acceso a la tabla "cursos".
 * Los metodos del catalogo se construyen con el nombre (query methods):
 * findByActivoTrueAndCategoriaIgnoreCaseOrderByNombreAsc se traduce a
 *   SELECT ... WHERE activo = true AND LOWER(categoria) = LOWER(?) ORDER BY nombre ASC
 */
public interface CursoRepository extends JpaRepository<Curso, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    long countByActivoTrue();

    // --- Catalogo (RF 04): solo cursos activos, con o sin filtros ---

    List<Curso> findByActivoTrueOrderByNombreAsc();

    List<Curso> findByActivoTrueAndCategoriaIgnoreCaseOrderByNombreAsc(String categoria);

    List<Curso> findByActivoTrueAndNivelOrderByNombreAsc(NivelCurso nivel);

    List<Curso> findByActivoTrueAndCategoriaIgnoreCaseAndNivelOrderByNombreAsc(String categoria, NivelCurso nivel);

    /** Categorias distintas de los cursos activos, para llenar el filtro del frontend. */
    @Query("SELECT DISTINCT c.categoria FROM Curso c WHERE c.activo = true ORDER BY c.categoria")
    List<String> findCategoriasActivas();
}

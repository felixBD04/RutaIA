package com.rutaia.backend.repository.projection;

/**
 * Proyeccion: cuantas veces fue recomendado un curso.
 */
public interface ConteoPorCurso {

    Long getCursoId();

    String getNombre();

    Long getTotal();
}

package com.rutaia.backend.repository;

import com.rutaia.backend.model.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a la tabla "estudiantes".
 * JpaRepository ya trae save, findById, findAll, deleteById, count...
 * Los metodos declarados aqui los implementa Spring leyendo su NOMBRE:
 * "existsByCorreoIgnoreCase" se traduce a un SELECT que busca el correo
 * sin distinguir mayusculas.
 */
public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {

    boolean existsByCorreoIgnoreCase(String correo);
}

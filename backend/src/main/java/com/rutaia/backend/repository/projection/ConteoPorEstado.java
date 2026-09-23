package com.rutaia.backend.repository.projection;

import com.rutaia.backend.model.enums.EstadoConsulta;

/**
 * Proyeccion: resultado de "SELECT estado, COUNT(*) ... GROUP BY estado".
 * Spring Data llena esta interfaz usando los alias de la consulta (AS estado, AS total).
 */
public interface ConteoPorEstado {

    EstadoConsulta getEstado();

    Long getTotal();
}

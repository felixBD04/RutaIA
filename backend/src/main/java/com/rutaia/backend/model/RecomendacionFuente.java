package com.rutaia.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representa una fila de la tabla intermedia "recomendacion_fuentes".
 * Une una recomendacion con un curso y guarda la similitud que obtuvo
 * ese curso para esa pregunta en particular.
 */
@Entity
@Table(name = "recomendacion_fuentes")
@Getter
@Setter
@NoArgsConstructor
public class RecomendacionFuente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recomendacion_id", nullable = false)
    private Recomendacion recomendacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    @Column(name = "similitud", nullable = false)
    private Double similitud;

    @Column(name = "posicion", nullable = false)
    private Integer posicion;
}

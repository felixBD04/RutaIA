package com.rutaia.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Representa una fila de la tabla "calificaciones" (RF 17).
 * Una recomendacion tiene como maximo una calificacion (recomendacion_id es UNIQUE).
 */
@Entity
@Table(name = "calificaciones")
@Getter
@Setter
@NoArgsConstructor
public class Calificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recomendacion_id", nullable = false, unique = true)
    private Recomendacion recomendacion;

    // Short porque la columna es SMALLINT (validate exige que los tipos coincidan)
    @Column(name = "puntuacion", nullable = false)
    private Short puntuacion;

    @Column(name = "comentario", length = 500)
    private String comentario;

    @Column(name = "fecha", nullable = false, updatable = false)
    private LocalDateTime fecha;

    @PrePersist
    void alCrear() {
        this.fecha = LocalDateTime.now();
    }
}

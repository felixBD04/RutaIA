package com.rutaia.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa una fila de la tabla "recomendaciones".
 * Guarda el texto generado por el modelo y, a traves de sus fuentes,
 * los cursos que se usaron como contexto (relacion N:M con cursos).
 */
@Entity
@Table(name = "recomendaciones")
@Getter
@Setter
@NoArgsConstructor
public class Recomendacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "consulta_id", nullable = false, unique = true)
    private Consulta consulta;

    @Column(name = "respuesta", nullable = false, columnDefinition = "TEXT")
    private String respuesta;

    @Column(name = "fecha_generacion", nullable = false, updatable = false)
    private LocalDateTime fechaGeneracion;

    // cascade ALL: al guardar la recomendacion se guardan tambien sus fuentes
    @OneToMany(mappedBy = "recomendacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("posicion ASC")
    private List<RecomendacionFuente> fuentes = new ArrayList<>();

    // Lado inverso: la clave foranea esta en la tabla "calificaciones"
    @OneToOne(mappedBy = "recomendacion", fetch = FetchType.LAZY)
    private Calificacion calificacion;

    @PrePersist
    void alCrear() {
        this.fechaGeneracion = LocalDateTime.now();
    }

    /** Agrega una fuente manteniendo sincronizados los dos lados de la relacion. */
    public void agregarFuente(Curso curso, double similitud, int posicion) {
        RecomendacionFuente fuente = new RecomendacionFuente();
        fuente.setRecomendacion(this);
        fuente.setCurso(curso);
        fuente.setSimilitud(similitud);
        fuente.setPosicion(posicion);
        this.fuentes.add(fuente);
    }
}

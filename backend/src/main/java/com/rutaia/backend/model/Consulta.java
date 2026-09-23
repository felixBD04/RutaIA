package com.rutaia.backend.model;

import com.rutaia.backend.model.enums.EstadoConsulta;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Representa una fila de la tabla "consultas".
 * Relaciones:
 *  - Muchas consultas pertenecen a un estudiante (ManyToOne).
 *  - Una consulta tiene como maximo una recomendacion (OneToOne).
 */
@Entity
@Table(name = "consultas")
@Getter
@Setter
@NoArgsConstructor
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // LAZY: el estudiante solo se carga desde la base si realmente se necesita
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @Column(name = "pregunta", nullable = false, columnDefinition = "TEXT")
    private String pregunta;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoConsulta estado;

    @Column(name = "fecha_consulta", nullable = false, updatable = false)
    private LocalDateTime fechaConsulta;

    // mappedBy: la clave foranea esta en la tabla "recomendaciones", no aqui
    @OneToOne(mappedBy = "consulta", fetch = FetchType.LAZY)
    private Recomendacion recomendacion;

    @PrePersist
    void alCrear() {
        this.fechaConsulta = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = EstadoConsulta.PENDIENTE;
        }
    }
}

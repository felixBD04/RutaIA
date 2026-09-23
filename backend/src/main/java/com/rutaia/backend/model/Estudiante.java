package com.rutaia.backend.model;

import com.rutaia.backend.model.enums.NivelExperiencia;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Representa una fila de la tabla "estudiantes".
 * Cada atributo se corresponde con una columna del script database/schema.sql.
 */
@Entity
@Table(name = "estudiantes")
@Getter
@Setter
@NoArgsConstructor
public class Estudiante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // lo genera el BIGSERIAL
    private Long id;

    @Column(name = "nombre_completo", nullable = false, length = 150)
    private String nombreCompleto;

    @Column(name = "correo", nullable = false, length = 150, unique = true)
    private String correo;

    @Enumerated(EnumType.STRING) // guarda "PRINCIPIANTE" y no un numero
    @Column(name = "nivel_experiencia", nullable = false, length = 20)
    private NivelExperiencia nivelExperiencia;

    @Column(name = "area_interes", nullable = false, length = 100)
    private String areaInteres;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    /** Se ejecuta automaticamente justo antes del INSERT. */
    @PrePersist
    void alCrear() {
        this.fechaRegistro = LocalDateTime.now();
    }
}

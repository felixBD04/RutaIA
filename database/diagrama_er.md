# Diagrama Entidad-Relacion - RutaIA

```mermaid
erDiagram
    ESTUDIANTES ||--o{ CONSULTAS : "realiza"
    CONSULTAS ||--o| RECOMENDACIONES : "genera"
    RECOMENDACIONES ||--|{ RECOMENDACION_FUENTES : "usa"
    CURSOS ||--o{ RECOMENDACION_FUENTES : "aparece en"
    RECOMENDACIONES ||--o| CALIFICACIONES : "recibe"

    ESTUDIANTES {
        bigint id PK
        varchar nombre_completo
        varchar correo UK
        varchar nivel_experiencia "PRINCIPIANTE | INTERMEDIO | AVANZADO"
        varchar area_interes
        timestamp fecha_registro
    }

    CURSOS {
        bigint id PK
        varchar nombre
        text descripcion
        varchar categoria
        varchar nivel "BASICO | INTERMEDIO | AVANZADO"
        int duracion_horas "mayor que 0"
        boolean activo
        timestamp fecha_creacion
    }

    CONSULTAS {
        bigint id PK
        bigint estudiante_id FK
        text pregunta
        varchar estado "PENDIENTE | RESPONDIDA | SIN_RESULTADOS | ERROR"
        timestamp fecha_consulta
    }

    RECOMENDACIONES {
        bigint id PK
        bigint consulta_id FK,UK
        text respuesta
        timestamp fecha_generacion
    }

    RECOMENDACION_FUENTES {
        bigint id PK
        bigint recomendacion_id FK
        bigint curso_id FK
        double similitud "score de Qdrant"
        int posicion
    }

    CALIFICACIONES {
        bigint id PK
        bigint recomendacion_id FK,UK
        smallint puntuacion "1 a 5"
        varchar comentario "opcional"
        timestamp fecha
    }
```

## Relaciones

| Relacion | Tipo | Como se implementa |
|---|---|---|
| Estudiante - Consultas | 1:N | `consultas.estudiante_id` (FK) |
| Consulta - Recomendacion | 1:0..1 | `recomendaciones.consulta_id` (FK + UNIQUE) |
| Recomendacion - Cursos | N:M | tabla intermedia `recomendacion_fuentes` |
| Recomendacion - Calificacion | 1:0..1 | `calificaciones.recomendacion_id` (FK + UNIQUE) |

## Indices justificados

| Indice | Justificacion |
|---|---|
| `idx_cursos_categoria_nivel` | Filtros del catalogo por categoria y nivel (RF 04) |
| `idx_cursos_activo` | El catalogo y la busqueda solo usan cursos activos |
| `idx_consultas_estudiante_fecha` | Historial del estudiante ordenado por fecha (RF 16) |
| `idx_consultas_estado` | Estadisticas por estado de consulta (RF 18) |
| `idx_fuentes_curso` | Calculo del curso mas recomendado (RF 18) |

Las restricciones UNIQUE (correo, consulta_id, recomendacion_id) crean su propio indice automaticamente.

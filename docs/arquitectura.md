# Arquitectura de RutaIA

## Vista de componentes

```mermaid
flowchart LR
    subgraph Navegador
        FE["Frontend<br/>HTML, CSS, JavaScript<br/>(nginx, puerto 3000)"]
    end

    subgraph Backend
        API["API REST<br/>Spring Boot<br/>(puerto 8080)"]
    end

    subgraph Datos
        PG[("PostgreSQL<br/>base relacional<br/>(puerto 5432)")]
        QD[("Qdrant<br/>base vectorial<br/>(puerto 6333)")]
    end

    subgraph Automatizacion
        N8N["n8n<br/>flujos de indexación y RAG<br/>(puerto 5678)"]
    end

    OR["OpenRouter<br/>embeddings y LLM"]

    FE -- "HTTP/JSON (Fetch API)" --> API
    API -- "JPA / JDBC" --> PG
    API -- "Webhook HTTP" --> N8N
    N8N -- "Búsqueda y upsert de vectores" --> QD
    N8N -- "Lectura de cursos (indexación)" --> PG
    N8N -- "Embeddings y chat completions" --> OR
```

**Reglas de comunicación:**

- El frontend **solo** se comunica con la API de Spring Boot. Nunca llama directamente a n8n,
  Qdrant, OpenRouter ni PostgreSQL.
- Spring Boot es el dueño de la base relacional y la **fuente de la verdad**: valida que las fuentes
  devueltas por Qdrant existan y estén activas antes de guardarlas.
- n8n es el único componente que conoce la API key de OpenRouter (guardada cifrada como credencial).
- PostgreSQL, Qdrant, n8n y el frontend corren como contenedores Docker en la misma red interna.
  Dentro de esa red se comunican por nombre de servicio (por ejemplo, `http://qdrant:6333`).

## Secuencia de una consulta (flujo RAG)

```mermaid
sequenceDiagram
    autonumber
    actor E as Estudiante
    participant FE as Frontend
    participant API as Spring Boot
    participant PG as PostgreSQL
    participant N8N as n8n
    participant OR as OpenRouter
    participant QD as Qdrant

    E->>FE: Escribe su pregunta
    FE->>API: POST /api/consultas
    API->>PG: Valida el estudiante y guarda la consulta (PENDIENTE)
    Note over API,PG: Transacción 1 confirmada antes de llamar a n8n
    API->>N8N: POST /webhook/rutaia-consulta
    N8N->>N8N: Valida que la pregunta no esté vacía
    N8N->>OR: Embedding de la pregunta
    OR-->>N8N: Vector de 1536 dimensiones
    N8N->>QD: Búsqueda semántica (top 5, solo activos)
    QD-->>N8N: Cursos con su score y payload
    N8N->>N8N: Aplica el umbral de relevancia (0.45)

    alt Ningún curso supera el umbral
        N8N-->>API: estado SIN_RESULTADOS (sin llamar al LLM)
    else Hay cursos relevantes
        N8N->>OR: Chat completion con el contexto de los cursos
        OR-->>N8N: Recomendación en texto
        N8N-->>API: estado RESPONDIDA, respuesta y fuentes
    end

    API->>PG: Valida fuentes, guarda recomendación y fuentes, actualiza estado
    Note over API,PG: Transacción 2 (rollback completo si algo es inválido)

    opt n8n, Qdrant u OpenRouter fallan
        API->>PG: Marca la consulta como ERROR (Transacción 3)
    end

    API-->>FE: Consulta con estado, recomendación, fuentes y similitudes
    FE-->>E: Ruta sugerida
```

## Flujo de indexación

```mermaid
flowchart LR
    A[Inicio manual] --> B{"¿Existe la colección<br/>cursos?"}
    B -- No --> C["Crear colección<br/>1536 dimensiones, Cosine"]
    C --> D[Crear índice del campo activo]
    D --> E
    B -- Sí --> E[Leer cursos de PostgreSQL]
    E --> F[Preparar texto de cada curso]
    F --> G[Embedding en OpenRouter]
    G --> H["Validar vector y<br/>construir puntos"]
    H --> I["Upsert en Qdrant<br/>id del punto = id del curso"]
```

El ID de cada punto en Qdrant es el mismo ID del curso en PostgreSQL. Como el guardado es un
*upsert*, ejecutar el flujo varias veces actualiza los puntos existentes en lugar de duplicarlos.

## Decisiones técnicas principales

| Decisión | Motivo |
|---|---|
| Llamada a n8n fuera de transacciones | Permite registrar la consulta como PENDIENTE antes de llamar a n8n y no bloquear conexiones de la base durante la llamada al LLM. |
| Validación de fuentes en Spring Boot | Qdrant es una copia de los datos y puede desactualizarse. PostgreSQL es la fuente de la verdad. |
| Umbral de 0.45 calibrado con datos reales | Ver `docs/calibracion_umbral.md`. |
| Doble filtro: umbral y prompt | El umbral elimina el ruido grueso; el prompt, las coincidencias por palabras. |
| Solo se vectoriza la pregunta | El perfil del estudiante personaliza la respuesta en el prompt, pero no altera la búsqueda. |
| Desactivación lógica de cursos | Conserva la integridad de las recomendaciones históricas. |
| `ddl-auto=validate` | Las tablas las define el script SQL; Hibernate solo verifica que coincidan. |

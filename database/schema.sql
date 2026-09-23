-- =====================================================================
-- RutaIA - Script de creacion de la base de datos (PostgreSQL 16)
-- =====================================================================
-- Ejecucion (desde la carpeta rutaia, en PowerShell):
--   docker cp database/schema.sql rutaia-postgres:/tmp/schema.sql
--   docker exec -it rutaia-postgres psql -U rutaia -d rutaia -f /tmp/schema.sql
--
-- ADVERTENCIA: las sentencias DROP borran las tablas y sus datos.
-- Sirven para poder re-ejecutar el script durante el desarrollo.
-- =====================================================================

-- Se borran en orden inverso a las dependencias (primero las hijas)
DROP TABLE IF EXISTS calificaciones;
DROP TABLE IF EXISTS recomendacion_fuentes;
DROP TABLE IF EXISTS recomendaciones;
DROP TABLE IF EXISTS consultas;
DROP TABLE IF EXISTS cursos;
DROP TABLE IF EXISTS estudiantes;


-- ---------------------------------------------------------------------
-- ESTUDIANTES (RF 01, RF 02)
-- ---------------------------------------------------------------------
CREATE TABLE estudiantes (
    id                 BIGSERIAL    PRIMARY KEY,
    nombre_completo    VARCHAR(150) NOT NULL,
    correo             VARCHAR(150) NOT NULL,
    nivel_experiencia  VARCHAR(20)  NOT NULL,
    area_interes       VARCHAR(100) NOT NULL,
    fecha_registro     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Regla: un correo solo puede pertenecer a un estudiante
    CONSTRAINT uk_estudiantes_correo UNIQUE (correo),
    -- Regla: solo se aceptan estos tres niveles
    CONSTRAINT ck_estudiantes_nivel
        CHECK (nivel_experiencia IN ('PRINCIPIANTE', 'INTERMEDIO', 'AVANZADO')),
    CONSTRAINT ck_estudiantes_nombre CHECK (LENGTH(TRIM(nombre_completo)) > 0)
);


-- ---------------------------------------------------------------------
-- CURSOS (RF 03, RF 04)
-- ---------------------------------------------------------------------
CREATE TABLE cursos (
    id               BIGSERIAL    PRIMARY KEY,
    nombre           VARCHAR(150) NOT NULL,
    descripcion      TEXT         NOT NULL,
    categoria        VARCHAR(80)  NOT NULL,
    nivel            VARCHAR(20)  NOT NULL,
    duracion_horas   INTEGER      NOT NULL,
    activo           BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_creacion   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Regla: no se registran cursos sin nombre o descripcion (ni en blanco)
    CONSTRAINT ck_cursos_nombre      CHECK (LENGTH(TRIM(nombre)) > 0),
    CONSTRAINT ck_cursos_descripcion CHECK (LENGTH(TRIM(descripcion)) > 0),
    -- Regla: la duracion debe ser mayor que cero
    CONSTRAINT ck_cursos_duracion    CHECK (duracion_horas > 0),
    CONSTRAINT ck_cursos_nivel
        CHECK (nivel IN ('BASICO', 'INTERMEDIO', 'AVANZADO'))
);

-- Justificacion: el catalogo se filtra por categoria y nivel (RF 04)
CREATE INDEX idx_cursos_categoria_nivel ON cursos (categoria, nivel);
-- Justificacion: casi todas las consultas del catalogo piden solo activos
CREATE INDEX idx_cursos_activo ON cursos (activo);


-- ---------------------------------------------------------------------
-- CONSULTAS (RF 06, RF 07)
-- Se crea en estado PENDIENTE antes de llamar a n8n.
-- ---------------------------------------------------------------------
CREATE TABLE consultas (
    id               BIGSERIAL    PRIMARY KEY,
    estudiante_id    BIGINT       NOT NULL,
    pregunta         TEXT         NOT NULL,
    estado           VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE',
    fecha_consulta   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Regla: toda consulta pertenece a un estudiante existente
    CONSTRAINT fk_consultas_estudiante
        FOREIGN KEY (estudiante_id) REFERENCES estudiantes (id),
    CONSTRAINT ck_consultas_estado
        CHECK (estado IN ('PENDIENTE', 'RESPONDIDA', 'SIN_RESULTADOS', 'ERROR')),
    -- Regla: no se aceptan preguntas vacias
    CONSTRAINT ck_consultas_pregunta CHECK (LENGTH(TRIM(pregunta)) > 0)
);

-- Justificacion: historial de un estudiante ordenado por fecha (RF 16)
CREATE INDEX idx_consultas_estudiante_fecha
    ON consultas (estudiante_id, fecha_consulta DESC);
-- Justificacion: estadisticas por estado (RF 18)
CREATE INDEX idx_consultas_estado ON consultas (estado);


-- ---------------------------------------------------------------------
-- RECOMENDACIONES (RF 13, RF 14)
-- Relacion 1:1 con consultas (consulta_id es UNIQUE).
-- ---------------------------------------------------------------------
CREATE TABLE recomendaciones (
    id                 BIGSERIAL  PRIMARY KEY,
    consulta_id        BIGINT     NOT NULL,
    respuesta          TEXT       NOT NULL,
    fecha_generacion   TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_recomendaciones_consulta
        FOREIGN KEY (consulta_id) REFERENCES consultas (id),
    -- Una consulta tiene como maximo una recomendacion
    CONSTRAINT uk_recomendaciones_consulta UNIQUE (consulta_id)
);


-- ---------------------------------------------------------------------
-- RECOMENDACION_FUENTES (RF 13, RF 14)
-- Tabla intermedia N:M entre recomendaciones y cursos.
-- Guarda la similitud (score de Qdrant) de cada curso para esa pregunta.
-- ---------------------------------------------------------------------
CREATE TABLE recomendacion_fuentes (
    id                 BIGSERIAL         PRIMARY KEY,
    recomendacion_id   BIGINT            NOT NULL,
    curso_id           BIGINT            NOT NULL,
    similitud          DOUBLE PRECISION  NOT NULL,
    posicion           INTEGER           NOT NULL,

    CONSTRAINT fk_fuentes_recomendacion
        FOREIGN KEY (recomendacion_id) REFERENCES recomendaciones (id)
        ON DELETE CASCADE,
    -- Regla: los cursos mostrados como fuentes deben existir en la base relacional
    CONSTRAINT fk_fuentes_curso
        FOREIGN KEY (curso_id) REFERENCES cursos (id),
    -- Un mismo curso no se repite dentro de una recomendacion
    CONSTRAINT uk_fuentes_recomendacion_curso UNIQUE (recomendacion_id, curso_id),
    CONSTRAINT ck_fuentes_similitud CHECK (similitud BETWEEN -1 AND 1),
    CONSTRAINT ck_fuentes_posicion  CHECK (posicion > 0)
);

-- Justificacion: calcular el curso mas recomendado (RF 18)
CREATE INDEX idx_fuentes_curso ON recomendacion_fuentes (curso_id);


-- ---------------------------------------------------------------------
-- CALIFICACIONES (RF 17)
-- Relacion 1:1 con recomendaciones (recomendacion_id es UNIQUE).
-- ---------------------------------------------------------------------
CREATE TABLE calificaciones (
    id                 BIGSERIAL     PRIMARY KEY,
    recomendacion_id   BIGINT        NOT NULL,
    puntuacion         SMALLINT      NOT NULL,
    comentario         VARCHAR(500),
    fecha              TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_calificaciones_recomendacion
        FOREIGN KEY (recomendacion_id) REFERENCES recomendaciones (id)
        ON DELETE CASCADE,
    -- Regla: una sola calificacion por recomendacion
    CONSTRAINT uk_calificaciones_recomendacion UNIQUE (recomendacion_id),
    -- Regla: la puntuacion va de 1 a 5
    CONSTRAINT ck_calificaciones_puntuacion CHECK (puntuacion BETWEEN 1 AND 5)
);

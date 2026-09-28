-- =====================================================================
-- RutaIA - Migracion del examen: filtro semantico por nivel
-- =====================================================================
-- Agrega a "consultas" el nivel de curso solicitado como filtro.
-- NO borra datos: se puede ejecutar sobre una base con informacion.
--
-- Ejecucion (desde la carpeta rutaia, en PowerShell):
--   docker cp database/migracion_examen_nivel.sql rutaia-postgres:/tmp/migracion.sql
--   docker exec -it rutaia-postgres psql -U rutaia -d rutaia -f /tmp/migracion.sql
-- =====================================================================

-- NULL = la consulta se hizo sin filtro de nivel
ALTER TABLE consultas ADD COLUMN IF NOT EXISTS nivel_curso VARCHAR(20);

ALTER TABLE consultas DROP CONSTRAINT IF EXISTS ck_consultas_nivel_curso;
ALTER TABLE consultas ADD CONSTRAINT ck_consultas_nivel_curso
    CHECK (nivel_curso IS NULL OR nivel_curso IN ('BASICO', 'INTERMEDIO', 'AVANZADO'));

-- Verificacion
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'consultas'
ORDER BY ordinal_position;

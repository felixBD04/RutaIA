# Documento de pruebas - RutaIA

## 1. Objetivo

Verificar que RutaIA cumple los requerimientos funcionales (RF 01 a RF 18) y las reglas de negocio
del proyecto, con integración completa entre el frontend, Spring Boot, PostgreSQL, n8n, Qdrant y
OpenRouter.

## 2. Entorno de pruebas

| Componente | Versión / configuración |
|---|---|
| Backend | Java 21, Spring Boot 4.1.1, Hibernate 7 |
| Base relacional | PostgreSQL 16 (Docker) |
| Base vectorial | Qdrant 1.19 (Docker), colección `cursos`, 1536 dimensiones, distancia Cosine |
| Automatización | n8n (Docker): flujo de indexación y flujo de consulta RAG |
| Embeddings | `openai/text-embedding-3-small` vía OpenRouter |
| Modelo de lenguaje | `openai/gpt-4o-mini` vía OpenRouter, temperatura 0.2 |
| Umbral de relevancia | 0.45 (ver `calibracion_umbral.md`) |
| Catálogo | 27 cursos (26 activos, 1 inactivo) en 8 categorías |

## 3. Métodos de prueba

- **Pruebas automatizadas de API:** colección de Postman `RutaIA.postman_collection.json` con 37
  peticiones y pruebas automáticas sobre códigos HTTP y contenido de las respuestas. Se ejecuta
  completa con el *Collection Runner*.
- **Pruebas manuales desde el frontend:** recorrido completo en `http://localhost:3000`.
- **Verificación directa de datos:** consultas SQL en PostgreSQL y revisión del dashboard de Qdrant.

---

## 4. Consultas de prueba obligatorias

| ID | Consulta | Resultado esperado | Resultado obtenido | Evidencia |
|---|---|---|---|---|
| CP-01 | Quiero aprender a crear páginas web | RESPONDIDA con cursos de desarrollo web | **RESPONDIDA.** Fuentes: Desarrollo Web con HTML, CSS y JavaScript (0.6419), Diseño Web Responsive (0.5116), JavaScript Moderno (0.4667). Ruta: empezar por HTML/CSS/JS y continuar con Diseño Responsive. | |
| CP-02 | Necesito aprender Java para trabajar con Spring Boot | RESPONDIDA con cursos de Java y Spring | **RESPONDIDA.** 4 fuentes entre 0.5904 y 0.6463. Ruta: Java desde Cero, luego APIs REST con Spring Boot, luego Spring Boot Avanzado. | |
| CP-03 | Me interesa analizar datos y construir dashboards | RESPONDIDA con cursos de análisis de datos | **RESPONDIDA.** Power BI (0.6448), Python y Pandas (0.5180), Excel (0.4861). Para un principiante recomienda empezar por Excel. | |
| CP-04 | Quiero automatizar procesos empresariales | RESPONDIDA con cursos de automatización | **RESPONDIDA.** RPA (0.6661), n8n (0.6068), Python (0.5558). | |
| CP-05 | ¿Qué puedo estudiar para trabajar con inteligencia artificial? | RESPONDIDA con cursos de IA | **RESPONDIDA.** 4 cursos de IA entre 0.5594 y 0.6609. Para un principiante recomienda Introducción a la IA. | |
| CP-06 | Quiero aprender a proteger aplicaciones web | RESPONDIDA con cursos de seguridad | **RESPONDIDA.** 5 fuentes; el modelo recomendó solo Fundamentos de Ciberseguridad y OWASP Top 10, descartando los dos cursos de desarrollo web que pasaron el umbral por coincidencia de términos (ver sección 7). | |
| CP-07 | Necesito desplegar aplicaciones usando contenedores | RESPONDIDA con cursos de contenedores | **RESPONDIDA.** Docker (0.6699) y Kubernetes (0.5656). | |
| CP-08 | Quiero aprender cocina italiana | SIN_RESULTADOS, sin llamar al LLM | **SIN_RESULTADOS.** Máxima similitud 0.3133, por debajo del umbral. El flujo no invocó al modelo de lenguaje. | |
| CP-09 | Pregunta vacía | Rechazo sin enviar a n8n | **400 Bad Request** con error en el campo `pregunta`. No se creó ninguna consulta ni se llamó a n8n. | |
| CP-10 | Consulta con estudiante inexistente | Rechazo sin crear la consulta | **404 Not Found:** "No existe un estudiante con id 999999". No se creó ninguna consulta. | |

---

## 5. Reglas de negocio

| ID | Regla | Prueba realizada | Resultado esperado | Resultado obtenido | Evidencia |
|---|---|---|---|---|---|
| RN-01 | Un correo solo puede pertenecer a un estudiante | Registrar dos veces el mismo correo, también con mayúsculas distintas | 409 Conflict | 409, "Ya existe un estudiante registrado con el correo..." | |
| RN-02 | Solo los cursos activos pueden recomendarse | Desactivar un curso sin reindexar y consultar | La consulta no muestra el curso | ERROR: Spring Boot detecta que Qdrant devolvió un curso inactivo y no lo guarda como fuente | |
| RN-03 | No se registran cursos sin nombre o descripción | Crear un curso con nombre y descripción vacíos | 400 | 400 con errores en `nombre` y `descripcion` | |
| RN-04 | La duración debe ser mayor que cero | Crear un curso con `duracionHoras: 0` | 400 | 400 en la API; además la base rechaza el valor por la restricción `ck_cursos_duracion` | |
| RN-05 | Toda consulta pertenece a un estudiante existente | CP-10 | 404 | 404 | |
| RN-06 | Las preguntas vacías no se envían a n8n | CP-09 | 400 | 400 | |
| RN-07 | Las fuentes deben existir en la base relacional | Validación en `ConsultaService` de cada `cursoId` recibido de n8n | Fuentes inexistentes provocan ERROR | Verificado con RN-02 (misma validación) | |
| RN-08 | La respuesta no incluye cursos diferentes a los recuperados | Revisión del texto de CP-01 a CP-07 | Solo cursos del contexto | Todos los cursos mencionados pertenecen a las fuentes recuperadas | |
| RN-09 | Sin resultados relevantes: estado Sin resultados | CP-08 | SIN_RESULTADOS | SIN_RESULTADOS | |
| RN-10 | Si n8n, Qdrant u OpenRouter fallan: estado Error | Ver sección 6 | ERROR | ERROR | |
| RN-11 | Calificación entre 1 y 5 | Calificar con 0 y con 6 | 400 | 400 | |
| RN-12 | Una sola calificación por recomendación | Calificar dos veces la misma recomendación | 409 | 409 | |
| RN-13 | Claves fuera del código y del repositorio | Revisar `git status` y el repositorio en GitHub | `.env` no publicado | `.env` excluido por `.gitignore`; la API key de OpenRouter vive cifrada en las credenciales de n8n | |

---

## 6. Resiliencia y estados de la consulta

| ID | Escenario | Procedimiento | Resultado esperado | Resultado obtenido | Evidencia |
|---|---|---|---|---|---|
| RE-01 | Registro previo a la llamada a n8n | Consultar `SELECT id, estado FROM consultas` tras una consulta fallida | La consulta existe en la base | La consulta quedó registrada: primero PENDIENTE, luego ERROR | |
| RE-02 | n8n no disponible | `docker compose stop n8n` y enviar una consulta | ERROR con mensaje claro | ERROR: "El servicio de recomendaciones no está disponible en este momento..." | |
| RE-03 | Qdrant desactualizado | Desactivar un curso sin reindexar | ERROR y rollback | ERROR indicando que se debe reindexar; no quedó ninguna recomendación a medias | |
| RE-04 | Backend apagado | Detener Spring Boot y usar el frontend | Mensaje de conexión, sin bloqueo | "No se pudo conectar con RutaIA. Verifica que el backend esté en ejecución..." | |
| RE-05 | Reintento tras recuperación | Levantar n8n y usar "Intentar de nuevo" en el frontend | RESPONDIDA | RESPONDIDA | |

---

## 7. Base vectorial (Qdrant)

| ID | Prueba | Resultado obtenido | Evidencia |
|---|---|---|---|
| VQ-01 | Creación de la colección | Colección `cursos` con vectores de tamaño 1536 y distancia Cosine | |
| VQ-02 | Inserción | 27 puntos; cada uno con `curso_id`, nombre, descripción, categoría, nivel, duración y `activo` en el payload | |
| VQ-03 | Ausencia de duplicados | Tras ejecutar la indexación dos veces, la colección sigue con 27 puntos (upsert por id) | |
| VQ-04 | Consulta semántica | "Find Similar" sobre el curso 1 devuelve primero los cursos de desarrollo web | |
| VQ-05 | Interpretación del score | Relevantes: 0.47 a 0.67. No relacionados: 0.28 a 0.41. Fuera del dominio: máximo 0.31 | |

**Caso de análisis (CP-06):** en "proteger aplicaciones web", dos cursos de desarrollo web obtuvieron
cerca de 0.48 por la coincidencia del término "aplicaciones web", prácticamente el mismo score que un
curso relevante (Fundamentos de Ciberseguridad, 0.4831). Ningún umbral fijo puede separarlos. La
segunda capa de filtrado (la regla del prompt que pide recomendar solo los cursos que respondan a la
necesidad) hizo que el modelo los descartara. El frontend los muestra aparte, en "Otros cursos que se
evaluaron", gracias al campo `mencionado`.

---

## 8. Frontend

| ID | Funcionalidad | Resultado obtenido | Evidencia |
|---|---|---|---|
| FE-01 | Registro de estudiante con validaciones y error de correo duplicado junto al campo | Correcto | |
| FE-02 | Catálogo con filtros por categoría y nivel, compartibles por URL | Correcto | |
| FE-03 | Formulario de consulta con mensajes de espera | Correcto | |
| FE-04 | Resultado con ruta sugerida, recomendación, fuentes y similitudes | Correcto | |
| FE-05 | Pantallas de Sin resultados y de Error con acciones para continuar | Correcto | |
| FE-06 | Historial con estado, cursos recomendados y calificación | Correcto | |
| FE-07 | Calificación de 1 a 5 estrellas con comentario opcional | Correcto | |
| FE-08 | Estadísticas | Correcto | |
| FE-09 | Administración de cursos (crear, editar, desactivar, activar) | Correcto | |
| FE-10 | Diseño adaptable a celulares | Pendiente de verificar con las herramientas de desarrollador del navegador | |

---

## 9. Ejecución de la colección de Postman

| Carpeta | Peticiones | Pruebas aprobadas | Pruebas fallidas |
|---|---|---|---|
| 1. Estudiantes | 7 | | |
| 2. Cursos y catálogo | 13 | | |
| 3. Consultas obligatorias | 10 | | |
| 4. Historial y calificaciones | 6 | | |
| 5. Estadísticas | 1 | | |
| **Total** | **37** | | |

Evidencia: captura del resumen del *Collection Runner*.

---

## 10. Errores conocidos y limitaciones

- **Coincidencia de términos en la búsqueda semántica:** cursos que comparten palabras con la pregunta
  pueden superar el umbral sin ser relevantes (CP-06). Se mitiga con la regla del prompt y con el campo
  `mencionado`.
- **Sincronización manual con Qdrant:** después de crear, editar o desactivar cursos se debe ejecutar el
  flujo de indexación. Si no se hace, las consultas que recuperen un curso desactualizado terminan en
  ERROR (comportamiento seguro, pero requiere reindexar).
- **Menos de tres fuentes en algunos casos:** cuando solo dos cursos superan el umbral (CP-07), se
  priorizan la relevancia (RF 11) sobre la cantidad mínima de cursos (RF 10).
- **Sin autenticación:** el estudiante activo se elige en el navegador y las rutas administrativas no
  están protegidas. No es obligatorio en el proyecto.

# Calibración del umbral de relevancia (RF 11)

## Contexto

La búsqueda semántica en Qdrant usa distancia **coseno** sobre embeddings del modelo
`openai/text-embedding-3-small` (1536 dimensiones). Qdrant recupera siempre los **5 cursos
activos más similares** a la pregunta, y luego el flujo de n8n descarta los que no superan un
**umbral mínimo de similitud**. Solo los cursos que lo superan se envían como contexto al modelo
de lenguaje.

## Metodología

Se ejecutaron las 8 consultas obligatorias con contenido contra el catálogo de 27 cursos y se
clasificó manualmente cada curso recuperado como:

- **Relevante:** responde a la necesidad del estudiante.
- **Parcial:** relacionado de forma secundaria.
- **No relacionado:** aparece por similitud de fondo o por coincidencia de palabras, pero no
  responde a la pregunta.

## Resultados

| Consulta | Curso | Similitud | Clasificación |
|---|---|---|---|
| Quiero aprender a crear páginas web | Desarrollo Web con HTML, CSS y JavaScript | 0.6419 | Relevante |
| | Diseño Web Responsive y Accesibilidad | 0.5116 | Relevante |
| | JavaScript Moderno y Consumo de APIs | 0.4667 | Relevante |
| | Seguridad en Aplicaciones Web: OWASP Top 10 | 0.4104 | No relacionado |
| | Desarrollo de APIs REST con Spring Boot | 0.3874 | No relacionado |
| Necesito aprender Java para trabajar con Spring Boot | Desarrollo de APIs REST con Spring Boot | 0.6463 | Relevante |
| | Programación en Java desde Cero | 0.6435 | Relevante |
| | Spring Boot Avanzado: Seguridad, Pruebas y Microservicios | 0.6140 | Relevante |
| | Programación Orientada a Objetos con Java | 0.5904 | Relevante |
| | Desarrollo Web con HTML, CSS y JavaScript | 0.4108 | No relacionado |
| Me interesa analizar datos y construir dashboards | Visualización de Datos y Dashboards con Power BI | 0.6448 | Relevante |
| | Análisis de Datos con Python y Pandas | 0.5180 | Relevante |
| | Excel para Análisis de Datos | 0.4861 | Relevante |
| | Bases de Datos NoSQL con MongoDB | 0.3898 | No relacionado |
| | Aplicaciones con Modelos de Lenguaje y RAG | 0.3869 | No relacionado |
| Quiero automatizar procesos empresariales | RPA: Automatización Robótica de Procesos Empresariales | 0.6661 | Relevante |
| | Automatización de Procesos con n8n | 0.6068 | Relevante |
| | Automatización de Tareas con Python | 0.5558 | Relevante |
| | Aplicaciones con Modelos de Lenguaje y RAG | 0.3668 | No relacionado |
| | Spring Boot Avanzado: Seguridad, Pruebas y Microservicios | 0.3612 | No relacionado |
| ¿Qué puedo estudiar para trabajar con inteligencia artificial? | Introducción a la Inteligencia Artificial | 0.6609 | Relevante |
| | Aplicaciones con Modelos de Lenguaje y RAG | 0.5903 | Relevante |
| | Deep Learning y Redes Neuronales | 0.5819 | Relevante |
| | Machine Learning con Python y Scikit-learn | 0.5594 | Relevante |
| | RPA: Automatización Robótica de Procesos Empresariales | 0.3973 | No relacionado |
| Quiero aprender a proteger aplicaciones web | Seguridad en Aplicaciones Web: OWASP Top 10 | 0.6320 | Relevante |
| | Spring Boot Avanzado: Seguridad, Pruebas y Microservicios | 0.5462 | Parcial (incluye Spring Security) |
| | Fundamentos de Ciberseguridad | 0.4831 | Relevante |
| | JavaScript Moderno y Consumo de APIs | 0.4807 | No relacionado (coincidencia "aplicaciones web") |
| | Desarrollo Web con HTML, CSS y JavaScript | 0.4794 | No relacionado (coincidencia "aplicaciones web") |
| Necesito desplegar aplicaciones usando contenedores | Contenedores con Docker | 0.6699 | Relevante |
| | Kubernetes y Despliegue en la Nube | 0.5656 | Relevante |
| | Spring Boot Avanzado: Seguridad, Pruebas y Microservicios | 0.3992 | No relacionado |
| | Programación Orientada a Objetos con Java | 0.3673 | No relacionado |
| | Automatización de Procesos con n8n | 0.3651 | No relacionado |
| Quiero aprender cocina italiana | JavaScript Moderno y Consumo de APIs | 0.3133 | No relacionado |
| | Desarrollo Web con HTML, CSS y JavaScript | 0.3097 | No relacionado |
| | Aplicaciones con Modelos de Lenguaje y RAG | 0.2846 | No relacionado |
| | Contenedores con Docker | 0.2832 | No relacionado |
| | Deep Learning y Redes Neuronales | 0.2761 | No relacionado |

**Observaciones:**

- Los cursos relevantes obtuvieron similitudes entre **0.4667 y 0.6699**.
- Los cursos no relacionados se ubicaron, en general, entre **0.2761 y 0.4108**. Este rango
  corresponde a la similitud "de fondo" entre dos textos cualesquiera sobre tecnología en español.
- La consulta fuera del dominio (cocina) no superó **0.3133** en ningún curso.
- **Excepción:** en "proteger aplicaciones web", dos cursos de desarrollo web obtuvieron ~0.48 por
  la coincidencia del término "aplicaciones web". Su score es prácticamente igual al de un curso
  relevante (Fundamentos de Ciberseguridad, 0.4831), por lo que ningún umbral fijo puede separarlos.

## Decisión

**Umbral = 0.45**

- Se ubica por encima de casi todos los cursos no relacionados (máximo general 0.4108) y por debajo
  del curso relevante con menor score (0.4667).
- Ninguna consulta legítima quedó sin resultados.
- Deja un margen de 0.14 frente a la consulta fuera del dominio, que finaliza como **Sin
  resultados** sin llamar al modelo de lenguaje.

**Valores descartados:**

- **0.35:** permitía el paso de cursos no relacionados. En la consulta de contenedores el modelo
  terminó recomendando "Programación Orientada a Objetos con Java".
- **0.49:** eliminaría los cursos de desarrollo web en la consulta de seguridad, pero también
  cursos relevantes como "Excel para Análisis de Datos" (0.4861) y "Fundamentos de Ciberseguridad"
  (0.4831).

## Doble capa de filtrado

Para los casos que el umbral no puede resolver, el prompt del modelo incluye la regla:
*"Recomienda solo los cursos que realmente respondan a la necesidad; no es obligatorio mencionarlos
todos"*. En la consulta de seguridad, el modelo recomendó únicamente "Fundamentos de
Ciberseguridad" y "Seguridad en Aplicaciones Web: OWASP Top 10", ignorando los cursos de desarrollo
web presentes en el contexto.

- **Capa 1 (umbral):** elimina el ruido grueso y las consultas fuera del dominio.
- **Capa 2 (prompt):** elimina el ruido fino por coincidencia de palabras.

## Relación con RF 10 (entre tres y cinco cursos)

Qdrant recupera siempre 5 candidatos. Cuando el umbral deja menos de 3 (por ejemplo, 2 en la
consulta de contenedores), se prioriza la **relevancia (RF 11)** sobre la cantidad: es preferible
recomendar 2 cursos correctos que incluir un tercero irrelevante en el contexto del modelo.

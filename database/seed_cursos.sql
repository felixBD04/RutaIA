-- =====================================================================
-- RutaIA - Carga inicial del catalogo de cursos
-- =====================================================================
-- Ejecucion (desde la carpeta rutaia, en PowerShell):
--   docker cp database/seed_cursos.sql rutaia-postgres:/tmp/seed_cursos.sql
--   docker exec -it rutaia-postgres psql -U rutaia -d rutaia -f /tmp/seed_cursos.sql
--
-- ADVERTENCIA: TRUNCATE borra los cursos existentes (y, en cascada, las
-- fuentes de recomendaciones que los usen) y reinicia los ids en 1.
-- Usalo solo durante el desarrollo.
-- =====================================================================

TRUNCATE TABLE cursos RESTART IDENTITY CASCADE;

INSERT INTO cursos (nombre, descripcion, categoria, nivel, duracion_horas, activo) VALUES

-- ----------------------------- DESARROLLO WEB -----------------------------
('Desarrollo Web con HTML, CSS y JavaScript',
 'Aprende a crear páginas web desde cero. Construirás la estructura de un sitio con HTML5, le darás diseño con CSS3, Flexbox y Grid, y agregarás interactividad con JavaScript manipulando el DOM. Al finalizar tendrás tu propio sitio web publicado. Ideal para quienes nunca han programado y quieren dar su primer paso en el desarrollo web y frontend.',
 'Desarrollo Web', 'BASICO', 40, TRUE),

('JavaScript Moderno y Consumo de APIs',
 'Profundiza en JavaScript moderno (ES6+): funciones flecha, módulos, promesas, async/await y manejo de errores. Aprenderás a consumir APIs REST con Fetch, mostrar datos dinámicos en una página web y construir aplicaciones web interactivas sin frameworks. Recomendado para quienes ya conocen HTML y CSS y quieren convertirse en desarrolladores frontend.',
 'Desarrollo Web', 'INTERMEDIO', 45, TRUE),

('Diseño Web Responsive y Accesibilidad',
 'Diseña sitios web que se vean bien en celulares, tablets y computadores. Trabajarás con media queries, diseño mobile first, unidades relativas, tipografía y buenas prácticas de experiencia de usuario (UX). También aprenderás accesibilidad web para que tus páginas puedan ser usadas por todas las personas. Ideal para desarrolladores frontend y diseñadores web.',
 'Desarrollo Web', 'INTERMEDIO', 30, TRUE),

-- ------------------------------ PROGRAMACION ------------------------------
('Fundamentos de Programación con Python',
 'Primer curso de programación para principiantes absolutos. Aprenderás lógica de programación, variables, condicionales, ciclos, funciones y estructuras de datos como listas y diccionarios usando Python, uno de los lenguajes más fáciles de aprender. Resolverás ejercicios prácticos que te prepararán para análisis de datos, automatización o inteligencia artificial.',
 'Programación', 'BASICO', 40, TRUE),

('Programación en Java desde Cero',
 'Aprende a programar en Java, uno de los lenguajes más usados en empresas. Verás la sintaxis del lenguaje, tipos de datos, estructuras de control, métodos, arreglos y colecciones, y usarás un IDE profesional como IntelliJ IDEA. Es el punto de partida recomendado para quienes quieren llegar a desarrollar aplicaciones backend con Spring Boot.',
 'Programación', 'BASICO', 50, TRUE),

('Programación Orientada a Objetos con Java',
 'Domina la programación orientada a objetos en Java: clases, objetos, encapsulamiento, herencia, polimorfismo, interfaces y manejo de excepciones. También aprenderás principios SOLID, colecciones genéricas, streams y lambdas. Es un requisito fundamental antes de trabajar con frameworks como Spring Boot y para desarrollar software empresarial mantenible.',
 'Programación', 'INTERMEDIO', 45, TRUE),

('Desarrollo de APIs REST con Spring Boot',
 'Construye APIs REST profesionales con Java y Spring Boot. Aprenderás a crear controladores, servicios y repositorios, conectarte a bases de datos con Spring Data JPA, validar datos con Jakarta Validation, manejar errores globalmente, usar DTO y documentar endpoints con Swagger. Ideal para quienes ya saben Java y quieren trabajar como desarrolladores backend.',
 'Programación', 'INTERMEDIO', 60, TRUE),

('Spring Boot Avanzado: Seguridad, Pruebas y Microservicios',
 'Lleva tus aplicaciones Spring Boot a nivel profesional. Implementarás autenticación y autorización con Spring Security y JWT, pruebas unitarias y de integración con JUnit y Mockito, y arquitectura de microservicios con comunicación entre servicios. Dirigido a desarrolladores backend Java que ya construyen APIs y quieren prepararse para proyectos empresariales.',
 'Programación', 'AVANZADO', 60, TRUE),

-- ----------------------------- BASES DE DATOS -----------------------------
('Fundamentos de Bases de Datos y SQL',
 'Aprende a diseñar y consultar bases de datos relacionales. Verás el modelo entidad-relación, normalización, claves primarias y foráneas, y el lenguaje SQL: SELECT, INSERT, UPDATE, DELETE, JOIN, agrupaciones y subconsultas usando PostgreSQL y MySQL. Indispensable para cualquier desarrollador, analista de datos o persona que trabaje con información.',
 'Bases de Datos', 'BASICO', 35, TRUE),

('PostgreSQL Avanzado: Modelado y Optimización',
 'Profundiza en PostgreSQL para aplicaciones exigentes. Aprenderás índices y planes de ejecución, optimización de consultas lentas, transacciones y niveles de aislamiento, funciones, procedimientos almacenados, triggers, vistas y respaldos. Dirigido a desarrolladores y administradores de bases de datos que necesitan sistemas rápidos y confiables.',
 'Bases de Datos', 'AVANZADO', 40, TRUE),

('Bases de Datos NoSQL con MongoDB',
 'Conoce el mundo de las bases de datos no relacionales. Aprenderás a modelar documentos JSON en MongoDB, hacer consultas, agregaciones e índices, y cuándo conviene usar NoSQL en lugar de SQL. También verás otros tipos de bases como las de clave-valor y las bases de datos vectoriales usadas en inteligencia artificial.',
 'Bases de Datos', 'INTERMEDIO', 30, TRUE),

-- ---------------------------- ANALISIS DE DATOS ----------------------------
('Excel para Análisis de Datos',
 'Aprende a analizar información usando Excel de forma profesional. Trabajarás con fórmulas, funciones de búsqueda, limpieza de datos, tablas dinámicas, gráficos y formato condicional para crear reportes. Ideal para quienes empiezan en el análisis de datos o necesitan tomar decisiones con información en su trabajo sin saber programar.',
 'Análisis de Datos', 'BASICO', 25, TRUE),

('Análisis de Datos con Python y Pandas',
 'Analiza grandes volúmenes de datos con Python. Aprenderás a cargar, limpiar y transformar datos con Pandas, hacer cálculos estadísticos, agrupar información y crear gráficos con Matplotlib y Seaborn en Jupyter Notebook. Recomendado para quienes quieren trabajar como analistas de datos o dar el paso hacia la ciencia de datos.',
 'Análisis de Datos', 'INTERMEDIO', 45, TRUE),

('Visualización de Datos y Dashboards con Power BI',
 'Convierte datos en decisiones construyendo dashboards interactivos con Power BI. Aprenderás a conectar distintas fuentes de datos, transformarlas con Power Query, crear modelos de datos, medidas con DAX e indicadores clave (KPI), y a diseñar reportes visuales claros para la gerencia. Ideal para analistas de datos y profesionales de inteligencia de negocios.',
 'Análisis de Datos', 'INTERMEDIO', 35, TRUE),

-- ------------------------- INTELIGENCIA ARTIFICIAL -------------------------
('Introducción a la Inteligencia Artificial',
 'Entiende qué es la inteligencia artificial y cómo está transformando las industrias. Conocerás los conceptos de machine learning, deep learning, procesamiento de lenguaje natural e IA generativa, sus aplicaciones reales, sus limitaciones y sus implicaciones éticas. No requiere programar. Es el punto de partida para quienes quieren trabajar con inteligencia artificial.',
 'Inteligencia Artificial', 'BASICO', 30, TRUE),

('Machine Learning con Python y Scikit-learn',
 'Aprende a construir modelos de aprendizaje automático con Python. Verás regresión, clasificación, árboles de decisión, clustering, preparación de datos, entrenamiento, evaluación de modelos y cómo evitar el sobreajuste usando Scikit-learn. Recomendado para quienes ya programan en Python y quieren desarrollar soluciones de inteligencia artificial basadas en datos.',
 'Inteligencia Artificial', 'INTERMEDIO', 60, TRUE),

('Deep Learning y Redes Neuronales',
 'Profundiza en el aprendizaje profundo: redes neuronales, redes convolucionales para visión por computador, redes recurrentes y transformers. Entrenarás modelos con TensorFlow y PyTorch, usarás GPUs y aplicarás transfer learning. Dirigido a personas con bases de machine learning que quieren especializarse como ingenieros de inteligencia artificial.',
 'Inteligencia Artificial', 'AVANZADO', 60, TRUE),

('Aplicaciones con Modelos de Lenguaje y RAG',
 'Desarrolla aplicaciones de IA generativa con modelos de lenguaje (LLM). Aprenderás ingeniería de prompts, embeddings, búsqueda semántica, bases de datos vectoriales como Qdrant y la arquitectura RAG (generación aumentada por recuperación) para construir asistentes y chatbots que responden con información propia. Ideal para desarrolladores que quieren crear productos con inteligencia artificial.',
 'Inteligencia Artificial', 'AVANZADO', 50, TRUE),

-- ------------------------------ AUTOMATIZACION -----------------------------
('Automatización de Procesos con n8n',
 'Automatiza tareas repetitivas sin necesidad de ser programador experto. Con n8n crearás flujos de trabajo que conectan aplicaciones como correo, hojas de cálculo, bases de datos y APIs, usando webhooks, disparadores programados y nodos de inteligencia artificial. Ideal para quienes quieren automatizar procesos empresariales y ahorrar tiempo en su trabajo.',
 'Automatización', 'BASICO', 30, TRUE),

('Automatización de Tareas con Python',
 'Usa Python para automatizar tareas del día a día: procesar archivos de Excel y CSV, renombrar y organizar archivos, enviar correos automáticos, extraer información de páginas web con web scraping y programar la ejecución de scripts. Recomendado para quienes ya conocen lo básico de Python y quieren ser más productivos automatizando su trabajo.',
 'Automatización', 'INTERMEDIO', 35, TRUE),

('RPA: Automatización Robótica de Procesos Empresariales',
 'Aprende a automatizar procesos de negocio con robots de software (RPA). Identificarás qué procesos empresariales conviene automatizar, diseñarás flujos con herramientas como UiPath y Power Automate, e integrarás sistemas administrativos, contables y de atención al cliente. Dirigido a profesionales que buscan transformar digitalmente las operaciones de una empresa.',
 'Automatización', 'INTERMEDIO', 40, TRUE),

-- ------------------------- SEGURIDAD INFORMATICA --------------------------
('Fundamentos de Ciberseguridad',
 'Conoce los principios de la seguridad informática: confidencialidad, integridad y disponibilidad. Aprenderás sobre amenazas comunes como malware, phishing e ingeniería social, gestión de contraseñas, cifrado, seguridad en redes y buenas prácticas para proteger la información de personas y empresas. Punto de partida para quienes quieren trabajar en ciberseguridad.',
 'Seguridad Informática', 'BASICO', 30, TRUE),

('Seguridad en Aplicaciones Web: OWASP Top 10',
 'Aprende a proteger aplicaciones web contra los ataques más comunes. Estudiarás las vulnerabilidades del OWASP Top 10 como inyección SQL, XSS, CSRF, control de acceso roto y exposición de datos sensibles, y cómo prevenirlas con validación, autenticación segura, manejo de sesiones, HTTPS y cabeceras de seguridad. Ideal para desarrolladores que quieren crear software seguro.',
 'Seguridad Informática', 'INTERMEDIO', 40, TRUE),

-- ----------------------------- DEVOPS Y CLOUD ------------------------------
('Git y GitHub para Trabajo Colaborativo',
 'Aprende a controlar las versiones de tu código con Git y a colaborar en equipo con GitHub. Verás commits, ramas, fusiones, resolución de conflictos, pull requests y buenas prácticas de trabajo en proyectos de software. Una herramienta indispensable para cualquier desarrollador, sin importar el lenguaje que use.',
 'DevOps y Cloud', 'BASICO', 20, TRUE),

('Contenedores con Docker',
 'Aprende a empaquetar y desplegar aplicaciones usando contenedores Docker. Crearás imágenes con Dockerfile, gestionarás contenedores, redes y volúmenes, y levantarás aplicaciones completas con varios servicios usando Docker Compose. Recomendado para desarrolladores que quieren desplegar sus aplicaciones de forma consistente en cualquier servidor o en la nube.',
 'DevOps y Cloud', 'INTERMEDIO', 35, TRUE),

('Kubernetes y Despliegue en la Nube',
 'Orquesta contenedores a gran escala con Kubernetes. Aprenderás pods, deployments, servicios, escalado automático y actualizaciones sin caídas, además de pipelines de integración y despliegue continuo (CI/CD) para publicar aplicaciones en la nube con AWS, Azure o Google Cloud. Dirigido a quienes ya dominan Docker y quieren especializarse en DevOps.',
 'DevOps y Cloud', 'AVANZADO', 45, TRUE),

-- -------------------- CURSO INACTIVO (para pruebas) ------------------------
-- Este curso NO debe aparecer en el catalogo ni en las recomendaciones.
('Desarrollo Web con jQuery (versión antigua)',
 'Curso descontinuado sobre la creación de páginas web interactivas usando la librería jQuery: selectores, eventos, animaciones y peticiones AJAX. Fue reemplazado por el curso de JavaScript Moderno y Consumo de APIs.',
 'Desarrollo Web', 'BASICO', 20, FALSE);


-- Verificacion rapida
SELECT categoria, COUNT(*) AS cursos, SUM(CASE WHEN activo THEN 1 ELSE 0 END) AS activos
FROM cursos
GROUP BY categoria
ORDER BY categoria;

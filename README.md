# RutaIA

Plataforma web que recomienda cursos a partir de una necesidad escrita en lenguaje natural, usando busqueda semantica y RAG (Retrieval-Augmented Generation).

> README en construccion. Se completara al final del proyecto con todas las secciones exigidas.

## Integrantes

- (por completar)

## Tecnologias

- Frontend: HTML, CSS, JavaScript
- Backend: Java 21, Spring Boot, Gradle
- Base de datos relacional: PostgreSQL 16
- Base de datos vectorial: Qdrant
- Automatizacion: n8n
- IA (embeddings y LLM): OpenRouter

## Estructura del repositorio

```
rutaia/
├── backend/          API REST con Spring Boot
├── frontend/         Interfaz web
├── database/         Script SQL y diagrama entidad-relacion
├── n8n/workflows/    Flujos exportados de n8n (JSON)
├── docs/             Diagramas, documento de pruebas, presentacion
├── docker-compose.yml
└── .env.example
```

## Levantar la infraestructura

```
copy .env.example .env      (y editar las claves)
docker compose up -d
docker compose ps
```

| Servicio   | URL                              |
|------------|----------------------------------|
| PostgreSQL | localhost:5432                   |
| Qdrant     | http://localhost:6333/dashboard  |
| n8n        | http://localhost:5678            |

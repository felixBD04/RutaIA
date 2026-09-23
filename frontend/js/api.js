// =====================================================================
// Cliente de la API REST de RutaIA.
// Todas las paginas usan estas funciones: ninguna llama a fetch directamente.
// El frontend SOLO habla con Spring Boot (nunca con n8n, Qdrant o PostgreSQL).
// =====================================================================

import { API_URL } from './config.js';

/** Error de la API con el mismo formato que devuelve el GlobalExceptionHandler. */
export class ApiError extends Error {
  constructor(mensaje, estado, errores = null) {
    super(mensaje);
    this.estado = estado;     // codigo HTTP (0 si no hubo conexion)
    this.errores = errores;   // errores por campo, cuando fallan validaciones
  }
}

async function peticion(ruta, { metodo = 'GET', cuerpo } = {}) {
  let respuesta;
  try {
    respuesta = await fetch(API_URL + ruta, {
      method: metodo,
      headers: cuerpo ? { 'Content-Type': 'application/json' } : {},
      body: cuerpo ? JSON.stringify(cuerpo) : undefined,
    });
  } catch {
    // fetch solo falla aqui si no hay conexion (backend apagado, CORS, red)
    throw new ApiError(
      'No se pudo conectar con RutaIA. Verifica que el backend esté en ejecución en el puerto 8080.',
      0
    );
  }

  const texto = await respuesta.text();
  let datos = null;
  if (texto) {
    try {
      datos = JSON.parse(texto);
    } catch {
      datos = null;
    }
  }

  if (!respuesta.ok) {
    throw new ApiError(
      datos?.mensaje ?? `La solicitud falló con el código ${respuesta.status}.`,
      respuesta.status,
      datos?.errores ?? null
    );
  }
  return datos;
}

function conParametros(ruta, parametros) {
  const limpios = Object.entries(parametros).filter(([, v]) => v !== undefined && v !== null && v !== '');
  return limpios.length ? `${ruta}?${new URLSearchParams(limpios)}` : ruta;
}

export const api = {
  estudiantes: {
    listar: () => peticion('/estudiantes'),
    obtener: (id) => peticion(`/estudiantes/${id}`),
    registrar: (datos) => peticion('/estudiantes', { metodo: 'POST', cuerpo: datos }),
    historial: (id) => peticion(`/estudiantes/${id}/historial`),
  },
  cursos: {
    catalogo: (filtros = {}) => peticion(conParametros('/cursos', filtros)),
    categorias: () => peticion('/cursos/categorias'),
    detalle: (id) => peticion(`/cursos/${id}`),
  },
  admin: {
    listar: () => peticion('/admin/cursos'),
    registrar: (datos) => peticion('/admin/cursos', { metodo: 'POST', cuerpo: datos }),
    actualizar: (id, datos) => peticion(`/admin/cursos/${id}`, { metodo: 'PUT', cuerpo: datos }),
    desactivar: (id) => peticion(`/admin/cursos/${id}/desactivar`, { metodo: 'PATCH' }),
    activar: (id) => peticion(`/admin/cursos/${id}/activar`, { metodo: 'PATCH' }),
  },
  consultas: {
    crear: (datos) => peticion('/consultas', { metodo: 'POST', cuerpo: datos }),
    detalle: (id) => peticion(`/consultas/${id}`),
  },
  calificar: (recomendacionId, datos) =>
    peticion(`/recomendaciones/${recomendacionId}/calificacion`, { metodo: 'POST', cuerpo: datos }),
  estadisticas: () => peticion('/estadisticas'),
};

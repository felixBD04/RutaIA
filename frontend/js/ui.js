// =====================================================================
// Utilidades de interfaz compartidas por todas las paginas.
// =====================================================================

const CLAVE_ESTUDIANTE = 'rutaia.estudiante';

const PAGINAS = [
  { id: 'inicio', href: 'index.html', texto: 'Inicio' },
  { id: 'consulta', href: 'consulta.html', texto: 'Preguntar' },
  { id: 'catalogo', href: 'catalogo.html', texto: 'Catálogo' },
  { id: 'historial', href: 'historial.html', texto: 'Mi historial' },
  { id: 'estadisticas', href: 'estadisticas.html', texto: 'Estadísticas' },
  { id: 'admin', href: 'admin.html', texto: 'Administrar cursos' },
];

export const NIVELES_ESTUDIANTE = { PRINCIPIANTE: 'Principiante', INTERMEDIO: 'Intermedio', AVANZADO: 'Avanzado' };
export const NIVELES_CURSO = { BASICO: 'Básico', INTERMEDIO: 'Intermedio', AVANZADO: 'Avanzado' };
export const ESTADOS = {
  PENDIENTE: 'Pendiente',
  RESPONDIDA: 'Respondida',
  SIN_RESULTADOS: 'Sin resultados',
  ERROR: 'Error',
};

// ---------------------------- Estudiante activo ----------------------------
// No hay autenticacion: se recuerda en el navegador que estudiante esta usando la app.

export function obtenerEstudianteActivo() {
  try {
    return JSON.parse(localStorage.getItem(CLAVE_ESTUDIANTE));
  } catch {
    return null;
  }
}

export function guardarEstudianteActivo(estudiante) {
  localStorage.setItem(CLAVE_ESTUDIANTE, JSON.stringify({
    id: estudiante.id,
    nombreCompleto: estudiante.nombreCompleto,
    nivelExperiencia: estudiante.nivelExperiencia,
    areaInteres: estudiante.areaInteres,
  }));
}

export function cerrarSesion() {
  localStorage.removeItem(CLAVE_ESTUDIANTE);
}

// -------------------------------- Encabezado --------------------------------

export function pintarEncabezado(paginaActual) {
  const estudiante = obtenerEstudianteActivo();
  const enlaces = PAGINAS.map(p =>
    `<a href="${p.href}"${p.id === paginaActual ? ' aria-current="page"' : ''}>${p.texto}</a>`
  ).join('');

  const usuario = estudiante
    ? `<p class="usuario-activo">Consultas como <strong>${escaparHtml(estudiante.nombreCompleto)}</strong>
         <a href="index.html" id="cambiar-estudiante">Cambiar</a></p>`
    : `<p class="usuario-activo"><a href="index.html">Elegir estudiante</a></p>`;

  document.getElementById('encabezado').innerHTML = `
    <div class="encabezado__contenido">
      <a class="marca" href="index.html"><span class="marca__icono" aria-hidden="true"><span></span></span>RutaIA</a>
      <nav class="navegacion" aria-label="Principal">${enlaces}</nav>
      ${usuario}
    </div>`;

  document.getElementById('cambiar-estudiante')?.addEventListener('click', () => cerrarSesion());
}

// --------------------------------- Seguridad ---------------------------------

/**
 * Convierte caracteres especiales en entidades HTML.
 * Todo texto que venga de la API (o del usuario) pasa por aqui antes de
 * insertarse con innerHTML: asi se evita la inyeccion de codigo (XSS).
 */
export function escaparHtml(valor) {
  return String(valor ?? '')
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;');
}

/**
 * Da formato basico al texto del modelo: parrafos y **negritas**.
 * Primero se escapa todo y luego se agregan solo las etiquetas permitidas.
 */
export function formatearRespuesta(texto) {
  return escaparHtml(texto)
    .split(/\n{2,}/)
    .map(parrafo => `<p>${parrafo.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>').replace(/\n/g, '<br>')}</p>`)
    .join('');
}

// ------------------------------- Formato de datos -------------------------------

export function formatearFecha(iso) {
  if (!iso) return '';
  return new Date(iso).toLocaleString('es-CO', { dateStyle: 'medium', timeStyle: 'short' });
}

export function pintarSimilitud(valor) {
  const porcentaje = Math.max(0, Math.min(100, Math.round(valor * 100)));
  return `<span class="similitud" title="Similitud semántica con tu pregunta">
            <span class="similitud__barra" aria-hidden="true"><span style="--valor:${porcentaje}%"></span></span>
            Similitud ${Number(valor).toFixed(2)}
          </span>`;
}

export function pintarEstrellas(puntuacion) {
  return `<span class="estrellas-fijas" aria-label="${puntuacion} de 5">${'★'.repeat(puntuacion)}${'☆'.repeat(5 - puntuacion)}</span>`;
}

// --------------------------------- Mensajes ---------------------------------

export function notificar(mensaje, tipo = 'info') {
  let contenedor = document.querySelector('.notificaciones');
  if (!contenedor) {
    contenedor = document.createElement('div');
    contenedor.className = 'notificaciones';
    contenedor.setAttribute('role', 'status');
    document.body.appendChild(contenedor);
  }
  const aviso = document.createElement('div');
  aviso.className = `notificacion${tipo === 'error' ? ' notificacion--error' : ''}`;
  aviso.textContent = mensaje;
  contenedor.appendChild(aviso);
  setTimeout(() => aviso.remove(), 5000);
}

/** Pone un boton en estado de carga y devuelve una funcion para restaurarlo. */
export function botonCargando(boton, texto) {
  const original = boton.innerHTML;
  boton.disabled = true;
  boton.innerHTML = `<span class="girando" aria-hidden="true"></span>${escaparHtml(texto)}`;
  return () => {
    boton.disabled = false;
    boton.innerHTML = original;
  };
}

/** Muestra los errores de validacion que devuelve la API junto a cada campo. */
export function mostrarErroresFormulario(formulario, errores) {
  limpiarErroresFormulario(formulario);
  if (!errores) return;
  for (const [campo, mensaje] of Object.entries(errores)) {
    const entrada = formulario.querySelector(`[name="${campo}"]`);
    const contenedor = entrada?.closest('.campo');
    if (!contenedor) continue;
    contenedor.classList.add('campo--error');
    const error = document.createElement('span');
    error.className = 'error-campo';
    error.id = `error-${campo}`;
    error.textContent = mensaje;
    contenedor.appendChild(error);
    entrada.setAttribute('aria-describedby', error.id);
  }
}

export function limpiarErroresFormulario(formulario) {
  formulario.querySelectorAll('.error-campo').forEach(e => e.remove());
  formulario.querySelectorAll('.campo--error').forEach(c => c.classList.remove('campo--error'));
}

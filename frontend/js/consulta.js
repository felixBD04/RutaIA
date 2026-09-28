import { api, ApiError } from './api.js';
import * as ui from './ui.js';

ui.pintarEncabezado('consulta');

const estudiante = ui.obtenerEstudianteActivo();
const formulario = document.getElementById('formulario-pregunta');
const campoPregunta = document.getElementById('pregunta');
const campoNivel = document.getElementById('nivelCurso'); // EXAMEN: filtro opcional por nivel
const resultado = document.getElementById('resultado');

// Tiempos de espera: el flujo RAG puede tardar varios segundos
const MENSAJES_ESPERA = [
  'Buscando cursos relacionados en el catálogo…',
  'Comparando tu necesidad con cada curso…',
  'Preparando tu ruta de aprendizaje…',
];

if (!estudiante) {
  document.getElementById('sin-estudiante').hidden = false;
} else {
  document.getElementById('zona-consulta').hidden = false;
  iniciar();
}

function iniciar() {
  // Las sugerencias llenan el campo con un clic
  document.querySelectorAll('#sugerencias button').forEach(boton => {
    boton.addEventListener('click', () => {
      campoPregunta.value = boton.textContent;
      campoPregunta.focus();
    });
  });

  formulario.addEventListener('submit', (evento) => {
    evento.preventDefault();
    enviarPregunta(campoPregunta.value, campoNivel.value);
  });

  // Si la URL trae ?id=5, se muestra esa consulta (se usa desde el historial)
  const id = new URLSearchParams(window.location.search).get('id');
  if (id) {
    cargarConsultaExistente(id);
  }
}

// ----------------------------------- Consultar -----------------------------------

async function enviarPregunta(texto, nivel = '') {
  const pregunta = texto.trim();
  ui.limpiarErroresFormulario(formulario);
  if (!pregunta) {
    ui.mostrarErroresFormulario(formulario, { pregunta: 'Escribe qué quieres aprender antes de buscar.' });
    campoPregunta.focus();
    return;
  }

  const boton = formulario.querySelector('button[type="submit"]');
  const restaurar = ui.botonCargando(boton, 'Buscando…');
  const detenerEspera = mostrarEspera();

  try {
    // EXAMEN: si no se elige nivel se envia null y la busqueda funciona sin filtro
    const consulta = await api.consultas.crear({ estudianteId: estudiante.id, pregunta, nivelCurso: nivel || null });
    pintarResultado(consulta);
  } catch (error) {
    pintarErrorPeticion(error);
  } finally {
    detenerEspera();
    restaurar();
  }
}

async function cargarConsultaExistente(id) {
  resultado.innerHTML = '<p class="texto-suave">Cargando la consulta…</p>';
  try {
    const consulta = await api.consultas.detalle(id);
    campoPregunta.value = consulta.pregunta;
    campoNivel.value = consulta.nivelCurso ?? '';
    pintarResultado(consulta);
  } catch (error) {
    pintarErrorPeticion(error);
  }
}

function mostrarEspera() {
  let indice = 0;
  resultado.innerHTML = `<div class="panel"><p class="texto-suave" id="texto-espera">${MENSAJES_ESPERA[0]}</p></div>`;
  const intervalo = setInterval(() => {
    indice = (indice + 1) % MENSAJES_ESPERA.length;
    const parrafo = document.getElementById('texto-espera');
    if (parrafo) parrafo.textContent = MENSAJES_ESPERA[indice];
  }, 2500);
  return () => clearInterval(intervalo);
}

// ------------------------------- Pintar resultados -------------------------------

function pintarResultado(consulta) {
  switch (consulta.estado) {
    case 'RESPONDIDA':
      pintarRecomendacion(consulta);
      break;
    case 'SIN_RESULTADOS':
      pintarSinResultados(consulta);
      break;
    case 'ERROR':
      pintarErrorConsulta(consulta);
      break;
    default:
      resultado.innerHTML = `
        <div class="alerta alerta--aviso">
          <h2>Esta consulta sigue pendiente</h2>
          <p>Aún no se ha generado una recomendación para esta pregunta.</p>
        </div>`;
  }
  resultado.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

function pintarRecomendacion(consulta) {
  const recomendacion = consulta.recomendacion;
  const texto = recomendacion.respuesta;

  // La ruta: los cursos que el modelo menciono, en el orden en que los menciona
  // (el modelo sugiere por cual empezar y como continuar).
  const textoNormalizado = normalizar(texto);
  let paradas = recomendacion.fuentes
    .filter(f => f.mencionado)
    .sort((a, b) => textoNormalizado.indexOf(normalizar(a.nombre)) - textoNormalizado.indexOf(normalizar(b.nombre)));
  if (paradas.length === 0) {
    paradas = [...recomendacion.fuentes]; // respaldo: si no se detectan menciones, se muestran todas
  }
  const consideradas = recomendacion.fuentes.filter(f => !paradas.includes(f));

  resultado.innerHTML = `
    <div class="resultado__rejilla">
      <section class="panel" aria-labelledby="titulo-ruta">
        <h2 id="titulo-ruta">Tu ruta sugerida</h2>
        ${pintarNotaNivel(consulta)}
        <ol class="ruta">
          ${paradas.map((f, i) => pintarParada(f, i, paradas.length)).join('')}
        </ol>
      </section>

      <section aria-labelledby="titulo-recomendacion">
        <h2 id="titulo-recomendacion">Por qué estos cursos</h2>
        <div class="respuesta-ia">${ui.formatearRespuesta(texto)}</div>

        ${consideradas.length ? `
          <details class="consideradas">
            <summary>Otros cursos que se evaluaron (${consideradas.length})</summary>
            <p class="texto-suave">Se parecían a tu pregunta, pero no se incluyeron en la recomendación.</p>
            <ul>
              ${consideradas.map(f => `
                <li>
                  <a href="catalogo.html?curso=${f.cursoId}">${ui.escaparHtml(f.nombre)}</a>
                  ${ui.pintarSimilitud(f.similitud)}
                </li>`).join('')}
            </ul>
          </details>` : ''}

        <div id="zona-calificacion"></div>
      </section>
    </div>`;

  pintarCalificacion(recomendacion);
}

function pintarParada(fuente, indice, total) {
  const paso = total === 1 ? 'Tu curso' : indice === 0 ? 'Empieza aquí' : `Paso ${indice + 1}`;
  return `
    <li class="ruta__parada">
      <span class="ruta__paso">${paso}</span>
      <h3 class="ruta__nombre">${ui.escaparHtml(fuente.nombre)}</h3>
      <div class="ruta__datos">
        <span class="etiqueta">${ui.NIVELES_CURSO[fuente.nivel] ?? ''}</span>
        <span class="etiqueta">${fuente.duracionHoras} horas</span>
        <span class="etiqueta">${ui.escaparHtml(fuente.categoria)}</span>
        ${ui.pintarSimilitud(fuente.similitud)}
      </div>
    </li>`;
}

function pintarSinResultados(consulta) {
  resultado.innerHTML = `
    <div class="alerta alerta--aviso">
      <h2>No encontramos cursos para esta necesidad</h2>
      ${pintarNotaNivel(consulta)}
      <p>${ui.escaparHtml(consulta.recomendacion?.respuesta ?? 'Ningún curso del catálogo se relaciona lo suficiente con tu pregunta.')}</p>
      <p>${consulta.nivelCurso
        ? 'Prueba con otro nivel, busca en todos los niveles o describe tu necesidad de otra forma.'
        : 'Prueba describiéndola de otra forma, o revisa el catálogo completo para ver qué áreas cubrimos.'}</p>
      <div class="acciones">
        <button type="button" class="boton" id="otra-pregunta">Hacer otra pregunta</button>
        <a class="boton boton--secundario" href="catalogo.html">Ver el catálogo</a>
      </div>
    </div>
    <div id="zona-calificacion"></div>`;

  document.getElementById('otra-pregunta').addEventListener('click', () => {
    campoPregunta.select();
    campoPregunta.focus();
  });
  if (consulta.recomendacion) {
    pintarCalificacion(consulta.recomendacion);
  }
}

function pintarErrorConsulta(consulta) {
  resultado.innerHTML = `
    <div class="alerta alerta--error">
      <h2>No se pudo generar la recomendación</h2>
      <p>${ui.escaparHtml(consulta.mensaje ?? 'Ocurrió un problema al procesar esta consulta. Quedó registrada con estado de error.')}</p>
      <div class="acciones">
        <button type="button" class="boton" id="reintentar">Intentar de nuevo</button>
      </div>
    </div>`;
  document.getElementById('reintentar').addEventListener('click', () => enviarPregunta(consulta.pregunta, consulta.nivelCurso ?? ''));
}

function pintarErrorPeticion(error) {
  // Errores antes de crear la consulta: sin conexion, validacion, estudiante inexistente...
  if (error instanceof ApiError && error.errores) {
    ui.mostrarErroresFormulario(formulario, error.errores);
    resultado.innerHTML = '';
    return;
  }
  resultado.innerHTML = `
    <div class="alerta alerta--error">
      <h2>No pudimos enviar tu pregunta</h2>
      <p>${ui.escaparHtml(error.message)}</p>
    </div>`;
}

// ---------------------------------- Calificar ----------------------------------

function pintarCalificacion(recomendacion) {
  const zona = document.getElementById('zona-calificacion');
  if (!zona) return;

  if (recomendacion.calificacion) {
    const c = recomendacion.calificacion;
    zona.innerHTML = `
      <div class="panel calificacion">
        <h3>Tu calificación</h3>
        <p>${ui.pintarEstrellas(c.puntuacion)}</p>
        ${c.comentario ? `<p class="texto-suave">${ui.escaparHtml(c.comentario)}</p>` : ''}
      </div>`;
    return;
  }

  // Las estrellas van en orden inverso (5 a 1) por el truco de CSS row-reverse
  const estrellas = [5, 4, 3, 2, 1].map(n => `
    <input type="radio" id="estrella-${n}" name="puntuacion" value="${n}">
    <label for="estrella-${n}" title="${n} de 5">★<span class="solo-lectores">${n} de 5</span></label>`).join('');

  zona.innerHTML = `
    <form class="panel calificacion" id="formulario-calificacion" novalidate>
      <h3>¿Te sirvió esta recomendación?</h3>
      <div class="campo">
        <fieldset class="estrellas">
          <legend class="solo-lectores">Puntuación de 1 a 5</legend>
          ${estrellas}
        </fieldset>
      </div>
      <div class="campo">
        <label for="comentario">Comentario (opcional)</label>
        <textarea id="comentario" name="comentario" maxlength="500"></textarea>
      </div>
      <button type="submit" class="boton">Enviar calificación</button>
    </form>`;

  const formularioCalificacion = document.getElementById('formulario-calificacion');
  formularioCalificacion.addEventListener('submit', async (evento) => {
    evento.preventDefault();
    const datos = new FormData(formularioCalificacion);
    const puntuacion = Number(datos.get('puntuacion'));
    if (!puntuacion) {
      ui.mostrarErroresFormulario(formularioCalificacion, { puntuacion: 'Elige de 1 a 5 estrellas.' });
      return;
    }

    const boton = formularioCalificacion.querySelector('button[type="submit"]');
    const restaurar = ui.botonCargando(boton, 'Enviando…');
    try {
      const calificacion = await api.calificar(recomendacion.id, {
        puntuacion,
        comentario: datos.get('comentario'),
      });
      recomendacion.calificacion = calificacion;
      pintarCalificacion(recomendacion);
      ui.notificar('Calificación enviada. ¡Gracias!');
    } catch (error) {
      restaurar();
      if (error instanceof ApiError && error.errores) {
        ui.mostrarErroresFormulario(formularioCalificacion, error.errores);
      } else {
        ui.notificar(error.message, 'error');
      }
    }
  });
}

// ---------------------------------- Auxiliares ----------------------------------

// EXAMEN: indica si la busqueda en Qdrant se filtro por nivel
function pintarNotaNivel(consulta) {
  return consulta.nivelCurso
    ? `<p class="texto-suave">Búsqueda filtrada por nivel <strong>${ui.NIVELES_CURSO[consulta.nivelCurso]}</strong>.</p>`
    : '<p class="texto-suave">Búsqueda en todos los niveles.</p>';
}

function normalizar(texto) {
  return String(texto ?? '').normalize('NFD').replace(/\p{M}/gu, '').toLowerCase();
}

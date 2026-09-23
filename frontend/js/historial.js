import { api } from './api.js';
import * as ui from './ui.js';

ui.pintarEncabezado('historial');

const estudiante = ui.obtenerEstudianteActivo();
const contenedor = document.getElementById('historial');
const resumen = document.getElementById('resumen');

async function cargar() {
  if (!estudiante) {
    contenedor.innerHTML = `
      <div class="alerta alerta--aviso">
        <h2>Primero elige tu perfil</h2>
        <p>El historial muestra las consultas de un estudiante en particular.</p>
        <div class="acciones"><a class="boton" href="index.html">Elegir o crear perfil</a></div>
      </div>`;
    return;
  }

  resumen.textContent = 'Cargando tus consultas…';
  try {
    const consultas = await api.estudiantes.historial(estudiante.id);
    pintar(consultas);
  } catch (error) {
    resumen.textContent = '';
    contenedor.innerHTML = `<div class="alerta alerta--error"><p>${ui.escaparHtml(error.message)}</p></div>`;
  }
}

function pintar(consultas) {
  if (consultas.length === 0) {
    resumen.textContent = '';
    contenedor.innerHTML = `
      <div class="vacio">
        <p>Todavía no has hecho ninguna consulta. Cuéntanos qué quieres aprender y aquí quedará guardada tu ruta.</p>
        <a class="boton" href="consulta.html">Hacer mi primera consulta</a>
      </div>`;
    return;
  }

  resumen.textContent = consultas.length === 1 ? 'Has hecho 1 consulta.' : `Has hecho ${consultas.length} consultas.`;
  contenedor.innerHTML = `<ol class="lista-historial">${consultas.map(pintarConsulta).join('')}</ol>`;
}

function pintarConsulta(consulta) {
  const recomendacion = consulta.recomendacion;
  const mencionados = recomendacion?.fuentes.filter(f => f.mencionado) ?? [];

  let detalle = '';
  if (consulta.estado === 'RESPONDIDA' && mencionados.length) {
    detalle = `<div class="cursos-mencionados">${mencionados
      .map(f => `<span class="etiqueta">${ui.escaparHtml(f.nombre)}</span>`).join('')}</div>`;
  } else if (consulta.estado === 'SIN_RESULTADOS') {
    detalle = '<p class="texto-suave">Ningún curso del catálogo coincidió con esta necesidad.</p>';
  } else if (consulta.estado === 'ERROR') {
    detalle = '<p class="texto-suave">No se pudo generar la recomendación. Puedes volver a intentarlo.</p>';
  }

  let calificacion = '';
  if (recomendacion?.calificacion) {
    calificacion = ui.pintarEstrellas(recomendacion.calificacion.puntuacion);
  } else if (recomendacion) {
    calificacion = '<span class="texto-suave">Sin calificar</span>';
  }

  return `
    <li class="historial-item">
      <div class="historial-item__cabecera">
        <time datetime="${ui.escaparHtml(consulta.fechaConsulta)}">${ui.formatearFecha(consulta.fechaConsulta)}</time>
        <span class="estado estado--${consulta.estado}">${ui.ESTADOS[consulta.estado] ?? consulta.estado}</span>
      </div>
      <p class="historial-item__pregunta">${ui.escaparHtml(consulta.pregunta)}</p>
      ${detalle}
      <div class="historial-item__pie">
        ${calificacion}
        <a class="boton boton--secundario boton--pequeno" href="consulta.html?id=${consulta.id}">Ver consulta completa</a>
      </div>
    </li>`;
}

cargar();

import { api } from './api.js';
import * as ui from './ui.js';

ui.pintarEncabezado('estadisticas');

const contenedor = document.getElementById('estadisticas');

const COLORES_ESTADO = {
  RESPONDIDA: 'var(--ruta)',
  SIN_RESULTADOS: 'var(--marcador)',
  ERROR: 'var(--error)',
  PENDIENTE: '#A7B2C2',
};

async function cargar() {
  contenedor.innerHTML = '<p class="texto-suave">Calculando estadísticas…</p>';
  try {
    pintar(await api.estadisticas());
  } catch (error) {
    contenedor.innerHTML = `<div class="alerta alerta--error"><p>${ui.escaparHtml(error.message)}</p></div>`;
  }
}

function pintar(e) {
  contenedor.innerHTML = `
    <section class="cifras" aria-label="Resumen">
      <p class="cifra"><strong>${e.totalConsultas}</strong><span>consultas realizadas</span></p>
      <p class="cifra"><strong>${e.totalEstudiantes}</strong><span>estudiantes registrados</span></p>
      <p class="cifra"><strong>${e.totalCursosActivos}</strong><span>cursos activos en el catálogo</span></p>
    </section>

    <section class="seccion panel" aria-labelledby="titulo-estados">
      <h2 id="titulo-estados">Resultado de las consultas</h2>
      ${pintarEstados(e.consultasPorEstado, e.totalConsultas)}
    </section>

    <div class="rejilla-2 seccion">
      <section class="panel" aria-labelledby="titulo-cursos">
        <h2 id="titulo-cursos">Cursos más recomendados</h2>
        ${pintarRanking(e.topCursosRecomendados)}
      </section>

      <section class="panel" aria-labelledby="titulo-calificaciones">
        <h2 id="titulo-calificaciones">Calificación de las recomendaciones</h2>
        ${pintarPromedio(e.promedioCalificacion, e.totalCalificaciones)}
      </section>
    </div>`;
}

function pintarEstados(porEstado, total) {
  if (!total) {
    return '<p class="vacio">Aún no hay consultas. Las estadísticas aparecerán cuando los estudiantes empiecen a preguntar.</p>';
  }
  const entradas = Object.entries(porEstado);
  const segmentos = entradas
    .filter(([, cantidad]) => cantidad > 0)
    .map(([estado, cantidad]) =>
      `<span style="width:${(cantidad / total) * 100}%; background:${COLORES_ESTADO[estado]}" title="${ui.ESTADOS[estado]}: ${cantidad}"></span>`)
    .join('');
  const leyenda = entradas
    .map(([estado, cantidad]) => {
      const porcentaje = Math.round((cantidad / total) * 100);
      return `<li><i style="background:${COLORES_ESTADO[estado]}"></i>${ui.ESTADOS[estado]}: <strong>${cantidad}</strong> (${porcentaje}%)</li>`;
    })
    .join('');
  return `
    <div class="barra-apilada" role="img" aria-label="Distribución de consultas por estado">${segmentos}</div>
    <ul class="leyenda">${leyenda}</ul>`;
}

function pintarRanking(cursos) {
  if (!cursos.length) {
    return '<p class="texto-suave">Todavía ningún curso ha sido recomendado.</p>';
  }
  const maximo = cursos[0].vecesRecomendado;
  return `
    <p class="texto-suave">Solo se cuentan los cursos que el asistente nombró en su respuesta.</p>
    <ol class="ranking">
      ${cursos.map(c => `
        <li>
          <a class="ranking__nombre" href="catalogo.html?curso=${c.cursoId}">${ui.escaparHtml(c.nombre)}</a>
          <span class="ranking__veces">${c.vecesRecomendado} ${c.vecesRecomendado === 1 ? 'vez' : 'veces'}</span>
          <span class="ranking__barra" aria-hidden="true"><span style="--valor:${(c.vecesRecomendado / maximo) * 100}%"></span></span>
        </li>`).join('')}
    </ol>`;
}

function pintarPromedio(promedio, total) {
  if (promedio === null || promedio === undefined) {
    return '<p class="texto-suave">Aún no hay calificaciones.</p>';
  }
  return `
    <div class="promedio">
      <strong>${promedio.toFixed(1)}</strong>
      ${ui.pintarEstrellas(Math.round(promedio))}
    </div>
    <p class="texto-suave">Promedio de ${total} ${total === 1 ? 'calificación' : 'calificaciones'}, en una escala de 1 a 5.</p>`;
}

cargar();

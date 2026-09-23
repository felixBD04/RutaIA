import { api } from './api.js';
import * as ui from './ui.js';

ui.pintarEncabezado('catalogo');

const selectCategoria = document.getElementById('filtro-categoria');
const selectNivel = document.getElementById('filtro-nivel');
const botonLimpiar = document.getElementById('limpiar-filtros');
const contador = document.getElementById('contador');
const resultados = document.getElementById('resultados');

const parametros = new URLSearchParams(window.location.search);
const cursoDestacado = Number(parametros.get('curso')) || null; // viene desde la pagina de consulta

async function iniciar() {
  try {
    const categorias = await api.cursos.categorias();
    selectCategoria.insertAdjacentHTML('beforeend',
      categorias.map(c => `<option value="${ui.escaparHtml(c)}">${ui.escaparHtml(c)}</option>`).join(''));
  } catch (error) {
    ui.notificar(error.message, 'error');
  }

  // Los filtros se pueden compartir por URL: catalogo.html?categoria=...&nivel=...
  selectCategoria.value = parametros.get('categoria') ?? '';
  selectNivel.value = parametros.get('nivel') ?? '';

  selectCategoria.addEventListener('change', cargar);
  selectNivel.addEventListener('change', cargar);
  botonLimpiar.addEventListener('click', () => {
    selectCategoria.value = '';
    selectNivel.value = '';
    cargar();
  });

  cargar();
}

async function cargar() {
  const filtros = { categoria: selectCategoria.value, nivel: selectNivel.value };
  actualizarUrl(filtros);
  contador.textContent = 'Cargando cursos…';

  try {
    const cursos = await api.cursos.catalogo(filtros);
    pintar(cursos);
  } catch (error) {
    contador.textContent = '';
    resultados.innerHTML = `<div class="alerta alerta--error"><p>${ui.escaparHtml(error.message)}</p></div>`;
  }
}

function pintar(cursos) {
  contador.textContent = cursos.length === 1 ? '1 curso disponible' : `${cursos.length} cursos disponibles`;

  if (cursos.length === 0) {
    resultados.innerHTML = `
      <div class="vacio">
        <p>No hay cursos activos con estos filtros.</p>
        <button type="button" class="boton boton--secundario" id="vacio-limpiar">Ver todos los cursos</button>
      </div>`;
    document.getElementById('vacio-limpiar').addEventListener('click', () => botonLimpiar.click());
    return;
  }

  // Agrupar por categoria
  const grupos = new Map();
  for (const curso of cursos) {
    if (!grupos.has(curso.categoria)) grupos.set(curso.categoria, []);
    grupos.get(curso.categoria).push(curso);
  }

  resultados.innerHTML = [...grupos.entries()]
    .sort(([a], [b]) => a.localeCompare(b, 'es'))
    .map(([categoria, lista]) => `
      <section class="categoria-grupo">
        <h2>${ui.escaparHtml(categoria)}<span>${lista.length} ${lista.length === 1 ? 'curso' : 'cursos'}</span></h2>
        <div class="cursos">${lista.map(pintarCurso).join('')}</div>
      </section>`)
    .join('');

  if (cursoDestacado) {
    document.getElementById(`curso-${cursoDestacado}`)?.scrollIntoView({ behavior: 'smooth', block: 'center' });
  }
}

function pintarCurso(curso) {
  const destacado = curso.id === cursoDestacado ? ' curso--destacado' : '';
  return `
    <article class="curso curso--${curso.nivel}${destacado}" id="curso-${curso.id}">
      <h3>${ui.escaparHtml(curso.nombre)}</h3>
      <div class="ruta__datos">
        <span class="etiqueta">${ui.NIVELES_CURSO[curso.nivel] ?? ''}</span>
        <span class="etiqueta">${curso.duracionHoras} horas</span>
      </div>
      <p>${ui.escaparHtml(curso.descripcion)}</p>
    </article>`;
}

function actualizarUrl(filtros) {
  const nuevos = new URLSearchParams();
  if (filtros.categoria) nuevos.set('categoria', filtros.categoria);
  if (filtros.nivel) nuevos.set('nivel', filtros.nivel);
  if (cursoDestacado) nuevos.set('curso', cursoDestacado);
  const consulta = nuevos.toString();
  history.replaceState(null, '', consulta ? `?${consulta}` : window.location.pathname);
}

iniciar();

import { api, ApiError } from './api.js';
import * as ui from './ui.js';

ui.pintarEncabezado('admin');

const formulario = document.getElementById('formulario-curso');
const tituloFormulario = document.getElementById('titulo-formulario');
const botonGuardar = document.getElementById('guardar-curso');
const botonCancelar = document.getElementById('cancelar-edicion');
const tabla = document.getElementById('tabla-cursos');
const buscador = document.getElementById('buscar-curso');
const listaCategorias = document.getElementById('categorias-existentes');

let cursos = [];
let idEnEdicion = null;

// -------------------------------- Listado --------------------------------

async function cargar() {
  try {
    cursos = await api.admin.listar();
    cursos.sort((a, b) => a.nombre.localeCompare(b.nombre, 'es'));
    listaCategorias.innerHTML = [...new Set(cursos.map(c => c.categoria))]
      .map(c => `<option value="${ui.escaparHtml(c)}"></option>`).join('');
    pintarTabla();
  } catch (error) {
    tabla.innerHTML = `<div class="alerta alerta--error"><p>${ui.escaparHtml(error.message)}</p></div>`;
  }
}

function pintarTabla() {
  const filtro = buscador.value.trim().toLowerCase();
  const visibles = cursos.filter(c =>
    c.nombre.toLowerCase().includes(filtro) || c.categoria.toLowerCase().includes(filtro));

  if (visibles.length === 0) {
    tabla.innerHTML = '<p class="vacio">No hay cursos que coincidan con la búsqueda.</p>';
    return;
  }

  tabla.innerHTML = `
    <div class="tabla-contenedor">
      <table>
        <thead>
          <tr><th>Curso</th><th>Nivel</th><th>Horas</th><th>Estado</th><th><span class="solo-lectores">Acciones</span></th></tr>
        </thead>
        <tbody>
          ${visibles.map(c => `
            <tr class="${c.activo ? '' : 'fila-inactiva'}">
              <td>${ui.escaparHtml(c.nombre)}<br><span class="texto-suave">${ui.escaparHtml(c.categoria)}</span></td>
              <td>${ui.NIVELES_CURSO[c.nivel] ?? ''}</td>
              <td>${c.duracionHoras}</td>
              <td><span class="estado estado--${c.activo ? 'activo' : 'inactivo'}">${c.activo ? 'Activo' : 'Inactivo'}</span></td>
              <td>
                <div class="acciones-fila">
                  <button class="boton boton--pequeno boton--secundario" data-accion="editar" data-id="${c.id}">Editar</button>
                  <button class="boton boton--pequeno boton--secundario" data-accion="${c.activo ? 'desactivar' : 'activar'}" data-id="${c.id}">
                    ${c.activo ? 'Desactivar' : 'Activar'}
                  </button>
                </div>
              </td>
            </tr>`).join('')}
        </tbody>
      </table>
    </div>`;
}

// Un solo "escuchador" para todos los botones de la tabla (delegacion de eventos)
tabla.addEventListener('click', async (evento) => {
  const boton = evento.target.closest('button[data-accion]');
  if (!boton) return;
  const curso = cursos.find(c => c.id === Number(boton.dataset.id));

  if (boton.dataset.accion === 'editar') {
    empezarEdicion(curso);
    return;
  }

  const desactivar = boton.dataset.accion === 'desactivar';
  if (desactivar && !confirm(`¿Desactivar "${curso.nombre}"? Dejará de aparecer en el catálogo y en las recomendaciones.`)) {
    return;
  }

  const restaurar = ui.botonCargando(boton, desactivar ? 'Desactivando…' : 'Activando…');
  try {
    if (desactivar) {
      await api.admin.desactivar(curso.id);
    } else {
      await api.admin.activar(curso.id);
    }
    ui.notificar(desactivar ? `"${curso.nombre}" desactivado.` : `"${curso.nombre}" activado.`);
    await cargar();
  } catch (error) {
    restaurar();
    ui.notificar(error.message, 'error');
  }
});

buscador.addEventListener('input', pintarTabla);

// ------------------------------ Crear y editar ------------------------------

function empezarEdicion(curso) {
  idEnEdicion = curso.id;
  formulario.nombre.value = curso.nombre;
  formulario.descripcion.value = curso.descripcion;
  formulario.categoria.value = curso.categoria;
  formulario.nivel.value = curso.nivel;
  formulario.duracionHoras.value = curso.duracionHoras;
  tituloFormulario.textContent = 'Editar curso';
  botonGuardar.textContent = 'Guardar cambios';
  botonCancelar.hidden = false;
  ui.limpiarErroresFormulario(formulario);
  formulario.scrollIntoView({ behavior: 'smooth', block: 'start' });
  formulario.nombre.focus();
}

function terminarEdicion() {
  idEnEdicion = null;
  formulario.reset();
  tituloFormulario.textContent = 'Nuevo curso';
  botonGuardar.textContent = 'Crear curso';
  botonCancelar.hidden = true;
  ui.limpiarErroresFormulario(formulario);
}

botonCancelar.addEventListener('click', terminarEdicion);

formulario.addEventListener('submit', async (evento) => {
  evento.preventDefault();
  const datos = Object.fromEntries(new FormData(formulario));
  datos.duracionHoras = datos.duracionHoras ? Number(datos.duracionHoras) : null;

  // Validacion rapida en el navegador (la API vuelve a validar todo)
  const errores = {};
  if (!datos.nombre.trim()) errores.nombre = 'Escribe el nombre del curso.';
  if (datos.descripcion.trim().length < 30) errores.descripcion = 'La descripción debe tener al menos 30 caracteres.';
  if (!datos.categoria.trim()) errores.categoria = 'Escribe la categoría.';
  if (!datos.nivel) errores.nivel = 'Selecciona el nivel.';
  if (!datos.duracionHoras || datos.duracionHoras <= 0) errores.duracionHoras = 'La duración debe ser mayor que cero.';
  ui.mostrarErroresFormulario(formulario, errores);
  if (Object.keys(errores).length) return;

  const editando = idEnEdicion !== null;
  const restaurar = ui.botonCargando(botonGuardar, editando ? 'Guardando…' : 'Creando…');
  try {
    if (editando) {
      await api.admin.actualizar(idEnEdicion, datos);
      ui.notificar('Cambios guardados.');
    } else {
      await api.admin.registrar(datos);
      ui.notificar('Curso creado.');
    }
    restaurar();
    terminarEdicion();
    await cargar();
  } catch (error) {
    restaurar();
    if (error instanceof ApiError && error.estado === 409) {
      ui.mostrarErroresFormulario(formulario, { nombre: error.message });
    } else if (error instanceof ApiError && error.errores) {
      ui.mostrarErroresFormulario(formulario, error.errores);
    } else {
      ui.notificar(error.message, 'error');
    }
  }
});

cargar();

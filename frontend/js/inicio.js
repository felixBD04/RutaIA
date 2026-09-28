import { api, ApiError } from './api.js';
import * as ui from './ui.js';

ui.pintarEncabezado('inicio');

const lista = document.getElementById('lista-estudiantes');
const formulario = document.getElementById('formulario-registro');

// ------------------------- Elegir un estudiante existente -------------------------

async function cargarEstudiantes() {
  try {
    const estudiantes = await api.estudiantes.listar();
    if (estudiantes.length === 0) {
      lista.innerHTML = '<p class="vacio">Aún no hay estudiantes registrados. Crea tu perfil para empezar.</p>';
      return;
    }
    lista.innerHTML = `
      <div class="tabla-contenedor">
        <table>
          <thead><tr><th>Nombre</th><th>Nivel</th><th><span class="solo-lectores">Acción</span></th></tr></thead>
          <tbody>
            ${estudiantes.map(e => `
              <tr>
                <td>${ui.escaparHtml(e.nombreCompleto)}<br><span class="texto-suave">${ui.escaparHtml(e.areaInteres)}</span></td>
                <td>${ui.NIVELES_ESTUDIANTE[e.nivelExperiencia] ?? ''}</td>
                <td><button class="boton boton--pequeno boton--secundario" data-id="${e.id}">Continuar</button></td>
              </tr>`).join('')}
          </tbody>
        </table>
      </div>`;

    lista.querySelectorAll('button[data-id]').forEach(boton => {
      boton.addEventListener('click', () => {
        const estudiante = estudiantes.find(e => e.id === Number(boton.dataset.id));
        ui.guardarEstudianteActivo(estudiante);
        window.location.href = 'consulta.html';
      });
    });
  } catch (error) {
    lista.innerHTML = `<div class="alerta alerta--error"><p>${ui.escaparHtml(error.message)}</p></div>`;
  }
}

// ------------------------------ Registrar estudiante ------------------------------

formulario.addEventListener('submit', async (evento) => {
  evento.preventDefault();
  const datos = Object.fromEntries(new FormData(formulario));

  // Validacion rapida en el navegador (la API vuelve a validar todo)
  const errores = {};
  if (!datos.nombreCompleto.trim()) errores.nombreCompleto = 'Escribe tu nombre completo.';
  if (!/^\S+@\S+\.\S+$/.test(datos.correo.trim())) errores.correo = 'Escribe un correo válido, como nombre@correo.com.';
  if (!datos.nivelExperiencia) errores.nivelExperiencia = 'Selecciona tu nivel de experiencia.';
  if (!datos.areaInteres.trim()) errores.areaInteres = 'Escribe un área de interés.';
  ui.mostrarErroresFormulario(formulario, errores); // tambien limpia errores anteriores
  if (Object.keys(errores).length) {
    return;
  }

  const boton = formulario.querySelector('button[type="submit"]');
  const restaurar = ui.botonCargando(boton, 'Creando perfil…');
  try {
    const estudiante = await api.estudiantes.registrar(datos);
    ui.guardarEstudianteActivo(estudiante);
    window.location.href = 'consulta.html';
  } catch (error) {
    if (error instanceof ApiError && error.estado === 409) {
      ui.mostrarErroresFormulario(formulario, { correo: error.message });
    } else if (error instanceof ApiError && error.errores) {
      ui.mostrarErroresFormulario(formulario, error.errores);
    } else {
      ui.notificar(error.message, 'error');
    }
    restaurar();
  }
});

cargarEstudiantes();

package com.rutaia.backend.dto.consulta;

import com.rutaia.backend.model.Curso;
import com.rutaia.backend.model.RecomendacionFuente;
import com.rutaia.backend.model.enums.NivelCurso;

import java.text.Normalizer;

/**
 * Un curso usado como fuente de una recomendacion (RF 13).
 *
 * "mencionado" indica si el modelo nombro este curso en su respuesta.
 * Recuerda el caso de "proteger aplicaciones web": se enviaron 5 cursos como
 * contexto, pero el modelo solo recomendo 2. El frontend puede destacar esos.
 */
public record FuenteResponse(
        Integer posicion,
        Long cursoId,
        String nombre,
        String categoria,
        NivelCurso nivel,
        Integer duracionHoras,
        Double similitud,
        boolean mencionado
) {

    public static FuenteResponse desde(RecomendacionFuente fuente, String textoRespuesta) {
        Curso curso = fuente.getCurso();
        boolean mencionado = normalizar(textoRespuesta).contains(normalizar(curso.getNombre()));
        return new FuenteResponse(
                fuente.getPosicion(),
                curso.getId(),
                curso.getNombre(),
                curso.getCategoria(),
                curso.getNivel(),
                curso.getDuracionHoras(),
                fuente.getSimilitud(),
                mencionado
        );
    }

    /** Minusculas y sin tildes, para comparar textos de forma tolerante. */
    private static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
    }
}

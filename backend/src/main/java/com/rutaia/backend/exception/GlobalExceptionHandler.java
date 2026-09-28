package com.rutaia.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manejo global de excepciones.
 * Cualquier excepcion lanzada en un controlador o servicio llega aqui,
 * y se transforma en una respuesta JSON con el codigo HTTP adecuado.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 404: el recurso no existe. */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(RecursoNoEncontradoException ex,
                                                      HttpServletRequest request) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    /** 409: conflicto con datos existentes (regla de negocio). */
    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<ErrorResponse> conflicto(ConflictoException ex,
                                                   HttpServletRequest request) {
        return construir(HttpStatus.CONFLICT, ex.getMessage(), request, null);
    }

    /** 400: fallaron las validaciones de un DTO (@NotBlank, @Email...). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException ex,
                                                    HttpServletRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errores.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return construir(HttpStatus.BAD_REQUEST,
                "La petición tiene datos inválidos", request, errores);
    }

    /** 400: JSON mal formado o valor no permitido (por ejemplo un nivel inexistente). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> jsonInvalido(HttpMessageNotReadableException ex,
                                                      HttpServletRequest request) {
        Throwable causa = ex.getMostSpecificCause();
        String mensaje = (causa instanceof IllegalArgumentException)
                ? causa.getMessage()
                : "El cuerpo de la petición no es un JSON válido";
        return construir(HttpStatus.BAD_REQUEST, mensaje, request, null);
    }

    /** 400: parametro de URL con tipo incorrecto (por ejemplo /estudiantes/abc). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> tipoIncorrecto(MethodArgumentTypeMismatchException ex,
                                                        HttpServletRequest request) {
        String mensaje = "El parámetro '" + ex.getName() + "' tiene un valor inválido: " + ex.getValue();
        return construir(HttpStatus.BAD_REQUEST, mensaje, request, null);
    }

    /** 400: valor no permitido detectado en el codigo (por ejemplo ?nivel=Experto). */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> argumentoInvalido(IllegalArgumentException ex,
                                                           HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null);
    }

    /** 404: la ruta pedida no existe en la API. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> rutaNoExiste(NoResourceFoundException ex,
                                                      HttpServletRequest request) {
        return construir(HttpStatus.NOT_FOUND, "La ruta solicitada no existe", request, null);
    }

    /** 405: la ruta existe pero no acepta ese metodo HTTP (por ejemplo DELETE). */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> metodoNoPermitido(HttpRequestMethodNotSupportedException ex,
                                                           HttpServletRequest request) {
        return construir(HttpStatus.METHOD_NOT_ALLOWED,
                "El método " + ex.getMethod() + " no está permitido en esta ruta", request, null);
    }

    /**
     * 409: la base de datos rechazo la operacion por una restriccion
     * (UNIQUE, CHECK, FOREIGN KEY). Es la "segunda capa" de proteccion.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> integridad(DataIntegrityViolationException ex,
                                                    HttpServletRequest request) {
        log.warn("Violación de integridad: {}", ex.getMostSpecificCause().getMessage());
        return construir(HttpStatus.CONFLICT,
                "La operación viola una restricción de la base de datos", request, null);
    }

    /** 500: cualquier error no previsto. Se registra en el log para investigarlo. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> general(Exception ex, HttpServletRequest request) {
        log.error("Error inesperado en {}", request.getRequestURI(), ex);
        return construir(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error interno. Intenta de nuevo más tarde", request, null);
    }

    private ResponseEntity<ErrorResponse> construir(HttpStatus estado, String mensaje,
                                                    HttpServletRequest request,
                                                    Map<String, String> errores) {
        ErrorResponse cuerpo = new ErrorResponse(
                LocalDateTime.now(),
                estado.value(),
                estado.getReasonPhrase(),
                mensaje,
                request.getRequestURI(),
                errores
        );
        return ResponseEntity.status(estado).body(cuerpo);
    }
}

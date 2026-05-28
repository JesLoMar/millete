package com.puntomartinez.millete.shared.infrastructure.in.controller.advice;

import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Atrapa los "throw new RuntimeException" de tus TransactionService y CategoryService
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDTO> handleRuntimeExceptions(RuntimeException ex, HttpServletRequest request) {

        // Lógica básica para deducir el status: si el mensaje habla de "no encontrada/no existe", es un 404.
        HttpStatus status = HttpStatus.BAD_REQUEST; // 400 por defecto
        if (ex.getMessage().toLowerCase().contains("no encontrada") || ex.getMessage().toLowerCase().contains("no existe")) {
            status = HttpStatus.NOT_FOUND; // 404
        }

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(), // Aquí sale el mensaje exacto que escribiste en el Servicio
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorResponse, status);
    }

    // Atrapa los errores puros de tu modelo de Dominio (ej: if(nombre.isBlank()) throw new IllegalArgumentException)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Atrapa los errores de las anotaciones @Valid de tus DTOs (ej: @NotNull, @NotBlank)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> validationErrors = new HashMap<>();

        // Recorremos todos los campos que han fallado y extraemos su mensaje
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            validationErrors.put(error.getField(), error.getDefaultMessage());
        }

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Error de validación en los datos enviados",
                validationErrors.toString(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
package com.puntomartinez.millete.shared.infrastructure.in.controller.advice;

import com.puntomartinez.millete.shared.domain.exception.*;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.ErrorResponseDTO;
import com.puntomartinez.millete.users.domain.exception.AccountLockedException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String GENERIC_ERROR_MESSAGE = "Ha ocurrido un error interno. Por favor, inténtalo de nuevo más tarde.";

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceAlreadyExistsException(ResourceAlreadyExistsException ex, HttpServletRequest request) {
        log.warn("Recurso duplicado: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<ErrorResponseDTO> handleForbiddenOperationException(ForbiddenOperationException ex, HttpServletRequest request) {
        log.warn("Operación prohibida: {}", ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidInputException(InvalidInputException ex, HttpServletRequest request) {
        log.warn("Entrada inválida: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAuthenticationFailedException(AuthenticationFailedException ex, HttpServletRequest request) {
        log.warn("Autenticación fallida: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDTO> handleSpringAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        log.warn("Autenticación fallida (Spring Security): {}", ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, "Credenciales inválidas o sesión expirada.", request.getRequestURI());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleSpringAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Acceso denegado (Spring Security): {}", ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, "No tienes permiso para realizar esta acción.", request.getRequestURI());
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccountLockedException(AccountLockedException ex, HttpServletRequest request) {
        log.warn("Cuenta bloqueada: {}", ex.getMessage());
        return buildResponse(HttpStatus.LOCKED, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponseDTO> handleDomainException(DomainException ex, HttpServletRequest request) {
        log.warn("Excepción de dominio no específica: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Argumento ilegal: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> validationErrors = new HashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            validationErrors.put(error.getField(), error.getDefaultMessage());
        }

        log.warn("Errores de validación: {}", validationErrors);

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Error de validación en los datos enviados",
                validationErrors.toString(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        log.warn("Tipo de argumento incorrecto: {}", ex.getMessage());
        String message = "El parámetro '" + ex.getName() + "' tiene un formato inválido.";
        return buildResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Mensaje no legible: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "El cuerpo de la petición no es válido.", request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleUnexpectedException(Exception ex, HttpServletRequest request) {
        log.error("Error interno no esperado en {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_ERROR_MESSAGE, request.getRequestURI());
    }

    private ResponseEntity<ErrorResponseDTO> buildResponse(HttpStatus status, String message, String path) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path
        );
        return new ResponseEntity<>(errorResponse, status);
    }
}

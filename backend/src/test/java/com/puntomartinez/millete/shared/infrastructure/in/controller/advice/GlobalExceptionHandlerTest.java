package com.puntomartinez.millete.shared.infrastructure.in.controller.advice;

import com.puntomartinez.millete.shared.domain.exception.*;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("GlobalExceptionHandler - Manejador global de excepciones")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/test");
    }

    @Test
    @DisplayName("ResourceNotFoundException devuelve 404")
    void shouldReturnNotFoundForResourceNotFoundException() {
        ResponseEntity<ErrorResponseDTO> response = handler.handleResourceNotFoundException(
                new ResourceNotFoundException("Recurso no encontrado"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getBody().message()).isEqualTo("Recurso no encontrado");
        assertThat(response.getBody().path()).isEqualTo("/api/v1/test");
    }

    @Test
    @DisplayName("ResourceAlreadyExistsException devuelve 409")
    void shouldReturnConflictForResourceAlreadyExistsException() {
        ResponseEntity<ErrorResponseDTO> response = handler.handleResourceAlreadyExistsException(
                new ResourceAlreadyExistsException("El recurso ya existe"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(response.getBody().message()).isEqualTo("El recurso ya existe");
    }

    @Test
    @DisplayName("ForbiddenOperationException devuelve 403")
    void shouldReturnForbiddenForForbiddenOperationException() {
        ResponseEntity<ErrorResponseDTO> response = handler.handleForbiddenOperationException(
                new ForbiddenOperationException("Operación no permitida"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(response.getBody().message()).isEqualTo("Operación no permitida");
    }

    @Test
    @DisplayName("InvalidInputException devuelve 400")
    void shouldReturnBadRequestForInvalidInputException() {
        ResponseEntity<ErrorResponseDTO> response = handler.handleInvalidInputException(
                new InvalidInputException("Entrada inválida"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().message()).isEqualTo("Entrada inválida");
    }

    @Test
    @DisplayName("AuthenticationFailedException devuelve 401")
    void shouldReturnUnauthorizedForAuthenticationFailedException() {
        ResponseEntity<ErrorResponseDTO> response = handler.handleAuthenticationFailedException(
                new AuthenticationFailedException("Credenciales inválidas"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(response.getBody().message()).isEqualTo("Credenciales inválidas");
    }

    @Test
    @DisplayName("Exception inesperada devuelve 500 con mensaje genérico")
    void shouldReturnInternalServerErrorForUnexpectedException() {
        ResponseEntity<ErrorResponseDTO> response = handler.handleUnexpectedException(
                new RuntimeException("Mensaje interno sensible"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(response.getBody().message()).isEqualTo("Ha ocurrido un error interno. Por favor, inténtalo de nuevo más tarde.");
        assertThat(response.getBody().message()).doesNotContain("Mensaje interno sensible");
    }
}

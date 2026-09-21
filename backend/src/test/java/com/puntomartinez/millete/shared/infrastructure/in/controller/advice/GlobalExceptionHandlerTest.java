package com.puntomartinez.millete.shared.infrastructure.in.controller.advice;

import com.puntomartinez.millete.shared.domain.exception.*;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        when(request.getRequestURI()).thenReturn("/api/v1/test");
    }

    @Nested
    @DisplayName("Domain exceptions")
    class DomainExceptions {

        @Test
        @DisplayName("Should return 404 for ResourceNotFoundException")
        void shouldReturn404ForResourceNotFound() {
            ResourceNotFoundException ex =
                    new ResourceNotFoundException("Item not found");

            ResponseEntity<ErrorResponseDTO> response =
                    handler.handleResourceNotFoundException(ex, request);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(404);
            assertThat(response.getBody().message())
                    .isEqualTo("Item not found");
            assertThat(response.getBody().path())
                    .isEqualTo("/api/v1/test");
        }

        @Test
        @DisplayName("Should return 409 for ResourceAlreadyExistsException")
        void shouldReturn409ForAlreadyExists() {
            ResourceAlreadyExistsException ex =
                    new ResourceAlreadyExistsException("Duplicate");

            ResponseEntity<ErrorResponseDTO> response =
                    handler.handleResourceAlreadyExistsException(ex, request);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody().message())
                    .isEqualTo("Duplicate");
        }

        @Test
        @DisplayName("Should return 403 for ForbiddenOperationException")
        void shouldReturn403ForForbidden() {
            ForbiddenOperationException ex =
                    new ForbiddenOperationException("Not allowed");

            ResponseEntity<ErrorResponseDTO> response =
                    handler.handleForbiddenOperationException(ex, request);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(response.getBody().message())
                    .isEqualTo("Not allowed");
        }

        @Test
        @DisplayName("Should return 400 for InvalidInputException")
        void shouldReturn400ForInvalidInput() {
            InvalidInputException ex =
                    new InvalidInputException("Bad data");

            ResponseEntity<ErrorResponseDTO> response =
                    handler.handleInvalidInputException(ex, request);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().message())
                    .isEqualTo("Bad data");
        }

        @Test
        @DisplayName("Should return 401 for AuthenticationFailedException")
        void shouldReturn401ForAuthFailed() {
            AuthenticationFailedException ex =
                    new AuthenticationFailedException("Invalid credentials");

            ResponseEntity<ErrorResponseDTO> response =
                    handler.handleAuthenticationFailedException(ex, request);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getBody().message())
                    .isEqualTo("Invalid credentials");
        }
    }

    @Nested
    @DisplayName("Spring Security exceptions")
    class SpringSecurityExceptions {

        @Test
        @DisplayName("Should return 401 for Spring AuthenticationException")
        void shouldReturn401ForSpringAuthException() {
            BadCredentialsException ex =
                    new BadCredentialsException("Bad credentials");

            ResponseEntity<ErrorResponseDTO> response =
                    handler.handleSpringAuthenticationException(ex, request);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getBody().message())
                    .isEqualTo("Credenciales inválidas o sesión expirada.");
        }

        @Test
        @DisplayName("Should return 403 for Spring AccessDeniedException")
        void shouldReturn403ForSpringAccessDenied() {
            AccessDeniedException ex =
                    new AccessDeniedException("Access denied");

            ResponseEntity<ErrorResponseDTO> response =
                    handler.handleSpringAccessDeniedException(ex, request);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(response.getBody().message())
                    .isEqualTo("No tienes permiso para realizar esta acción.");
        }
    }

    @Nested
    @DisplayName("Generic exceptions")
    class GenericExceptions {

        @Test
        @DisplayName("Should return 400 for IllegalArgumentException")
        void shouldReturn400ForIllegalArgument() {
            IllegalArgumentException ex =
                    new IllegalArgumentException("Invalid arg");

            ResponseEntity<ErrorResponseDTO> response =
                    handler.handleIllegalArgumentException(ex, request);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().message())
                    .isEqualTo("Invalid arg");
        }

        @Test
        @DisplayName("Should return 500 with generic message for unexpected exceptions")
        void shouldReturn500WithGenericMessage() {
            Exception ex = new RuntimeException("Secret internal error");

            ResponseEntity<ErrorResponseDTO> response =
                    handler.handleUnexpectedException(ex, request);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody().message())
                    .isEqualTo("Ha ocurrido un error interno. Por favor, inténtalo de nuevo más tarde.");
            // Must NOT leak internal error details
            assertThat(response.getBody().message())
                    .doesNotContain("Secret internal error");
        }

        @Test
        @DisplayName("Should return 400 for generic DomainException")
        void shouldReturn400ForGenericDomainException() {
            DomainException ex = new InvalidInputException("domain error");

            ResponseEntity<ErrorResponseDTO> response =
                    handler.handleDomainException(ex, request);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().message())
                    .isEqualTo("domain error");
        }
    }
}
package com.puntomartinez.millete.shared.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Domain Exceptions")
class DomainExceptionsTest {

    @Nested
    @DisplayName("InvalidInputException")
    class InvalidInputExceptionTest {

        @Test
        @DisplayName("Should extend DomainException")
        void shouldExtendDomainException() {
            InvalidInputException ex = new InvalidInputException("bad input");

            assertThat(ex).isInstanceOf(DomainException.class);
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Should carry message and cause")
        void shouldCarryMessageAndCause() {
            RuntimeException cause = new RuntimeException("root");
            InvalidInputException ex = new InvalidInputException("bad input", cause);

            assertThat(ex.getMessage()).isEqualTo("bad input");
            assertThat(ex.getCause()).isSameAs(cause);
        }
    }

    @Nested
    @DisplayName("ResourceNotFoundException")
    class ResourceNotFoundExceptionTest {

        @Test
        @DisplayName("Should extend DomainException")
        void shouldExtendDomainException() {
            ResourceNotFoundException ex =
                    new ResourceNotFoundException("not found");

            assertThat(ex).isInstanceOf(DomainException.class);
        }

        @Test
        @DisplayName("Should carry message and cause")
        void shouldCarryMessageAndCause() {
            RuntimeException cause = new RuntimeException("root");
            ResourceNotFoundException ex =
                    new ResourceNotFoundException("not found", cause);

            assertThat(ex.getMessage()).isEqualTo("not found");
            assertThat(ex.getCause()).isSameAs(cause);
        }
    }

    @Nested
    @DisplayName("ResourceAlreadyExistsException")
    class ResourceAlreadyExistsExceptionTest {

        @Test
        @DisplayName("Should extend DomainException")
        void shouldExtendDomainException() {
            ResourceAlreadyExistsException ex =
                    new ResourceAlreadyExistsException("duplicate");

            assertThat(ex).isInstanceOf(DomainException.class);
        }

        @Test
        @DisplayName("Should carry message and cause")
        void shouldCarryMessageAndCause() {
            RuntimeException cause = new RuntimeException("root");
            ResourceAlreadyExistsException ex =
                    new ResourceAlreadyExistsException("duplicate", cause);

            assertThat(ex.getMessage()).isEqualTo("duplicate");
            assertThat(ex.getCause()).isSameAs(cause);
        }
    }

    @Nested
    @DisplayName("ForbiddenOperationException")
    class ForbiddenOperationExceptionTest {

        @Test
        @DisplayName("Should extend DomainException")
        void shouldExtendDomainException() {
            ForbiddenOperationException ex =
                    new ForbiddenOperationException("forbidden");

            assertThat(ex).isInstanceOf(DomainException.class);
        }
    }

    @Nested
    @DisplayName("AuthenticationFailedException")
    class AuthenticationFailedExceptionTest {

        @Test
        @DisplayName("Should extend DomainException")
        void shouldExtendDomainException() {
            AuthenticationFailedException ex =
                    new AuthenticationFailedException("auth failed");

            assertThat(ex).isInstanceOf(DomainException.class);
        }

        @Test
        @DisplayName("Should carry message and cause")
        void shouldCarryMessageAndCause() {
            RuntimeException cause = new RuntimeException("root");
            AuthenticationFailedException ex =
                    new AuthenticationFailedException("auth failed", cause);

            assertThat(ex.getMessage()).isEqualTo("auth failed");
            assertThat(ex.getCause()).isSameAs(cause);
        }
    }
}
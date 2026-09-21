package com.puntomartinez.millete.shared.infrastructure.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CookieAuthFactory")
class CookieAuthFactoryTest {

    @Nested
    @DisplayName("validateCookieSecurityPolicy")
    class Validation {

        @Test
        @DisplayName("Should accept __Host- cookie with Secure and Path=/")
        void shouldAcceptHostCookieWithSecureAndRootPath() {
            CookieAuthFactory factory = new CookieAuthFactory(
                    "__Host-ms_token", true, true, "Strict", "/", 86400
            );

            assertThat(factory).isNotNull();
        }

        @Test
        @DisplayName("Should reject __Host- cookie without Secure")
        void shouldRejectHostCookieWithoutSecure() {
            assertThatThrownBy(() -> new CookieAuthFactory(
                    "__Host-ms_token", false, true, "Strict", "/", 86400
            )).isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("__Host-")
                    .hasMessageContaining("Secure");
        }

        @Test
        @DisplayName("Should reject __Host- cookie with non-root Path")
        void shouldRejectHostCookieWithNonRootPath() {
            assertThatThrownBy(() -> new CookieAuthFactory(
                    "__Host-ms_token", true, true, "Strict", "/api", 86400
            )).isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("__Host-")
                    .hasMessageContaining("Path=/");
        }

        @Test
        @DisplayName("Should accept non-__Host- cookie without Secure (dev mode)")
        void shouldAcceptNonHostCookieWithoutSecure() {
            CookieAuthFactory factory = new CookieAuthFactory(
                    "dev_token", false, true, "Lax", "/", 86400
            );

            assertThat(factory).isNotNull();
        }
    }

    @Nested
    @DisplayName("createJwtCookie")
    class CreateJwtCookie {

        @Test
        @DisplayName("Should create cookie with all security attributes")
        void shouldCreateCookieWithAllAttributes() {
            CookieAuthFactory factory = new CookieAuthFactory(
                    "__Host-ms_token", true, true, "Strict", "/", 86400
            );

            ResponseCookie cookie = factory.createJwtCookie("my-jwt-token");

            assertThat(cookie.getName()).isEqualTo("__Host-ms_token");
            assertThat(cookie.getValue()).isEqualTo("my-jwt-token");
            assertThat(cookie.isHttpOnly()).isTrue();
            assertThat(cookie.isSecure()).isTrue();
            assertThat(cookie.getSameSite()).isEqualTo("Strict");
            assertThat(cookie.getPath()).isEqualTo("/");
            assertThat(cookie.getMaxAge().getSeconds()).isEqualTo(86400);
        }

        @Test
        @DisplayName("Should respect configured max age")
        void shouldRespectConfiguredMaxAge() {
            CookieAuthFactory factory = new CookieAuthFactory(
                    "token", false, true, "Lax", "/", 3600
            );

            ResponseCookie cookie = factory.createJwtCookie("jwt");

            assertThat(cookie.getMaxAge().getSeconds()).isEqualTo(3600);
            assertThat(cookie.isSecure()).isFalse();
        }
    }

    @Nested
    @DisplayName("createExpiredCookie")
    class CreateExpiredCookie {

        @Test
        @DisplayName("Should create expired cookie with max age zero")
        void shouldCreateExpiredCookie() {
            CookieAuthFactory factory = new CookieAuthFactory(
                    "__Host-ms_token", true, true, "Strict", "/", 86400
            );

            ResponseCookie cookie = factory.createExpiredCookie();

            assertThat(cookie.getName()).isEqualTo("__Host-ms_token");
            assertThat(cookie.getValue()).isEmpty();
            assertThat(cookie.getMaxAge().getSeconds()).isZero();
            assertThat(cookie.isHttpOnly()).isTrue();
            assertThat(cookie.isSecure()).isTrue();
            assertThat(cookie.getSameSite()).isEqualTo("Strict");
            assertThat(cookie.getPath()).isEqualTo("/");
        }
    }
}
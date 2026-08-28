package com.puntomartinez.millete.shared.infrastructure.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class CookieAuthFactoryTest {

    @Test
    @DisplayName("Fallo rápido: cookie __Host- con secure=false no arranca")
    void shouldFailFastWhenHostPrefixWithoutSecure() {
        assertThatThrownBy(() -> new CookieAuthFactory(
                "__Host-ms_token", false, true, "Strict", "/", 43200))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("__Host-");
    }

    @Test
    @DisplayName("Fallo rápido: cookie __Host- con path distinto de / no arranca")
    void shouldFailFastWhenHostPrefixWithNonRootPath() {
        assertThatThrownBy(() -> new CookieAuthFactory(
                "__Host-ms_token", true, true, "Strict", "/api", 43200))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Path=/");
    }

    @Test
    @DisplayName("Cookie sin prefijo __Host- permite secure=false (perfil dev explícito)")
    void shouldAllowInsecureCookieWithoutHostPrefix() {
        assertDoesNotThrow(() -> new CookieAuthFactory(
                "ms_token", false, true, "Strict", "/", 43200));
    }

    @Test
    @DisplayName("La cookie JWT se crea con todos los atributos del contrato __Host-")
    void shouldCreateCookieWithHostPrefixAttributes() {
        CookieAuthFactory factory = new CookieAuthFactory(
                "__Host-ms_token", true, true, "Strict", "/", 43200);

        ResponseCookie cookie = factory.createJwtCookie("token");

        assertThat(cookie.getName()).isEqualTo("__Host-ms_token");
        assertThat(cookie.isSecure()).isTrue();
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.getSameSite()).isEqualTo("Strict");
    }

    @Test
    @DisplayName("La cookie expirada usa el mismo nombre y path (borrado efectivo)")
    void shouldExpireCookieWithSameNameAndPath() {
        CookieAuthFactory factory = new CookieAuthFactory(
                "__Host-ms_token", true, true, "Strict", "/", 43200);

        ResponseCookie expired = factory.createExpiredCookie();

        assertThat(expired.getName()).isEqualTo("__Host-ms_token");
        assertThat(expired.getPath()).isEqualTo("/");
        assertThat(expired.getMaxAge().getSeconds()).isZero();
    }
}
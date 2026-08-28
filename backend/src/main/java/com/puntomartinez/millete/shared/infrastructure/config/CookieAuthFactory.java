package com.puntomartinez.millete.shared.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CookieAuthFactory {

    private static final String HOST_PREFIX = "__Host-";

    private final String cookieName;
    private final boolean cookieSecure;
    private final boolean cookieHttpOnly;
    private final String cookieSameSite;
    private final String cookiePath;
    private final long cookieMaxAge;

    public CookieAuthFactory(
            @Value("${jwt.cookie-name:__Host-ms_token}") String cookieName,
            @Value("${jwt.cookie-secure:true}") boolean cookieSecure,
            @Value("${jwt.cookie-http-only:true}") boolean cookieHttpOnly,
            @Value("${jwt.cookie-same-site:Strict}") String cookieSameSite,
            @Value("${jwt.cookie-path:/}") String cookiePath,
            @Value("${jwt.cookie-max-age:86400}") long cookieMaxAge) {
        this.cookieName = cookieName;
        this.cookieSecure = cookieSecure;
        this.cookieHttpOnly = cookieHttpOnly;
        this.cookieSameSite = cookieSameSite;
        this.cookiePath = cookiePath;
        this.cookieMaxAge = cookieMaxAge;
        validateCookieSecurityPolicy();
    }

    private void validateCookieSecurityPolicy() {
        if (cookieName.startsWith(HOST_PREFIX)) {
            if (!cookieSecure) {
                throw new IllegalStateException(
                        "La cookie '" + cookieName + "' usa el prefijo __Host- pero jwt.cookie-secure=false. "
                                + "Los navegadores rechazan cookies __Host- sin Secure y el login nunca persistiría. "
                                + "Activa Secure o cambia el nombre de la cookie.");
            }
            if (!"/".equals(cookiePath)) {
                throw new IllegalStateException(
                        "La cookie '" + cookieName + "' usa el prefijo __Host- pero jwt.cookie-path='" + cookiePath + "'. "
                                + "El prefijo __Host- exige Path=/ exactamente.");
            }
            return;
        }
        if (!cookieSecure) {
            log.warn("⚠ La cookie de autenticación '{}' se está sirviendo SIN atributo Secure. "
                    + "Esto solo es aceptable en desarrollo local. En cualquier despliegue accesible "
                    + "desde red, sirve la aplicación tras HTTPS (Cloudflare Tunnel, TLS en nginx...) "
                    + "y usa cookie-secure=true con nombre __Host-.", cookieName);
        }
    }

    public ResponseCookie createJwtCookie(String token) {
        return ResponseCookie.from(cookieName, token)
                .httpOnly(cookieHttpOnly)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path(cookiePath)
                .maxAge(cookieMaxAge)
                .build();
    }

    public ResponseCookie createExpiredCookie() {
        return ResponseCookie.from(cookieName, "")
                .httpOnly(cookieHttpOnly)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path(cookiePath)
                .maxAge(0)
                .build();
    }
}
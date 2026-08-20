package com.puntomartinez.millete.shared.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieAuthFactory {

    private final String cookieName;
    private final boolean cookieSecure;
    private final boolean cookieHttpOnly;
    private final String cookieSameSite;
    private final String cookiePath;
    private final long cookieMaxAge;

    public CookieAuthFactory(
            @Value("${jwt.cookie-name:ms_token}") String cookieName,
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
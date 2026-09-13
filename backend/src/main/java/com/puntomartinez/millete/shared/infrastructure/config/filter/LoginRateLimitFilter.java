package com.puntomartinez.millete.shared.infrastructure.config.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_ATTEMPTS = 20;
    private static final long WINDOW_MS = TimeUnit.MINUTES.toMillis(1);
    private static final int MAX_IPS = 500;
    private static final String LOGIN_PATH = "/api/v1/auth/login";

    private final ConcurrentHashMap<String, RateLimitEntry> attemptsPerIp =
            new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if (!isLoginRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = request.getRemoteAddr();
        long now = System.currentTimeMillis();

        RateLimitEntry entry = attemptsPerIp.compute(clientIp, (ip, current) -> {
            if (current == null || now - current.windowStart > WINDOW_MS) {
                return new RateLimitEntry(now, 1);
            }

            current.attempts++;
            return current;
        });

        if (entry.attempts > MAX_ATTEMPTS) {
            long secondsUntilReset =
                    Math.max(0, (WINDOW_MS - (now - entry.windowStart)) / 1000);

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"Demasiados intentos. Espera %d segundos.\"}"
                            .formatted(secondsUntilReset)
            );
            return;
        }

        if (attemptsPerIp.size() > MAX_IPS) {
            evictExpiredEntries(now);
        }

        filterChain.doFilter(request, response);
    }

    private boolean isLoginRequest(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod())
                && request.getRequestURI().equals(LOGIN_PATH);
    }

    private void evictExpiredEntries(long now) {
        for (Map.Entry<String, RateLimitEntry> entry : attemptsPerIp.entrySet()) {
            if (now - entry.getValue().windowStart > WINDOW_MS) {
                attemptsPerIp.remove(entry.getKey(), entry.getValue());
            }
        }
    }

    private static final class RateLimitEntry {

        private final long windowStart;
        private int attempts;

        private RateLimitEntry(long windowStart, int attempts) {
            this.windowStart = windowStart;
            this.attempts = attempts;
        }
    }
}
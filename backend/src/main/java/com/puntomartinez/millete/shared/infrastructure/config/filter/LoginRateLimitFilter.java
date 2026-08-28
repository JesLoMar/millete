package com.puntomartinez.millete.shared.infrastructure.config.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_ATTEMPTS = 20;
    private static final long WINDOW_MS = TimeUnit.MINUTES.toMillis(1);
    private static final int MAX_IPS = 10_000;
    private static final String LOGIN_PATH = "/api/v1/auth/login";

    private final ConcurrentHashMap<String, AtomicInteger> attemptsPerIp = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> windowStartPerIp = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (!isLoginRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = request.getRemoteAddr();
        long now = System.currentTimeMillis();

        Long windowStart = windowStartPerIp.get(clientIp);
        if (windowStart == null || now - windowStart > WINDOW_MS) {

            if (attemptsPerIp.size() >= MAX_IPS) {
                evictExpiredEntries(now);
            }
            windowStartPerIp.put(clientIp, now);
            attemptsPerIp.put(clientIp, new AtomicInteger(0));
            windowStart = now;
        }

        AtomicInteger attempts = attemptsPerIp.get(clientIp);
        int currentAttempts = attempts != null ? attempts.incrementAndGet() : 1;

        if (currentAttempts > MAX_ATTEMPTS) {
            long secondsUntilReset = Math.max(0, (WINDOW_MS - (now - windowStart)) / 1000);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"Demasiados intentos. Espera %d segundos.\"}"
                            .formatted(secondsUntilReset)
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isLoginRequest(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod()) &&
                request.getRequestURI().equals(LOGIN_PATH);
    }

    private void evictExpiredEntries(long now) {
        windowStartPerIp.entrySet().removeIf(entry -> now - entry.getValue() > WINDOW_MS);
        attemptsPerIp.keySet().retainAll(windowStartPerIp.keySet());
    }
}
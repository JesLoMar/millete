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

public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MS = TimeUnit.MINUTES.toMillis(1);
    private static final String LOGIN_PATH = "/api/v1/auth/login";

    private final ConcurrentHashMap<String, AttemptWindow> attemptsPerIp = new ConcurrentHashMap<>();

    public LoginRateLimitFilter() {
        Thread cleanupThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(TimeUnit.MINUTES.toMillis(1));
                    attemptsPerIp.entrySet().removeIf(entry -> entry.getValue().isExpired());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "rate-limit-cleanup");
        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (!isLoginRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);

        AttemptWindow window = attemptsPerIp.compute(clientIp, (ip, existing) -> {
            if (existing == null || existing.isExpired()) {
                return new AttemptWindow();
            }
            existing.increment();
            return existing;
        });

        if (window.isBlocked()) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"Demasiados intentos. Espera %d segundos.\"}"
                            .formatted(window.secondsUntilReset())
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isLoginRequest(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod()) &&
                request.getRequestURI().equals(LOGIN_PATH);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class AttemptWindow {
        private int attempts;
        private final long windowStart;

        AttemptWindow() {
            this.attempts = 1;
            this.windowStart = System.currentTimeMillis();
        }

        void increment() {
            this.attempts++;
        }

        boolean isExpired() {
            return System.currentTimeMillis() - windowStart > WINDOW_MS;
        }

        boolean isBlocked() {
            return !isExpired() && attempts > MAX_ATTEMPTS;
        }

        long secondsUntilReset() {
            long elapsed = System.currentTimeMillis() - windowStart;
            return Math.max(0, (WINDOW_MS - elapsed) / 1000);
        }
    }
}
package com.puntomartinez.millete.shared.infrastructure.config.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Rate limiting para el endpoint de login.
 *
 * <p>Implementación en memoria (ConcurrentHashMap) diseñada para
 * despliegues self-hosted de una sola instancia. Para despliegues
 * multi-instancia, migrar a Redis + Bucket4j o rate limiting en
 * el proxy/nginx.</p>
 *
 * <p>La IP del cliente se resuelve respetando X-Forwarded-For cuando
 * la aplicación está detrás de un proxy inverso (nginx, Cloudflare).
 * El proxy DEBE configurarse para enviar la cabecera de forma fiable.</p>
 */
@Component
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

        String clientIp = resolveClientIp(request);
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

    /**
     * Resuelve la IP real del cliente.
     *
     * <p>Si la aplicación está detrás de un proxy inverso (nginx,
     * Cloudflare), usa la primera entrada de X-Forwarded-For.
     * El proxy debe estar configurado para enviar esta cabecera
     * de forma fiable y sobreescribir cualquier valor enviado
     * por el cliente.</p>
     *
     * <p>Configuración mínima de nginx:
     * <pre>
     * proxy_set_header X-Forwarded-For $remote_addr;
     * </pre>
     * </p>
     */
    public static String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    public void resetForIp(String ip) {
        attemptsPerIp.remove(ip);
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
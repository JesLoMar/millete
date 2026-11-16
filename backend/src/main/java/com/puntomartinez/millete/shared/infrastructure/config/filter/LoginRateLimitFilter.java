package com.puntomartinez.millete.shared.infrastructure.config.filter;

import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
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
 * <p>Solo proxies configurados explícitamente pueden aportar X-Forwarded-For.</p>
 */
@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_ATTEMPTS = 20;
    private static final long WINDOW_MS = TimeUnit.MINUTES.toMillis(1);
    private static final int MAX_IPS = 500;
    private static final String LOGIN_PATH = "/api/v1/auth/login";
    private static final String CLIENT_IP_ATTRIBUTE =
            LoginRateLimitFilter.class.getName() + ".clientIp";

    private final ConcurrentHashMap<String, RateLimitEntry> attemptsPerIp =
            new ConcurrentHashMap<>();
    private final TrustedProxyClientIpResolver clientIpResolver;
    private final TimeProvider timeProvider;

    public LoginRateLimitFilter(
            @Value("${MILLETE_TRUSTED_PROXIES:}") String trustedProxies,
            TimeProvider timeProvider) {
        this.clientIpResolver = new TrustedProxyClientIpResolver(trustedProxies);
        this.timeProvider = timeProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain)
            throws ServletException, IOException {

        if (!isLoginRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = clientIpResolver.resolve(request);
        request.setAttribute(CLIENT_IP_ATTRIBUTE, clientIp);
        long now = timeProvider.now().toEpochMilli();
        RateLimitEntry entry;
        synchronized (attemptsPerIp) {
            evictExpiredEntries(now);
            entry = attemptsPerIp.get(clientIp);
            if (entry == null) {
                if (attemptsPerIp.size() >= MAX_IPS) {
                    reject(response, 60);
                    return;
                }
                entry = new RateLimitEntry(now, 1);
                attemptsPerIp.put(clientIp, entry);
            } else if (now - entry.windowStart > WINDOW_MS) {
                entry = new RateLimitEntry(now, 1);
                attemptsPerIp.put(clientIp, entry);
            } else {
                entry.attempts++;
            }
        }

        if (entry.attempts > MAX_ATTEMPTS) {
            long secondsUntilReset =
                    Math.max(0, (WINDOW_MS - (now - entry.windowStart)) / 1000);
            reject(response, secondsUntilReset);
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Devuelve la IP validada por el filtro para la petición actual o, si
     * todavía no se procesó, la IP de conexión. Nunca confía directamente
     * en encabezados reenviados.
     */
    public static String resolveClientIp(HttpServletRequest request) {
        Object resolved = request.getAttribute(CLIENT_IP_ATTRIBUTE);
        if (resolved instanceof String address) {
            return address;
        }
        String peer = TrustedProxyClientIpResolver.normalize(request.getRemoteAddr());
        return peer == null ? request.getRemoteAddr() : peer;
    }

    public void resetForIp(String ip) {
        attemptsPerIp.remove(ip);
    }

    private boolean isLoginRequest(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod())
                && request.getRequestURI().equals(LOGIN_PATH);
    }

    private void evictExpiredEntries(long now) {
        attemptsPerIp.entrySet().removeIf(
                entry -> now - entry.getValue().windowStart > WINDOW_MS);
    }

    private void reject(HttpServletResponse response, long secondsUntilReset)
            throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"Demasiados intentos. Espera %d segundos.\"}"
                        .formatted(secondsUntilReset));
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

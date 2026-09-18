package com.puntomartinez.millete.shared.infrastructure.config.filter;

import com.puntomartinez.millete.users.domain.ports.out.TokenProvider;
import com.puntomartinez.millete.users.domain.ports.out.UserSessionRepository;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import io.jsonwebtoken.JwtException;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

/**
 * Filtro de autenticación JWT basado en cookie httpOnly.
 *
 * <p>DEPENDENCIA CON 'users': Este filtro depende de TokenProvider y
 * UserSessionRepository del módulo 'users'. Esto es intencional porque
 * el filtro es intrínsecamente parte del sistema de identidad. El módulo
 * 'shared' actúa como kernel de autenticación y necesita conocer los
 * puertos de identidad.</p>
 *
 * <p>VARIABLES DE ENTORNO:
 * <ul>
 *   <li>{@code jwt.cookie-name} — Nombre de la cookie JWT.
 *       Default: {@code __Host-ms_token}. Si usa prefijo {@code __Host-},
 *       la cookie DEBE servirse con Secure y Path=/ (validado en
 *       CookieAuthFactory).</li>
 *   <li>{@code jwt.cookie-secure} — Default: {@code true}.</li>
 *   <li>{@code jwt.cookie-same-site} — Default: {@code Strict}.</li>
 * </ul>
 * </p>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;
    private final UserSessionRepository userSessionRepository;
    private final String cookieName;

    public JwtAuthenticationFilter(TokenProvider tokenProvider,
                                    UserSessionRepository userSessionRepository,
                                    @Value("${jwt.cookie-name}") String cookieName) {
        this.tokenProvider = tokenProvider;
        this.userSessionRepository = userSessionRepository;
        this.cookieName = cookieName;
    }

    @Override
    protected void doFilterInternal(
            @Nonnull HttpServletRequest request,
            @Nonnull HttpServletResponse response,
            @Nonnull FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = extractJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenProvider.isTokenValid(jwt)) {
                authenticateRequest(request, jwt, response);
            }
        } catch (JwtException | IllegalArgumentException ex) {
            logger.warn("Invalid JWT authentication data: " + ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateRequest(
            HttpServletRequest request,
            String jwt,
            HttpServletResponse response) {

        String userId = tokenProvider.extractUserId(jwt);
        String sessionIdStr = tokenProvider.getClaim(jwt, "sessionId");

        if (userId == null || sessionIdStr == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        UUID userUuid = UUID.fromString(userId);
        UUID sessionId = UUID.fromString(sessionIdStr);

        if (!userSessionRepository.existsByIdAndActiveTrue(sessionId)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String email = tokenProvider.getClaim(jwt, "email");
            String username = tokenProvider.getClaim(jwt, "username");

            JwtUser jwtUser = new JwtUser(userUuid, username, email);

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            jwtUser,
                            null,
                            Collections.emptyList()
                    );

            authToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            SecurityContextHolder.getContext().setAuthentication(authToken);
            request.setAttribute("sessionId", sessionId);
        }
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
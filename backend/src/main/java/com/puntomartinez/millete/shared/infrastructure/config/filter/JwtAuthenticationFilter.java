package com.puntomartinez.millete.shared.infrastructure.config.filter;

import com.puntomartinez.millete.users.domain.ports.out.TokenProvider;
import com.puntomartinez.millete.users.domain.ports.out.UserSessionRepository;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;
    private final UserSessionRepository userSessionRepository;

    public JwtAuthenticationFilter(TokenProvider tokenProvider, UserSessionRepository userSessionRepository) {
        this.tokenProvider = tokenProvider;
        this.userSessionRepository = userSessionRepository;
    }

    @Override
    protected void doFilterInternal(
            @Nonnull HttpServletRequest request,
            @Nonnull HttpServletResponse response,
            @Nonnull FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = extractJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenProvider.isTokenValid(jwt)) {
                String userId = tokenProvider.extractUserId(jwt);
                String sessionIdStr = tokenProvider.getClaim(jwt, "sessionId");

                if (sessionIdStr == null || !userSessionRepository.existsByIdAndActiveTrue(UUID.fromString(sessionIdStr))) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    String email = tokenProvider.getClaim(jwt, "email");
                    String username = tokenProvider.getClaim(jwt, "username");
                    JwtUser jwtUser = new JwtUser(UUID.fromString(userId), username, email);

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            jwtUser,
                            null,
                            Collections.emptyList()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    request.setAttribute("sessionId", UUID.fromString(sessionIdStr));
                }
            }
        } catch (Exception ex) {
            logger.error("JWT Authentication failed: " + ex.getMessage(), ex);
        }

        filterChain.doFilter(request, response);
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("ms_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
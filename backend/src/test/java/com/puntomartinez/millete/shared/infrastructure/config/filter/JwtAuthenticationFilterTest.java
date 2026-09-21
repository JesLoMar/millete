package com.puntomartinez.millete.shared.infrastructure.config.filter;

import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import com.puntomartinez.millete.users.domain.ports.out.TokenProvider;
import com.puntomartinez.millete.users.domain.ports.out.UserSessionRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter")
class JwtAuthenticationFilterTest {

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private UserSessionRepository userSessionRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(
                tokenProvider, userSessionRepository, "__Host-ms_token"
        );
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("no cookie scenarios")
    class NoCookieScenarios {

        @Test
        @DisplayName("Should pass through when no cookies present")
        void shouldPassThroughWhenNoCookies()
                throws ServletException, IOException {
            when(request.getCookies()).thenReturn(null);

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext()
                    .getAuthentication()).isNull();
        }

        @Test
        @DisplayName("Should pass through when JWT cookie not found")
        void shouldPassThroughWhenCookieNotFound()
                throws ServletException, IOException {
            Cookie otherCookie = new Cookie("other", "value");
            when(request.getCookies())
                    .thenReturn(new Cookie[]{otherCookie});

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext()
                    .getAuthentication()).isNull();
        }
    }

    @Nested
    @DisplayName("valid token scenarios")
    class ValidTokenScenarios {

        @Test
        @DisplayName("Should authenticate with valid token and active session")
        void shouldAuthenticateWithValidToken()
                throws ServletException, IOException {
            UUID userId = UUID.randomUUID();
            UUID sessionId = UUID.randomUUID();
            String jwt = "valid-jwt-token";

            Cookie jwtCookie = new Cookie("__Host-ms_token", jwt);
            when(request.getCookies())
                    .thenReturn(new Cookie[]{jwtCookie});
            when(tokenProvider.isTokenValid(jwt)).thenReturn(true);
            when(tokenProvider.extractUserId(jwt))
                    .thenReturn(userId.toString());
            when(tokenProvider.getClaim(jwt, "sessionId"))
                    .thenReturn(sessionId.toString());
            when(tokenProvider.getClaim(jwt, "email"))
                    .thenReturn("user@example.com");
            when(tokenProvider.getClaim(jwt, "username"))
                    .thenReturn("testuser");
            when(userSessionRepository.existsByIdAndActiveTrue(sessionId))
                    .thenReturn(true);

            filter.doFilterInternal(request, response, filterChain);

            assertThat(SecurityContextHolder.getContext()
                    .getAuthentication()).isNotNull();
            JwtUser principal = (JwtUser) SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal();
            assertThat(principal.getId()).isEqualTo(userId);
            assertThat(principal.getUsername()).isEqualTo("testuser");
            assertThat(principal.getEmail()).isEqualTo("user@example.com");
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should set sessionId request attribute")
        void shouldSetSessionIdAttribute()
                throws ServletException, IOException {
            UUID userId = UUID.randomUUID();
            UUID sessionId = UUID.randomUUID();
            String jwt = "valid-jwt-token";

            Cookie jwtCookie = new Cookie("__Host-ms_token", jwt);
            when(request.getCookies())
                    .thenReturn(new Cookie[]{jwtCookie});
            when(tokenProvider.isTokenValid(jwt)).thenReturn(true);
            when(tokenProvider.extractUserId(jwt))
                    .thenReturn(userId.toString());
            when(tokenProvider.getClaim(jwt, "sessionId"))
                    .thenReturn(sessionId.toString());
            when(tokenProvider.getClaim(jwt, "email")).thenReturn(null);
            when(tokenProvider.getClaim(jwt, "username")).thenReturn(null);
            when(userSessionRepository.existsByIdAndActiveTrue(sessionId))
                    .thenReturn(true);

            filter.doFilterInternal(request, response, filterChain);

            verify(request).setAttribute("sessionId", sessionId);
        }
    }

    @Nested
    @DisplayName("invalid token scenarios")
    class InvalidTokenScenarios {

        @Test
        @DisplayName("Should return 401 when userId is null in token")
        void shouldReturn401WhenUserIdNull()
                throws ServletException, IOException {
            String jwt = "jwt-without-user";
            Cookie jwtCookie = new Cookie("__Host-ms_token", jwt);
            when(request.getCookies())
                    .thenReturn(new Cookie[]{jwtCookie});
            when(tokenProvider.isTokenValid(jwt)).thenReturn(true);
            when(tokenProvider.extractUserId(jwt)).thenReturn(null);

            filter.doFilterInternal(request, response, filterChain);

            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            // El filtro no detiene la cadena, solo marca el status 401 y deja que Spring Security lo gestione después
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should return 401 when session is inactive")
        void shouldReturn401WhenSessionInactive()
                throws ServletException, IOException {
            UUID userId = UUID.randomUUID();
            UUID sessionId = UUID.randomUUID();
            String jwt = "valid-jwt";

            Cookie jwtCookie = new Cookie("__Host-ms_token", jwt);
            when(request.getCookies())
                    .thenReturn(new Cookie[]{jwtCookie});
            when(tokenProvider.isTokenValid(jwt)).thenReturn(true);
            when(tokenProvider.extractUserId(jwt))
                    .thenReturn(userId.toString());
            when(tokenProvider.getClaim(jwt, "sessionId"))
                    .thenReturn(sessionId.toString());
            when(userSessionRepository.existsByIdAndActiveTrue(sessionId))
                    .thenReturn(false);

            filter.doFilterInternal(request, response, filterChain);

            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            // El filtro no detiene la cadena, solo marca el status 401 y deja que Spring Security lo gestione después
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should pass through when token is invalid")
        void shouldPassThroughWhenTokenInvalid()
                throws ServletException, IOException {
            String jwt = "invalid-jwt";
            Cookie jwtCookie = new Cookie("__Host-ms_token", jwt);
            when(request.getCookies())
                    .thenReturn(new Cookie[]{jwtCookie});
            when(tokenProvider.isTokenValid(jwt)).thenReturn(false);

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext()
                    .getAuthentication()).isNull();
        }
    }
}
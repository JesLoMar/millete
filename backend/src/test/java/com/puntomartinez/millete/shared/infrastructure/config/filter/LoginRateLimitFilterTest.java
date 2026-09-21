package com.puntomartinez.millete.shared.infrastructure.config.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginRateLimitFilter")
class LoginRateLimitFilterTest {

    @InjectMocks
    private LoginRateLimitFilter filter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private PrintWriter writer;

    @BeforeEach
    void setUp() throws IOException {
        lenient().when(response.getWriter()).thenReturn(writer);
    }

    @Nested
    @DisplayName("non-login requests")
    class NonLoginRequests {

        @Test
        @DisplayName("Should pass through GET requests without rate limiting")
        void shouldPassThroughGetRequests()
                throws ServletException, IOException {
            when(request.getMethod()).thenReturn("GET");
            lenient().when(request.getRequestURI())
                    .thenReturn("/api/v1/transactions");

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should pass through POST to non-login endpoints")
        void shouldPassThroughPostToNonLoginEndpoints()
                throws ServletException, IOException {
            when(request.getMethod()).thenReturn("POST");
            when(request.getRequestURI())
                    .thenReturn("/api/v1/categories");

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("login requests")
    class LoginRequests {

        @Test
        @DisplayName("Should allow first login attempt")
        void shouldAllowFirstLoginAttempt()
                throws ServletException, IOException {
            when(request.getMethod()).thenReturn("POST");
            when(request.getRequestURI())
                    .thenReturn("/api/v1/auth/login");
            when(request.getRemoteAddr()).thenReturn("192.168.1.1");
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verify(response, never()).setStatus(429);
        }

        @Test
        @DisplayName("Should block after exceeding max attempts")
        void shouldBlockAfterExceedingMaxAttempts()
                throws ServletException, IOException {
            when(request.getMethod()).thenReturn("POST");
            when(request.getRequestURI())
                    .thenReturn("/api/v1/auth/login");
            when(request.getRemoteAddr()).thenReturn("10.0.0.1");
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);

            // Exhaust 20 attempts
            for (int i = 0; i < 20; i++) {
                filter.doFilterInternal(request, response, filterChain);
            }

            // 21st attempt should be blocked
            filter.doFilterInternal(request, response, filterChain);

            verify(response).setStatus(429);
            verify(response).setContentType("application/json");
        }

        @Test
        @DisplayName("Should track IPs independently")
        void shouldTrackIpsIndependently()
                throws ServletException, IOException {
            HttpServletRequest request2 = mock(HttpServletRequest.class);
            when(request.getMethod()).thenReturn("POST");
            when(request.getRequestURI())
                    .thenReturn("/api/v1/auth/login");
            when(request.getRemoteAddr()).thenReturn("10.0.0.1");
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);

            when(request2.getMethod()).thenReturn("POST");
            when(request2.getRequestURI())
                    .thenReturn("/api/v1/auth/login");
            when(request2.getRemoteAddr()).thenReturn("10.0.0.2");
            when(request2.getHeader("X-Forwarded-For")).thenReturn(null);

            // Exhaust IP1
            for (int i = 0; i < 20; i++) {
                filter.doFilterInternal(request, response, filterChain);
            }

            // IP2 should still be allowed
            filter.doFilterInternal(request2, response, filterChain);

            verify(filterChain, atLeast(21)).doFilter(any(), eq(response));
        }

        @Test
        @DisplayName("Should reset rate limit for specific IP")
        void shouldResetRateLimitForIp()
                throws ServletException, IOException {
            when(request.getMethod()).thenReturn("POST");
            when(request.getRequestURI())
                    .thenReturn("/api/v1/auth/login");
            when(request.getRemoteAddr()).thenReturn("10.0.0.5");
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);

            // Use some attempts
            for (int i = 0; i < 15; i++) {
                filter.doFilterInternal(request, response, filterChain);
            }

            // Reset
            filter.resetForIp("10.0.0.5");

            // Should be able to make more attempts
            filter.doFilterInternal(request, response, filterChain);

            verify(response, never()).setStatus(429);
        }
    }

    @Nested
    @DisplayName("resolveClientIp")
    class ResolveClientIp {

        @Test
        @DisplayName("Should use X-Forwarded-For first entry when present")
        void shouldUseXForwardedForFirstEntry() {
            HttpServletRequest req = mock(HttpServletRequest.class);
            when(req.getHeader("X-Forwarded-For"))
                    .thenReturn("203.0.113.50, 70.41.3.18, 150.172.238.178");

            String ip = LoginRateLimitFilter.resolveClientIp(req);

            assertThat(ip).isEqualTo("203.0.113.50");
        }

        @Test
        @DisplayName("Should use remote address when X-Forwarded-For is absent")
        void shouldUseRemoteAddrWhenNoXForwardedFor() {
            HttpServletRequest req = mock(HttpServletRequest.class);
            when(req.getHeader("X-Forwarded-For")).thenReturn(null);
            when(req.getRemoteAddr()).thenReturn("192.168.1.100");

            String ip = LoginRateLimitFilter.resolveClientIp(req);

            assertThat(ip).isEqualTo("192.168.1.100");
        }

        @Test
        @DisplayName("Should use remote address when X-Forwarded-For is blank")
        void shouldUseRemoteAddrWhenXForwardedForBlank() {
            HttpServletRequest req = mock(HttpServletRequest.class);
            when(req.getHeader("X-Forwarded-For")).thenReturn("   ");
            when(req.getRemoteAddr()).thenReturn("192.168.1.200");

            String ip = LoginRateLimitFilter.resolveClientIp(req);

            assertThat(ip).isEqualTo("192.168.1.200");
        }
    }
}
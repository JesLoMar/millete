package com.puntomartinez.millete.shared.infrastructure.in.controller;

import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;
import com.puntomartinez.millete.shared.infrastructure.config.SecurityConfig;
import com.puntomartinez.millete.shared.infrastructure.config.filter.JwtAuthenticationFilter;
import com.puntomartinez.millete.shared.infrastructure.config.filter.LoginRateLimitFilter;
import com.puntomartinez.millete.users.domain.ports.out.TokenProvider;
import com.puntomartinez.millete.users.domain.ports.out.UserSessionRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitWebConfig(HealthEndpointTest.Config.class)
@TestPropertySource(properties = "jwt.cookie-name=__Host-ms_token")
class HealthEndpointTest {

    @Configuration
    @EnableWebMvc
    @Import({SecurityConfig.class, JwtAuthenticationFilter.class, LoginRateLimitFilter.class, HealthController.class})
    static class Config {
    }

    @MockitoBean
    TokenProvider tokenProvider;

    @MockitoBean
    UserSessionRepository userSessionRepository;

    @MockitoBean
    TimeProvider timeProvider;

    @Autowired
    WebApplicationContext context;

    @Autowired
    FilterChainProxy securityFilterChain;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(securityFilterChain).build();
    }

    @Test
    void reportsAvailabilityWithoutAuthenticationOrCaching() throws Exception {
        mvc.perform(get("/api/v1/health"))
                .andExpect(status().isNoContent())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(content().string(""));
    }

    @Test
    void ignoresSessionCookiesOnTheHealthEndpoint() throws Exception {
        mvc.perform(get("/api/v1/health").cookie(new Cookie("__Host-ms_token", "stale-session")))
                .andExpect(status().isNoContent());

        verifyNoInteractions(tokenProvider, userSessionRepository);
    }

    @Test
    void keepsOtherRoutesAndMethodsProtected() throws Exception {
        mvc.perform(get("/api/v1/auth/me/topnav")).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/health")).andExpect(status().isForbidden());
    }
}

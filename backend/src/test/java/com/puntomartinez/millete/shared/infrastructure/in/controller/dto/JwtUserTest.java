package com.puntomartinez.millete.shared.infrastructure.in.controller.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtUser")
class JwtUserTest {

    @Test
    @DisplayName("Should store id, username and email")
    void shouldStoreAllFields() {
        UUID id = UUID.randomUUID();
        JwtUser user = new JwtUser(id, "testuser", "test@example.com");

        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should allow null username and email")
    void shouldAllowNullUsernameAndEmail() {
        UUID id = UUID.randomUUID();
        JwtUser user = new JwtUser(id, null, null);

        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getUsername()).isNull();
        assertThat(user.getEmail()).isNull();
    }

    @Test
    @DisplayName("Should be usable as Authentication principal")
    void shouldBeUsableAsAuthenticationPrincipal() {
        UUID id = UUID.randomUUID();
        JwtUser user = new JwtUser(id, "admin", "admin@example.com");

        // Simulates what JwtAuthenticationFilter does
        Object principal = user;

        assertThat(principal).isInstanceOf(JwtUser.class);
        assertThat(((JwtUser) principal).getId()).isEqualTo(id);
    }
}
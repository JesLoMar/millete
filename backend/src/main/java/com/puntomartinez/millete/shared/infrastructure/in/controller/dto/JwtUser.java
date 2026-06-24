package com.puntomartinez.millete.shared.infrastructure.in.controller.dto;

import java.util.UUID;

public class JwtUser {
    private final UUID id;
    private final String username;
    private final String email;

    public JwtUser(UUID id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }
}
